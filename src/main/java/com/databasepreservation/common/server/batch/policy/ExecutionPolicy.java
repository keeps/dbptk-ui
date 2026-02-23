package com.databasepreservation.common.server.batch.policy;

import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * Defines the execution strategy for a Step, including gatekeeping
 * (shouldExecute) and performance tuning (chunk size and concurrency).
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ExecutionPolicy {

  /**
   * Gatekeeper method to determine if the step should be skipped or executed.
   * 
   * @param context
   *          The current job context.
   * @return true if the step has work to do.
   */
  boolean shouldExecute(JobContext context);

  /**
   * Defines the commit interval (how many items are processed before writing).
   * 
   * @return Default is 100.
   */
  default int getChunkSize() {
    return 100;
  }

  /**
   * Defines the maximum number of threads for parallel processing of this step.
   * 
   * @return Default is 1 (single-threaded).
   */
  default int getConcurrencyLimit() {
    return 1;
  }
}
