package com.databasepreservation.common.server.batch.core;

import org.springframework.batch.core.BatchStatus;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

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
   *         (must match the Spring Bean name)
   */
  default String getName() {
    Class<?> realClass = ClassUtils.getUserClass(this.getClass());
    return StringUtils.uncapitalize(realClass.getSimpleName());
  }

  /**
   * The human-readable name of the step to be displayed in the UI. Default
   * implementation returns the internal name if not overridden.
   */
  default String getDisplayName() {
    return getName();
  }

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

  /**
   * Estimates the volume of work for progress tracking.
   * 
   * @param context
   *          The current job context.
   * @return The total number of items or units of work (default 1).
   */
  default long calculateWorkload(JobContext context) {
    return 1L;
  }

  default void onStepStarted(JobContext context) throws BatchJobException {
  }

  /**
   * Lifecycle hook executed after the step finishes.
   * 
   * 
   * @param context
   *          The current job context.
   * @param status
   *          The resulting status of the step execution.
   * @throws BatchJobException
   *           if post-processing fails.
   */
  void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException;
}
