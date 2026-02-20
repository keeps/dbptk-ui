package com.databasepreservation.common.server.v2batch.steps.denormalization;

import java.util.Set;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.v2batch.common.policy.ExecutionPolicy;
import com.databasepreservation.common.server.v2batch.job.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepExecutionPolicy implements ExecutionPolicy {
  @Override
  public boolean shouldExecute(JobContext context) {
    CollectionStatus status = context.getCollectionStatus();

    if (status.getDenormalizations() == null || status.getDenormalizations().isEmpty()) {
      return false;
    }

    Set<String> entries = status.getDenormalizations();
    for (String entryID : entries) {
      DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

      if (config != null && config.shouldProcess()) {
        return true;
      }
    }

    return false;
  }
}
