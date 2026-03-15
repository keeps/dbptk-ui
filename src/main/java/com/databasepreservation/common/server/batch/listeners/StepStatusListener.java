package com.databasepreservation.common.server.batch.listeners;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.StepDefinition;
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
    try {

      if (stepExecution.getStepName().contains(BatchConstants.PARTITION_PREFIX)
        || stepExecution.getStepName().contains(BatchConstants.PARTITION_WORKER_NAME)) {
        return;
      }
      context.incrementStepNumber();
      context.setCurrentStepName(definition.getDisplayName());

      JobController.editSolrBatchJob(stepExecution.getJobExecution(), context);
      LOGGER.info("[STEP] [{}/{}] STARTED: {} for database: {}", context.getCurrentStepNumber(),
        context.getTotalSteps(), definition.getName(), context.getDatabaseUUID());
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("[STEP] ERROR: Cannot update Step on SOLR for {}", context.getDatabaseUUID(), e);
    }
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    try {
      definition.onStepCompleted(context, stepExecution.getStatus());

      LOGGER.info("[STEP] [{}/{}] FINISHED: {} with status: {}. Total items: {}", context.getCurrentStepNumber(),
        context.getTotalSteps(), definition.getName(), stepExecution.getExitStatus().getExitCode(),
        context.getJobProgressAggregator().getTotal());

    } catch (Exception e) {
      LOGGER.error("[STEP] FAILED during onStepCompleted for step {}", definition.getName(), e);
      return ExitStatus.FAILED;
    }
    return stepExecution.getExitStatus();
  }
}
