package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.StepDefinition;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class StepStatusListener implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(StepStatusListener.class);

  private final StepDefinition definition;
  private final JobContext context;

  public StepStatusListener(StepDefinition definition, JobContext context) {
    this.definition = definition;
    this.context = context;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    LOGGER.info("Starting Step: {} for database: {}", definition.getName(), context.getDatabaseUUID());
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    try {
      definition.onStepCompleted(context, stepExecution.getStatus());

      LOGGER.info("Step {} FINISHED with status: {}. Total items: {}", definition.getName(),
        stepExecution.getExitStatus().getExitCode(), context.getJobProgressAggregator().getTotal());

    } catch (Exception e) {
      LOGGER.error("Error during onStepCompleted for step {}", definition.getName(), e);
      return ExitStatus.FAILED;
    }
    return stepExecution.getExitStatus();
  }
}
