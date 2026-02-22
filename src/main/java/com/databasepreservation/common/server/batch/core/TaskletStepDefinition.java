package com.databasepreservation.common.server.batch.core;

import com.databasepreservation.common.server.batch.context.JobContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TaskletStepDefinition extends StepDefinition {
  Tasklet createTasklet(JobContext context, ExecutionContext partitionContext);
}
