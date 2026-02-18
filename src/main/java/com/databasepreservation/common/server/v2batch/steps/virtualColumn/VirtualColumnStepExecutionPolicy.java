package com.databasepreservation.common.server.v2batch.steps.virtualColumn;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.server.v2batch.common.policy.ExecutionPolicy;
import com.databasepreservation.common.server.v2batch.job.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStepExecutionPolicy implements ExecutionPolicy {
  @Override
  public boolean shouldExecute(JobContext context) {
    CollectionStatus status = context.getCollectionStatus();

    if (status.getTables() == null) {
      return false;
    }

    return status.getTables().stream().anyMatch(this::hasVirtualColumnsToProcess);
  }

  private boolean hasVirtualColumnsToProcess(TableStatus table) {
    return table.getColumns().stream().anyMatch(this::isVirtualColumnPending);
  }

  private boolean isVirtualColumnPending(ColumnStatus column) {
    return column.getVirtualColumnStatus() != null && column.getVirtualColumnStatus().shouldProcess();
  }
}
