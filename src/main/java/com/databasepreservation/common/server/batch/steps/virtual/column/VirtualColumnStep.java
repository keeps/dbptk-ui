package com.databasepreservation.common.server.batch.steps.virtual.column;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.AbstractIndexingStepDefinition;
import com.databasepreservation.common.server.batch.core.PartitionableStep;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;
import com.databasepreservation.common.server.batch.steps.partition.TablePartitionStrategy;

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
  public String getName() {
    return "virtualColumnStep";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new VirtualColumnStepExecutionPolicy();
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new TablePartitionStrategy(solrManager, VirtualColumnStepUtils::hasVirtualColumnsToProcess);
  }

  @Override
  public long calculateWorkload(JobContext context) {
    return calculatePartitionedWorkload(context);
  }

  @Override
  public ItemProcessor<ViewerRow, ViewerRow> createProcessor(JobContext context, ExecutionContext partitionContext) {
    return new VirtualColumnStepProcessor(context, partitionContext.getString("tableId"));
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext partitionContext, BatchStatus status)
    throws BatchJobException {
    if (status == BatchStatus.COMPLETED) {
      String tableId = partitionContext.getString("tableId");
      TableStatus tableStatus = VirtualColumnStepUtils.findTableStatus(jobContext, tableId);

      if (tableStatus != null) {
        VirtualColumnStepUtils.updateProcessedColumnsStateInMemory(tableStatus, status);
      }

    }
  }

  @Override
  public void onStepCompleted(JobContext jobContext, BatchStatus status) throws BatchJobException {
    if (status == BatchStatus.COMPLETED) {
      for (TableStatus tableStatus : jobContext.getCollectionStatus().getTables()) {
        VirtualColumnStepUtils.removeMarkedVirtualColumnsInMemory(tableStatus);
      }
    }
  }
}
