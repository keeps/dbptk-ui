package com.databasepreservation.common.server.batch.steps.virtual.table;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
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

  private boolean isTableToProcess(TableStatus table) {
    return table.getVirtualTableStatus() != null && table.getVirtualTableStatus().shouldProcess()
      && !table.getVirtualTableStatus().isMarkedForRemoval();
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new TablePartitionStrategy(solrManager, this::isTableToProcess,
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
      TableStatus tableStatus = jobContext.getCollectionStatus().findTableStatusById(tableId);

      if (tableStatus != null && tableStatus.getVirtualTableStatus() != null) {
        tableStatus.updateProcessedVirtualTableState();
      }
    }
  }
}
