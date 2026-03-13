package com.databasepreservation.common.server.batch.steps.virtual.table;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.AbstractIndexingStepDefinition;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.PartitionableStep;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;
import com.databasepreservation.common.server.batch.steps.partition.TablePartitionStrategy;
import com.databasepreservation.common.server.batch.steps.virtual.VirtualEntityStepUtils;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

@Component
public class VirtualTableStep extends AbstractIndexingStepDefinition<ViewerRow, ViewerRow>
  implements PartitionableStep {
  @Override
  public String getDisplayName() {
    return "Virtual Table Processing";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new VirtualTableStepExecutionPolicy();
  }

  @Override
  public DatabaseRowsSolrManager.WriteMode getWriteSolrMode() {
    return DatabaseRowsSolrManager.WriteMode.INSERT;
  }

  @Override
  public ItemProcessor<ViewerRow, ViewerRow> createProcessor(JobContext context, ExecutionContext executionContext) {
    return new VirtualTableStepProcessor(context, executionContext.getString(BatchConstants.TABLE_ID_KEY));
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new TablePartitionStrategy(solrManager, VirtualEntityStepUtils::hasVirtualTableToProcess,
      VirtualEntityStepUtils::enrichVirtualTablePartitionContext);
  }

  @Override
  public long calculateWorkload(JobContext context) {
    return calculatePartitionedWorkload(context);
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext partitionContext, BatchStatus status)
    throws BatchJobException {
    if (status == BatchStatus.COMPLETED) {
      String tableId = partitionContext.getString(BatchConstants.TABLE_ID_KEY);
      TableStatus tableStatus = VirtualEntityStepUtils.findTableStatus(jobContext, tableId);

      if (tableStatus != null && tableStatus.getVirtualTableStatus() != null) {
        ProcessingState state = tableStatus.getVirtualTableStatus().getProcessingState();

        if (state == ProcessingState.TO_REMOVE) {
          Filter filter = FilterUtils.filterByTableUUID(new Filter(), tableStatus.getUuid());
          try {
            solrManager.deleteRowsByQuery(jobContext.getDatabaseUUID(), filter);
          } catch (ViewerException e) {
            throw new BatchJobException("Failed to delete rows from Solr for virtual table with ID: " + tableId, e);
          }
        } else {
          VirtualEntityStepUtils.updateProcessedTableStateInMemory(tableStatus, status);
        }
      }
    }
  }

  @Override
  public void onStepCompleted(JobContext jobContext, BatchStatus status) throws BatchJobException {
    if (status == BatchStatus.COMPLETED) {

      try {
        ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, jobContext.getDatabaseUUID());
        ViewerMetadata metadata = database.getMetadata();
        boolean metadataChanged = false;

        for (TableStatus tableStatus : jobContext.getCollectionStatus().getTables()) {
          if (tableStatus.getVirtualTableStatus() != null) {
            ProcessingState state = tableStatus.getVirtualTableStatus().getProcessingState();

            if (state == ProcessingState.TO_REMOVE) {
              ViewerTable viewerTable = metadata.getTable(tableStatus.getUuid());
              if (viewerTable != null) {
                metadata.getSchema(viewerTable.getSchemaUUID()).getTables()
                  .removeIf(t -> t.getUuid().equals(tableStatus.getUuid()));
                metadataChanged = true;
              }
            } else {
              ViewerTable originalTable = metadata.getTable(tableStatus.getVirtualTableStatus().getSourceTableUUID());
              if (originalTable != null) {
                ViewerTable virtualViewerTable = buildVirtualViewerTable(tableStatus, originalTable);
                metadata.getSchema(virtualViewerTable.getSchemaUUID()).getTables()
                  .removeIf(t -> t.getUuid().equals(virtualViewerTable.getUuid()));
                metadata.getSchema(virtualViewerTable.getSchemaUUID()).getTables().add(virtualViewerTable);
                metadataChanged = true;
              }
            }
          }
        }

        if (metadataChanged) {
          solrManager.updateDatabaseMetadata(database.getUuid(), metadata);
        }

      } catch (NotFoundException | GenericException e) {
        throw new BatchJobException("Failed to update Solr metadata centrally in VirtualTableStep", e);
      }

      VirtualEntityStepUtils.removeMarkedVirtualTableInMemory(jobContext.getCollectionStatus());
      jobContext.getCollectionStatus().setNeedsToBeProcessed(false);

      try {
        ViewerFactory.getConfigurationManager().updateCollectionStatus(jobContext.getDatabaseUUID(),
          jobContext.getCollectionStatus());
      } catch (Exception e) {
        throw new BatchJobException("Failed to save CollectionStatus to disk after completing VirtualTableStep", e);
      }
    }
  }

  public static ViewerTable buildVirtualViewerTable(TableStatus tableStatus, ViewerTable originalTable) {
    ViewerTable viewerTable = new ViewerTable();
    viewerTable.setUuid(tableStatus.getUuid());
    viewerTable.setId(tableStatus.getId());
    viewerTable.setName(tableStatus.getName());
    viewerTable.setDescription(tableStatus.getDescription());
    viewerTable.setFolder(tableStatus.getTableFolder());

    // Get Only columns present on tableStatus columns, filter by columnId
    List<ViewerColumn> columnsTonInclude = tableStatus.getColumns().stream()
      .map(column -> originalTable.getColumnBySolrName(column.getId())).filter(c -> c != null).toList();
    viewerTable.setColumns(columnsTonInclude);

    // Form original viewer table
    viewerTable.setSchemaUUID(originalTable.getSchemaUUID());
    viewerTable.setSchemaName(originalTable.getSchemaName());
    viewerTable.setCountRows(originalTable.getCountRows());
    viewerTable.setTriggers(originalTable.getTriggers());
    viewerTable.setCheckConstraints(originalTable.getCheckConstraints());
    viewerTable.setCandidateKeys(originalTable.getCandidateKeys());

    // For primary key, we need to check if any of the columns that compose the
    // primary key are present in the virtual table
    if (originalTable.getPrimaryKey() != null) {
      if (columnsTonInclude.stream().anyMatch(c -> originalTable.getPrimaryKey().getColumnIndexesInViewerTable()
        .contains(c.getColumnIndexInEnclosingTable()))) {
        viewerTable.setPrimaryKey(originalTable.getPrimaryKey());
      }
    }

    // For foreign keys, we need to filter only those that are related to the
    // columns present in the virtual table
    viewerTable.setForeignKeys(new ArrayList<>());
    if (tableStatus.getVirtualTableStatus().getUseSourceTableForeignKeys()) {
      if (originalTable.getForeignKeys() != null) {
        for (ViewerColumn viewerColumn : columnsTonInclude) {
          List<ViewerForeignKey> viewerForeignKeys = originalTable.getForeignKeys().stream()
            .filter(fk -> fk.getReferences().stream()
              .anyMatch(r -> r.getSourceColumnIndex().equals(viewerColumn.getColumnIndexInEnclosingTable())))
            .toList();
          if (!viewerForeignKeys.isEmpty()) {
            viewerTable.getForeignKeys().addAll(viewerForeignKeys);
          }
        }
      }
    }

    return viewerTable;
  }
}
