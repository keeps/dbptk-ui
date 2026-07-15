/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.Set;

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

    return entries.stream().map(context::getDenormalizeConfig)
      .anyMatch(config -> config != null && config.shouldProcess() && !config.isMarkedForRemoval());
  }
}
