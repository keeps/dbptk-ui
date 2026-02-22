package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.PartitionableStep;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PartitionStatusListener implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(PartitionStatusListener.class);

  private final PartitionableStep definition;
  private final JobContext context;

  public PartitionStatusListener(PartitionableStep definition, JobContext context) {
    this.definition = definition;
    this.context = context;
  }

  @Override
  public void beforeStep(StepExecution partitionExecution) {
    LOGGER.debug("[Worker] Starting partition for step: {}", partitionExecution.getStepName());
  }

  @Override
  public ExitStatus afterStep(StepExecution partitionExecution) {
    try {
      definition.onPartitionCompleted(context, partitionExecution.getExecutionContext(),
        partitionExecution.getStatus());

      if (partitionExecution.getStatus() == BatchStatus.COMPLETED) {
        LOGGER.info("[Worker] Partition for {} completed successfully", partitionExecution.getStepName());
      }

    } catch (Exception e) {
      LOGGER.error("[Worker] Error during onPartitionCompleted for step {}", partitionExecution.getStepName(), e);
      partitionExecution.setStatus(BatchStatus.FAILED);
      partitionExecution.addFailureException(e);
      return ExitStatus.FAILED;
    }

    return partitionExecution.getExitStatus();
  }
}
