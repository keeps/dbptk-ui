package com.databasepreservation.common.server.batch.policy;

import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ExecutionPolicy {
  boolean shouldExecute(JobContext context);
}
