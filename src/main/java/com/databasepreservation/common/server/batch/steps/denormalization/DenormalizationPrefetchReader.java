package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.AndFiltersParameters;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.OneOfManyFilterParameter;
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
 * A specialized wrapper for {@link ItemStreamReader} that implements a Bulk
 * Fetching (Prefetching) strategy to solve the N+1 select problem when
 * denormalizing hierarchical data from Solr. *
 * <p>
 * Instead of querying the database for each individual parent row, this reader
 * consumes a chunk of rows from the delegate reader, extracts all required
 * foreign keys, and executes a single optimized "IN" query to fetch all related
 * child rows simultaneously. It then maps the children back to their respective
 * parents in-memory.
 * </p>
 * *
 * <p>
 * This strategy drastically reduces I/O latency and prevents thread exhaustion
 * on the Solr server during heavy Spring Batch executions.
 * </p>
 * * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationPrefetchReader implements ItemStreamReader<ViewerRow> {

  private final ItemStreamReader<ViewerRow> delegate;
  private final int chunkSize;
  private final DatabaseRowsSolrManager solrManager;
  private final DenormalizeConfiguration config;
  private final String databaseUUID;
  private final ViewerDatabase database;
  private static final int MAX_CLAUSES_BATCH_SIZE = 500;

  // In-memory buffer to hold the fully enriched chunk before yielding to the
  // Processor
  private final List<ViewerRow> buffer = new ArrayList<>();
  private int bufferIndex = 0;

  /**
   * Constructs a new Prefetch Reader.
   *
   * @param delegate
   *          The base reader responsible for fetching the raw parent rows.
   * @param chunkSize
   *          The size of the chunk to prefetch. Should match the Step's chunk
   *          size.
   * @param solrManager
   *          The Solr client wrapper to execute the bulk queries.
   * @param config
   *          The denormalization configuration tree.
   * @param databaseUUID
   *          The unique identifier of the target database.
   * @param database
   *          The pre-loaded database metadata.
   */
  public DenormalizationPrefetchReader(ItemStreamReader<ViewerRow> delegate, int chunkSize,
    DatabaseRowsSolrManager solrManager, DenormalizeConfiguration config, String databaseUUID,
    ViewerDatabase database) {
    this.delegate = delegate;
    this.chunkSize = chunkSize;
    this.solrManager = solrManager;
    this.config = config;
    this.databaseUUID = databaseUUID;
    this.database = database;
  }

  @Override
  public ViewerRow read() throws Exception {
    // If the buffer is exhausted, fetch and enrich the next chunk
    if (bufferIndex >= buffer.size()) {
      fetchNextBuffer();
    }

    // Return the next fully enriched row from the buffer, or null if the delegate
    // is exhausted
    if (bufferIndex < buffer.size()) {
      return buffer.get(bufferIndex++);
    }

    return null;
  }

  /**
   * Reads a chunk of raw rows from the delegate reader, initializes their nested
   * lists, and triggers the bulk enrichment process.
   */
  private void fetchNextBuffer() throws Exception {
    buffer.clear();
    bufferIndex = 0;

    // 1. Fill the buffer with the next chunk of parent rows
    for (int i = 0; i < chunkSize; i++) {
      ViewerRow row = delegate.read();
      if (row == null)
        break; // End of data

      row.setNestedRowList(new ArrayList<>());
      buffer.add(row);
    }

    // 2. If we have rows, execute the bulk fetch for the entire relational tree
    if (!buffer.isEmpty() && config.getRelatedTables() != null) {
      for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
        enrichWithRelatedTables(buffer, relatedTable);
      }
    }
  }

  /**
   * Recursively fetches and maps related child rows using a Breadth-First Search
   * (BFS) approach. Handles sub-batching to prevent Solr maxBooleanClauses syntax
   * errors.
   */
  private void enrichWithRelatedTables(List<ViewerRow> parentRows, RelatedTablesConfiguration relatedTable)
    throws Exception {
    if (parentRows == null || parentRows.isEmpty())
      return;

    for (int i = 0; i < parentRows.size(); i += MAX_CLAUSES_BATCH_SIZE) {
      int end = Math.min(i + MAX_CLAUSES_BATCH_SIZE, parentRows.size());
      List<ViewerRow> parentBatch = parentRows.subList(i, end);

      processBatchOfRelatedTables(parentBatch, relatedTable);
    }
  }

  private void processBatchOfRelatedTables(List<ViewerRow> parentRows, RelatedTablesConfiguration relatedTable)
    throws Exception {

    List<ReferencesConfiguration> refs = relatedTable.getReferences();
    if (refs == null || refs.isEmpty())
      return;

    // STEP 1: Extract unique Foreign Keys from the parent chunk
    Map<String, Set<String>> columnToValues = new HashMap<>();
    for (ReferencesConfiguration ref : refs) {
      columnToValues.put(ref.getSourceTable().getSolrName(), new HashSet<>());
    }

    // Map to keep track of which parent expects which exact set of keys
    Map<ViewerRow, Map<String, String>> parentToExpectedKeys = new HashMap<>();

    for (ViewerRow parent : parentRows) {
      Map<String, String> expectedKeys = new HashMap<>();
      boolean hasNullKey = false;

      for (ReferencesConfiguration ref : refs) {
        String parentCol = ref.getReferencedTable().getSolrName();
        ViewerCell cell = parent.getCells().get(parentCol);
        String val = cell != null ? cell.getValue() : null;

        if (val == null) {
          hasNullKey = true;
          break; // If a composite key is partially null, skip this parent
        }

        expectedKeys.put(ref.getSourceTable().getSolrName(), val);
        columnToValues.get(ref.getSourceTable().getSolrName()).add(val);
      }

      if (!hasNullKey) {
        parentToExpectedKeys.put(parent, expectedKeys);
      }
    }

    if (parentToExpectedKeys.isEmpty())
      return;

    // STEP 2: Build the optimized Solr Query
    List<FilterParameter> params = new ArrayList<>();
    params.add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, relatedTable.getTableID()));

    for (Map.Entry<String, Set<String>> entry : columnToValues.entrySet()) {
      params.add(new OneOfManyFilterParameter(entry.getKey(), new ArrayList<>(entry.getValue())));
    }

    Filter filter = new Filter();
    filter.add(new AndFiltersParameters(params));
    List<String> fieldsToReturn = buildFieldsToReturn(relatedTable);

    // STEP 3: Execute a single query to fetch ALL relevant children for the entire
    // chunk
    List<ViewerRow> allFetchedChildren = new ArrayList<>();
    try (IterableIndexResult nestedRows = solrManager.findAllRows(databaseUUID, filter, null, fieldsToReturn)) {
      for (ViewerRow nestedRow : nestedRows) {
        nestedRow.setNestedRowList(new ArrayList<>());
        allFetchedChildren.add(nestedRow);
      }
    }

    if (allFetchedChildren.isEmpty())
      return;

    // STEP 4: Recursion (Fetch Grandchildren BEFORE attaching children to parents)
    if (relatedTable.getRelatedTables() != null) {
      for (RelatedTablesConfiguration innerTable : relatedTable.getRelatedTables()) {
        enrichWithRelatedTables(allFetchedChildren, innerTable);
      }
    }

    // STEP 5: In-memory association
    List<String> columnsToDisplay = getColumnsIncludedNames(relatedTable);

    for (ViewerRow parent : parentRows) {
      Map<String, String> expectedKeys = parentToExpectedKeys.get(parent);
      if (expectedKeys == null)
        continue;

      for (ViewerRow child : allFetchedChildren) {
        boolean matches = true;

        // Verify if the child matches the parent's foreign key requirements
        for (Map.Entry<String, String> expected : expectedKeys.entrySet()) {
          ViewerCell childCell = child.getCells().get(expected.getKey());
          if (childCell == null || !expected.getValue().equals(childCell.getValue())) {
            matches = false;
            break;
          }
        }

        // If it matches, format and attach the child document
        if (matches) {
          if (!columnsToDisplay.isEmpty() || !child.getNestedRowList().isEmpty()) {
            addNestedDocument(parent, child, relatedTable, columnsToDisplay);
          }
        }
      }
    }
  }

  /**
   * Transforms a raw child row into a nested document format and attaches it to
   * the parent row.
   */
  private void addNestedDocument(ViewerRow parent, ViewerRow child, RelatedTablesConfiguration relatedTable,
    List<String> columnsToDisplay) {
    ViewerTable tableMeta = database.getMetadata().getTable(relatedTable.getTableUUID());
    if (tableMeta == null)
      return;

    ViewerRow nestedEntry = new ViewerRow();
    nestedEntry.setNestedUUID(relatedTable.getUuid());
    nestedEntry.setNestedTableId(child.getTableId());
    nestedEntry.setNestedOriginalUUID(child.getUuid());

    // Convert columns to proper nested fields format
    for (ViewerColumn col : tableMeta.getColumns()) {
      if (columnsToDisplay.contains(col.getSolrName())) {
        ViewerCell cell = child.getCells().get(col.getSolrName());
        if (cell != null) {
          String key = ViewerConstants.SOLR_ROWS_NESTED_COL + col.getSolrName();
          ViewerCell newCell = new ViewerCell();
          newCell.setValue(resolveFieldValue(col, cell, tableMeta, child).toString());
          nestedEntry.getCells().put(key, newCell);
        }
      }
    }

    // 1. Append the immediate child to the parent's flat list
    parent.getNestedRowList().add(nestedEntry);

    // 2. FLATTENING LOGIC: If this child has its own descendants (grandchildren,
    // etc.),
    // pull them up and append them directly to the root parent's list to maintain a
    // 100% flat structure.
    if (child.getNestedRowList() != null && !child.getNestedRowList().isEmpty()) {
      parent.getNestedRowList().addAll(child.getNestedRowList());
    }
  }

  /**
   * Constructs the list of fields to project from the Solr query, minimizing
   * network payload.
   */
  private List<String> buildFieldsToReturn(RelatedTablesConfiguration relatedTable) {
    List<String> fields = new ArrayList<>();
    fields.add(ViewerConstants.INDEX_ID);
    fields.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    // Solr nested document metadata
    fields.add(String.format("%s:\"%s\"", ViewerConstants.SOLR_ROWS_NESTED_UUID, relatedTable.getUuid()));
    fields.add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_ORIGINAL_UUID, ViewerConstants.INDEX_ID));
    fields.add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_TABLE_ID, ViewerConstants.SOLR_ROWS_TABLE_ID));

    // MUST explicitly include the join keys required for Parent-Child mapping!
    if (relatedTable.getReferences() != null) {
      for (ReferencesConfiguration ref : relatedTable.getReferences()) {
        fields.add(ref.getSourceTable().getSolrName());
      }
    }

    // Include nested references required for Child-Grandchild mapping
    if (relatedTable.getRelatedTables() != null) {
      for (RelatedTablesConfiguration inner : relatedTable.getRelatedTables()) {
        if (inner.getReferences() != null) {
          for (ReferencesConfiguration ref : inner.getReferences()) {
            fields.add(ref.getReferencedTable().getSolrName());
          }
        }
      }
    }

    // Include the actual columns requested by the user
    if (relatedTable.getColumnsIncluded() != null) {
      for (RelatedColumnConfiguration col : relatedTable.getColumnsIncluded()) {
        fields.add(col.getSolrName());
      }
    }

    return fields;
  }

  private List<String> getColumnsIncludedNames(RelatedTablesConfiguration relatedTable) {
    if (relatedTable.getColumnsIncluded() == null)
      return new ArrayList<>();
    return relatedTable.getColumnsIncluded().stream().map(RelatedColumnConfiguration::getSolrName)
      .collect(Collectors.toList());
  }

  /**
   * Resolves the final value for a field, generating appropriate access URLs for
   * Binary Large Objects (BLOBs).
   */
  private Object resolveFieldValue(ViewerColumn col, ViewerCell cell, ViewerTable table, ViewerRow row) {
    if ("BINARY LARGE OBJECT".equals(col.getType().getTypeName())) {
      return String.format("%s%s/%s/collection/%s/data/%s/%s/%s/%d", ViewerConstants.API_SERVLET,
        ViewerConstants.API_V1_DATABASE_RESOURCE, databaseUUID, databaseUUID, table.getSchemaName(), table.getName(),
        row.getUuid(), col.getColumnIndexInEnclosingTable());
    }
    return cell.getValue();
  }

  // --- ItemStream pass-through methods to manage the delegate's state ---

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    delegate.open(executionContext);
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    delegate.update(executionContext);
  }

  @Override
  public void close() throws ItemStreamException {
    delegate.close();
  }
}
