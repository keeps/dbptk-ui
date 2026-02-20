package com.databasepreservation.common.server.v2batch.steps.denormalization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
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
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.v2batch.exceptions.DataTransformationException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepProcessor
  implements ItemProcessor<ViewerRow, DenormalizationStepProcessor.NestedDocumentWrapper> {

  public record NestedDocumentWrapper(String parentUUID, List<SolrInputDocument> nestedDocuments) {
  }

  private final DatabaseRowsSolrManager solrManager;
  private final DenormalizeConfiguration config;
  private ViewerDatabase database;
  private final String databaseUUID;

  public DenormalizationStepProcessor(DatabaseRowsSolrManager solrManager, DenormalizeConfiguration config,
    String databaseUUID) {
    this.solrManager = solrManager;
    this.config = config;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public DenormalizationStepProcessor.NestedDocumentWrapper process(ViewerRow row) throws Exception {
    ensureMetadataIsLoaded();

    List<SolrInputDocument> nestedDocuments = new ArrayList<>();

    try {
      for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
        processRelatedTableRecursively(row, relatedTable, nestedDocuments);
      }
    } catch (SolrServerException | IOException e) {
      // Propagate Solr and IO exceptions as they are critical and should trigger
      // retry logic if configured.
      throw e;
    } catch (Exception e) {
      throw new DataTransformationException("Error processing row " + row.getUuid() + " for denormalization", e);
    }

    return nestedDocuments.isEmpty() ? null
      : new DenormalizationStepProcessor.NestedDocumentWrapper(row.getUuid(), nestedDocuments);
  }

  private void ensureMetadataIsLoaded() throws SolrServerException, IOException, BatchJobException {
    try {

      this.database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    } catch (NotFoundException e) {
      throw new BatchJobException("Database not found for UUID: " + databaseUUID, e);
    } catch (GenericException e) {
      // If the cause is a SolrServerException or IOException, rethrow it to allow
      // retry logic to handle it.
      if (e.getCause() instanceof SolrServerException) {
        throw (SolrServerException) e.getCause();
      } else if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }

      // For other types of exceptions, wrap them in a BatchJobException to indicate a
      // failure
      throw new BatchJobException("Error retrieving database metadata for UUID: " + databaseUUID, e);
    }
  }

  private void processRelatedTableRecursively(ViewerRow row, RelatedTablesConfiguration relatedTable,
    List<SolrInputDocument> nestedDocuments) throws SolrServerException, IOException {

    Filter filter = buildJoinFilter(row, relatedTable);
    if (filter == null)
      return;

    List<String> fieldsToReturn = buildFieldsToReturn(relatedTable);
    List<String> columnsToDisplay = getColumnsIncludedNames(relatedTable);

    try (IterableIndexResult nestedRows = solrManager.findAllRows(databaseUUID, filter, null, fieldsToReturn)) {
      for (ViewerRow nestedRow : nestedRows) {
        // 1. Process children first (Recursion)
        for (RelatedTablesConfiguration innerTable : relatedTable.getRelatedTables()) {
          processRelatedTableRecursively(nestedRow, innerTable, nestedDocuments);
        }

        // 2. Only create a nested document for this table if there are columns
        // configured to be included.
        if (!columnsToDisplay.isEmpty()) {
          addNestedDocument(nestedRow, nestedDocuments, relatedTable, columnsToDisplay);
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

    // Add fields needed for recursion and nested document creation
    fields.add(String.format("%s:\"%s\"", ViewerConstants.SOLR_ROWS_NESTED_UUID, relatedTable.getUuid()));
    fields.add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_ORIGINAL_UUID, ViewerConstants.INDEX_ID));
    fields.add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_TABLE_ID, ViewerConstants.SOLR_ROWS_TABLE_ID));

    // Add referenced table fields for join conditions
    for (RelatedTablesConfiguration inner : relatedTable.getRelatedTables()) {
      for (ReferencesConfiguration ref : inner.getReferences()) {
        fields.add(ref.getReferencedTable().getSolrName());
      }
    }

    // Add fields that are configured to be included in the nested document
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

  private void addNestedDocument(ViewerRow row, List<SolrInputDocument> nestedDocuments,
    RelatedTablesConfiguration relatedTable, List<String> columnsToDisplay) {

    ViewerTable tableMeta = database.getMetadata().getTable(relatedTable.getTableUUID());
    if (tableMeta == null)
      return;

    Map<String, Object> fields = new HashMap<>();
    for (ViewerColumn col : tableMeta.getColumns()) {
      if (!columnsToDisplay.contains(col.getSolrName()))
        continue;

      ViewerCell cell = row.getCells().get(col.getSolrName());
      if (cell == null)
        continue;

      String key = ViewerConstants.SOLR_ROWS_NESTED_COL + col.getSolrName();
      fields.put(key, resolveFieldValue(col, cell, tableMeta, row));
    }

    if (!fields.isEmpty()) {
      nestedDocuments.add(solrManager.createNestedDocument(row.getNestedUUID(), row.getUuid(),
        row.getNestedOriginalUUID(), fields, row.getTableId(), row.getNestedUUID()));
    }
  }

  private Object resolveFieldValue(ViewerColumn col, ViewerCell cell, ViewerTable table, ViewerRow row) {
    if ("BINARY LARGE OBJECT".equals(col.getType().getTypeName())) {
      return String.format("%s%s/%s/collection/%s/data/%s/%s/%s/%d", ViewerConstants.API_SERVLET,
        ViewerConstants.API_V1_DATABASE_RESOURCE, databaseUUID, databaseUUID, table.getSchemaName(), table.getName(),
        row.getNestedOriginalUUID(), col.getColumnIndexInEnclosingTable());
    }
    return cell.getValue();
  }
}
