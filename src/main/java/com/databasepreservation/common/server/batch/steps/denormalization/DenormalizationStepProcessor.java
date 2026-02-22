package com.databasepreservation.common.server.batch.steps.denormalization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
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
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.exceptions.DataTransformationException;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepProcessor implements ItemProcessor<ViewerRow, ViewerRow> {

  private final DatabaseRowsSolrManager solrManager;
  private final DenormalizeConfiguration config;
  private final String databaseUUID;
  private ViewerDatabase database;

  public DenormalizationStepProcessor(DatabaseRowsSolrManager solrManager, DenormalizeConfiguration config,
    String databaseUUID) {
    this.solrManager = solrManager;
    this.config = config;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    ensureMetadataIsLoaded();

    List<ViewerRow> results = new ArrayList<>();

    try {
      for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
        processRelatedTableRecursively(row, relatedTable, results);
      }
    } catch (SolrServerException | IOException e) {
      throw e;
    } catch (Exception e) {
      throw new DataTransformationException("Error processing row " + row.getUuid() + " for denormalization", e);
    }

    row.setNestedRowList(results);

    return results.isEmpty() ? null : row;
  }

  private void ensureMetadataIsLoaded() throws SolrServerException, IOException, BatchJobException {
    if (this.database != null)
      return;

    try {
      this.database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    } catch (NotFoundException e) {
      throw new BatchJobException("Database not found for UUID: " + databaseUUID, e);
    } catch (GenericException e) {
      if (e.getCause() instanceof SolrServerException)
        throw (SolrServerException) e.getCause();
      if (e.getCause() instanceof IOException)
        throw (IOException) e.getCause();
      throw new BatchJobException("Error retrieving database metadata for UUID: " + databaseUUID, e);
    }
  }

  private void processRelatedTableRecursively(ViewerRow row, RelatedTablesConfiguration relatedTable,
    List<ViewerRow> collector) throws SolrServerException, IOException {

    Filter filter = buildJoinFilter(row, relatedTable);
    if (filter == null)
      return;

    List<String> fieldsToReturn = buildFieldsToReturn(relatedTable);
    List<String> columnsToDisplay = getColumnsIncludedNames(relatedTable);

    // findAllRows allows iterating over large volumes of nested results without
    // loading everything into memory at once
    try (IterableIndexResult nestedRows = solrManager.findAllRows(databaseUUID, filter, null, fieldsToReturn)) {
      for (ViewerRow nestedRow : nestedRows) {

        nestedRow.getNestedRowList().clear();

        // Process children first (Recursion - Depth-first)
        for (RelatedTablesConfiguration innerTable : relatedTable.getRelatedTables()) {
          processRelatedTableRecursively(nestedRow, innerTable, collector);
        }

        // Create nested document for this table if there are configured columns
        if (!columnsToDisplay.isEmpty() || !nestedRow.getNestedRowList().isEmpty()) {
          addNestedDocument(nestedRow, relatedTable, columnsToDisplay, collector);
        }
      }
    }
  }

  private Filter buildJoinFilter(ViewerRow row, RelatedTablesConfiguration relatedTable) {
    List<FilterParameter> params = new ArrayList<>();
    params.add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, relatedTable.getTableID()));

    for (ReferencesConfiguration ref : relatedTable.getReferences()) {
      String value = row.getCells().containsKey(ref.getReferencedTable().getSolrName())
        ? row.getCells().get(ref.getReferencedTable().getSolrName()).getValue()
        : null;

      if (value == null)
        return null;
      params.add(new SimpleFilterParameter(ref.getSourceTable().getSolrName(), value));
    }

    Filter filter = new Filter();
    filter.add(new AndFiltersParameters(params));
    return filter;
  }

  private List<String> buildFieldsToReturn(RelatedTablesConfiguration relatedTable) {
    List<String> fields = new ArrayList<>();
    fields.add(ViewerConstants.INDEX_ID);
    fields.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    // Metadata needed for Solr's nested structure
    fields.add(String.format("%s:\"%s\"", ViewerConstants.SOLR_ROWS_NESTED_UUID, relatedTable.getUuid()));
    fields.add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_ORIGINAL_UUID, ViewerConstants.INDEX_ID));
    fields.add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_TABLE_ID, ViewerConstants.SOLR_ROWS_TABLE_ID));

    for (RelatedTablesConfiguration inner : relatedTable.getRelatedTables()) {
      for (ReferencesConfiguration ref : inner.getReferences()) {
        fields.add(ref.getReferencedTable().getSolrName());
      }
    }

    for (RelatedColumnConfiguration col : relatedTable.getColumnsIncluded()) {
      fields.add(col.getSolrName());
    }

    return fields;
  }

  private List<String> getColumnsIncludedNames(RelatedTablesConfiguration relatedTable) {
    List<String> names = new ArrayList<>();
    for (RelatedColumnConfiguration col : relatedTable.getColumnsIncluded()) {
      names.add(col.getSolrName());
    }
    return names;
  }

  private void addNestedDocument(ViewerRow nestedRow, RelatedTablesConfiguration relatedTable,
    List<String> columnsToDisplay, List<ViewerRow> collector) {

    ViewerTable tableMeta = database.getMetadata().getTable(relatedTable.getTableUUID());
    if (tableMeta == null)
      return;

    ViewerRow nestedEntry = new ViewerRow();
    nestedEntry.setNestedUUID(relatedTable.getUuid());
    nestedEntry.setNestedTableId(nestedRow.getTableId());
    nestedEntry.setNestedOriginalUUID(nestedRow.getUuid());

    for (ViewerColumn col : tableMeta.getColumns()) {
      if (columnsToDisplay.contains(col.getSolrName())) {

        ViewerCell cell = nestedRow.getCells().get(col.getSolrName());
        if (cell != null) {

          String key = ViewerConstants.SOLR_ROWS_NESTED_COL + col.getSolrName();
          ViewerCell newCell = new ViewerCell();
          newCell.setValue(resolveFieldValue(col, cell, tableMeta, nestedRow).toString());
          nestedEntry.getCells().put(key, newCell);
        }

      }
    }

    collector.add(nestedEntry);
  }

  private Object resolveFieldValue(ViewerColumn col, ViewerCell cell, ViewerTable table, ViewerRow row) {
    if ("BINARY LARGE OBJECT".equals(col.getType().getTypeName())) {
      // Generate access URL for binary fields (BLOBs)
      return String.format("%s%s/%s/collection/%s/data/%s/%s/%s/%d", ViewerConstants.API_SERVLET,
        ViewerConstants.API_V1_DATABASE_RESOURCE, databaseUUID, databaseUUID, table.getSchemaName(), table.getName(),
        row.getNestedOriginalUUID(), col.getColumnIndexInEnclosingTable());
    }
    return cell.getValue();
  }
}
