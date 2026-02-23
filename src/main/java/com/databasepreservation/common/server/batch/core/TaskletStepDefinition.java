package com.databasepreservation.common.server.batch.core;

import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * Interface defining a Tasklet-based step in a batch job. A
 * TaskletStepDefinition specifies how to create a Tasklet that will execute the
 * step's logic when invoked by the batch framework. The Tasklet is responsible
 * for performing the actual work of the step, and can access job-level
 * configuration and status through the JobContext, as well as manage state and
 * share data across executions using the provided ExecutionContext. This
 * interface extends StepDefinition to include tasklet-specific behavior, while
 * still adhering to the execution and error policies defined in the
 * StepDefinition contract.
 * 
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TaskletStepDefinition extends StepDefinition {

  /**
   * Creates a Tasklet instance for the given execution context. The Tasklet will
   * be responsible for executing the step's logic, and can access job-level
   * configuration and status through the JobContext, as well as manage state and
   * share data across executions using the provided ExecutionContext.
   * 
   * @param context
   *          The job context providing access to job-level configuration and
   *          status.
   * @param executionContext
   *          The execution context specific to the current partition or step
   *          execution, allowing for state management and sharing across
   *          executions.
   * @return A Tasklet instance that will execute the step's logic when invoked by
   *         the batch framework.
   */
  Tasklet createTasklet(JobContext context, ExecutionContext executionContext);
}
