package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.Set;

import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepExecutionPolicy implements ExecutionPolicy {
  @Override
  public boolean shouldExecute(JobContext context) {
    Set<String> entries = context.getCollectionStatus().getDenormalizations();

    if (entries == null || entries.isEmpty()) {
      return false;
    }

    for (String entryID : entries) {
      DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

      if (config != null && config.shouldProcess()) {
        return true;
      }
    }

    return false;
  }
}
