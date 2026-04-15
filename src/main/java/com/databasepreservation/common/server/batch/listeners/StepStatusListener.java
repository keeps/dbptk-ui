package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.StepDefinition;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.controller.JobController;

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
    if (stepExecution.getStepName().contains(BatchConstants.PARTITION_PREFIX)
      || stepExecution.getStepName().contains(BatchConstants.PARTITION_WORKER_NAME)) {
      return;
    }

    context.incrementStepNumber();
    context.setCurrentStepName(definition.getDisplayName());

    stepExecution.getExecutionContext().putString(BatchConstants.CONTEXT_STEP_DISPLAY_NAME_KEY,
      definition.getDisplayName());

    LOGGER.info("[STEP] [{}/{}] STARTED: {} for database: {}", context.getCurrentStepNumber(), context.getTotalSteps(),
      definition.getName(), context.getDatabaseUUID());

    try {
      JobController.syncJobStateToSolr(stepExecution.getJobExecution());
    } catch (Exception e) {
      LOGGER.warn("[STEP] Failed to sync step start state to Solr for step: {}", definition.getName(), e);
    }
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    try {
      definition.onStepCompleted(context, stepExecution.getStatus());

      if (definition instanceof TaskletStepDefinition && stepExecution.getStatus() == BatchStatus.COMPLETED) {
        long processed = definition.calculateWorkload(context);
        context.getJobProgressAggregator().addProgress(stepExecution.getId(), processed);
      }

      LOGGER.info("[STEP] [{}/{}] FINISHED: {} with status: {}. Total items: {}", context.getCurrentStepNumber(),
        context.getTotalSteps(), definition.getName(), stepExecution.getExitStatus().getExitCode(),
        context.getJobProgressAggregator().getTotal());

      // Sync step completion state (crucial for Tasklets) to the Solr UI
      try {
        JobController.syncJobStateToSolr(stepExecution.getJobExecution());
      } catch (Exception e) {
        LOGGER.warn("[STEP] Failed to sync step completion state to Solr for step: {}", definition.getName(), e);
      }

    } catch (Exception e) {
      LOGGER.error("[STEP] FAILED during onStepCompleted for step {}", definition.getName(), e);
      return ExitStatus.FAILED;
    }
    return stepExecution.getExitStatus();
  }
}
