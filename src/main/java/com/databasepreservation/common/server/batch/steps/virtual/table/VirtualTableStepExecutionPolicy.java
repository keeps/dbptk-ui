package com.databasepreservation.common.server.batch.steps.virtual.table;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public class VirtualTableStepExecutionPolicy implements ExecutionPolicy {
  @Override
  public boolean shouldExecute(JobContext context) {
    CollectionStatus status = context.getCollectionStatus();

    if (status.getTables() == null) {
      return false;
    }

    return status.getTables().stream().anyMatch(TableStatus::hasVirtualTableToProcess);
  }
}
