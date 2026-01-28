package com.databasepreservation.common.server.batch.item.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.AndFiltersParameters;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeProcessor implements ItemProcessor<ViewerRow, DenormalizeProcessor.NestedDocumentWrapper> {
  public static class NestedDocumentWrapper {
    private final String parentUUID;
    private final List<SolrInputDocument> nestedDocuments;

    public NestedDocumentWrapper(String parentUUID, List<SolrInputDocument> nestedDocuments) {
      this.parentUUID = parentUUID;
      this.nestedDocuments = nestedDocuments;
    }

    public String getParentUUID() {
      return parentUUID;
    }

    public List<SolrInputDocument> getNestedDocuments() {
      return nestedDocuments;
    }
  }

  private final DatabaseRowsSolrManager solrManager;
  private final DenormalizeConfiguration config;
  private final ViewerDatabase database;
  private final String databaseUUID;

  public DenormalizeProcessor(DatabaseRowsSolrManager solrManager, DenormalizeConfiguration config,
    ViewerDatabase database, String databaseUUID) {
    this.solrManager = solrManager;
    this.config = config;
    this.database = database;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public NestedDocumentWrapper process(ViewerRow row) throws Exception {
    List<SolrInputDocument> nestedDocuments = new ArrayList<>();

    for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
      queryOverRelatedTables(row, relatedTable, nestedDocuments);
    }

    if (nestedDocuments.isEmpty()) {
      return null;
    }

    return new NestedDocumentWrapper(row.getUuid(), nestedDocuments);
  }

  private void queryOverRelatedTables(ViewerRow row, RelatedTablesConfiguration relatedTable,
    List<SolrInputDocument> nestedDocuments) {

    String tableId = relatedTable.getTableID();
    Filter resultingFilter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();
    List<String> fieldsToReturn = new ArrayList<>();

    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_TABLE_ID);
    fieldsToReturn.add(String.format("%s:\"%s\"", ViewerConstants.SOLR_ROWS_NESTED_UUID, relatedTable.getUuid()));
    fieldsToReturn
      .add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_ORIGINAL_UUID, ViewerConstants.INDEX_ID));
    fieldsToReturn
      .add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_TABLE_ID, ViewerConstants.SOLR_ROWS_TABLE_ID));

    filterParameterList.add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, tableId));

    for (ReferencesConfiguration reference : relatedTable.getReferences()) {
      String sourceSolrName = reference.getSourceTable().getSolrName();
      String referencedSolrName = reference.getReferencedTable().getSolrName();
      String value = null;
      Map<String, ViewerCell> cells = row.getCells();
      for (Map.Entry<String, ViewerCell> entry : cells.entrySet()) {
        if (entry.getKey().equals(referencedSolrName)) {
          value = entry.getValue().getValue();
          break;
        }
      }
      if (value == null)
        return;
      filterParameterList.add(new SimpleFilterParameter(sourceSolrName, value));
    }

    resultingFilter.add(new AndFiltersParameters(filterParameterList));

    for (RelatedTablesConfiguration innerRelatedTable : relatedTable.getRelatedTables()) {
      for (ReferencesConfiguration reference : innerRelatedTable.getReferences()) {
        fieldsToReturn.add(reference.getReferencedTable().getSolrName());
      }
    }

    List<String> auxColumns = new ArrayList<>();
    for (ReferencesConfiguration reference : relatedTable.getReferences()) {
      auxColumns.add(reference.getReferencedTable().getSolrName());
    }

    List<String> columnsToDisplay = new ArrayList<>();
    for (RelatedColumnConfiguration relatedColumnConfiguration : relatedTable.getColumnsIncluded()) {
      columnsToDisplay.add(relatedColumnConfiguration.getSolrName());
    }

    fieldsToReturn.addAll(auxColumns);
    fieldsToReturn.addAll(columnsToDisplay);

    try (
      IterableIndexResult nestedRows = solrManager.findAllRows(databaseUUID, resultingFilter, null, fieldsToReturn)) {
      for (ViewerRow nestedRow : nestedRows) {
        for (RelatedTablesConfiguration innerRelatedTable : relatedTable.getRelatedTables()) {
          queryOverRelatedTables(nestedRow, innerRelatedTable, nestedDocuments);
        }
        if (!columnsToDisplay.isEmpty()) {
          createdNestedDocument(nestedRow, nestedDocuments, relatedTable.getTableUUID());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error querying nested tables", e);
    }
  }

  private void createdNestedDocument(ViewerRow row, List<SolrInputDocument> nestedDocuments, String relatedTableUUID) {
    Map<String, ViewerCell> cells = row.getCells();
    String uuid = row.getNestedUUID();

    Map<String, Object> fields = new HashMap<>();
    ViewerTable viewerTable = database.getMetadata().getTable(relatedTableUUID);
    for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
      for (Map.Entry<String, ViewerCell> cell : cells.entrySet()) {
        String key = ViewerConstants.SOLR_ROWS_NESTED_COL + cell.getKey();
        String urlPath = null;
        if (cell.getKey().equals(viewerColumn.getSolrName())
          && viewerColumn.getType().getTypeName().equals("BINARY LARGE OBJECT")) {
          urlPath = ViewerConstants.API_SERVLET + ViewerConstants.API_V1_DATABASE_RESOURCE + "/" + databaseUUID
            + "/collection/" + databaseUUID + "/data/" + viewerTable.getSchemaName() + "/" + viewerTable.getName() + "/"
            + row.getNestedOriginalUUID() + "/" + viewerColumn.getColumnIndexInEnclosingTable();
          fields.put(key, urlPath);

        } else if (cell.getKey().equals(viewerColumn.getSolrName())) {
          ViewerCell cellValue = cell.getValue();
          fields.put(key, cellValue.getValue());
        }
      }
    }
    if (!fields.isEmpty()) {
      nestedDocuments.add(solrManager.createNestedDocument(uuid, row.getUuid(), row.getNestedOriginalUUID(), fields,
        row.getTableId(), row.getNestedUUID()));
    }
  }
}
