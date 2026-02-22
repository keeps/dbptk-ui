package com.databasepreservation.common.server.batch.core;

import org.springframework.batch.core.BatchStatus;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface StepDefinition {

  /**
   * @return The unique name of the step, used for identification within the Job.
   */
  String getName();

  /**
   * Defines how the step should be executed, including concurrency and chunk size
   * settings.
   * 
   * @return The execution policy for this step.
   */
  ExecutionPolicy getExecutionPolicy();

  /**
   * Defines fault-tolerance behavior, specifying which exceptions are retryable
   * or skippable.
   * 
   * @return The error policy for this step.
   */
  ErrorPolicy getErrorPolicy();

  default long calculateWorkload(JobContext context) {
    return 1L;
  }

  void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException;
}
