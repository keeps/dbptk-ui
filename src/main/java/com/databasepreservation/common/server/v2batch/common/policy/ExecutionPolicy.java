package com.databasepreservation.common.server.v2batch.common.policy;

import com.databasepreservation.common.server.v2batch.job.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@FunctionalInterface
public interface ExecutionPolicy {
  boolean shouldExecute(JobContext context);
}
