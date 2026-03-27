package com.databasepreservation.common.server.batch.steps.virtual.column;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.AbstractIndexingStepDefinition;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.PartitionableStep;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;
import com.databasepreservation.common.server.batch.steps.partition.TablePartitionStrategy;
import com.databasepreservation.common.server.batch.steps.virtual.VirtualEntityStepUtils;
import com.databasepreservation.common.server.batch.steps.virtual.VirtualSchemaBuilderUtils;

/**
 * Step responsible for computing virtual columns. It is chunk-oriented and
 * partitionable by table.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualColumnStep extends AbstractIndexingStepDefinition<ViewerRow, ViewerRow>
  implements PartitionableStep {

  @Override
  public String getDisplayName() {
    return "Virtual Columns Processing";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new VirtualColumnStepExecutionPolicy();
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new TablePartitionStrategy(solrManager, TableStatus::hasVirtualColumnsToProcess,
      VirtualEntityStepUtils::enrichVirtualColumnPartitionContext);
  }

  @Override
  public long calculateWorkload(JobContext context) {
    return calculatePartitionedWorkload(context);
  }

  @Override
  public ItemProcessor<ViewerRow, ViewerRow> createProcessor(JobContext context, ExecutionContext partitionContext) {
    return new VirtualColumnStepProcessor(context, partitionContext.getString(BatchConstants.TABLE_ID_KEY));
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext partitionContext, BatchStatus status)
    throws BatchJobException {
    if (status == BatchStatus.COMPLETED) {
      String tableId = partitionContext.getString(BatchConstants.TABLE_ID_KEY);
      TableStatus tableStatus = jobContext.getCollectionStatus().findTableStatusById(tableId);
      if (tableStatus != null) {
        tableStatus.getColumns().stream().filter(ColumnStatus::isVirtual)
          .filter(ColumnStatus::hasVirtualColumnToProcess).filter(c -> !c.getVirtualColumnStatus().isMarkedForRemoval())
          .forEach(c -> c.getVirtualColumnStatus().markAsPendingMetadata());

        jobContext.changeViewerDatabase(database -> {
          ViewerMetadata metadata = database.getMetadata();
          ViewerTable viewerTable = metadata.getTable(tableStatus.getUuid());
          if (viewerTable != null) {
            if (!(viewerTable.getColumns() instanceof java.util.ArrayList)) {
              viewerTable.setColumns(new java.util.ArrayList<>(viewerTable.getColumns()));
            }

            tableStatus.getColumns().stream().filter(c -> c.isVirtual() && c.getVirtualColumnStatus() != null)
              .filter(c -> c.getVirtualColumnStatus().isPendingMetadata()).forEach(column -> {
                viewerTable.getColumns().removeIf(c -> c.getSolrName().equals(column.getId()));
                viewerTable.getColumns().add(VirtualSchemaBuilderUtils.buildViewerColumn(column));
              });
          }
        });
      }
    }
  }
}
