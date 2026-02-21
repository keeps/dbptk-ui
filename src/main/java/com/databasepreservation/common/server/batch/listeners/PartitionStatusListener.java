package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.StepDefinition;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PartitionStatusListener implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(PartitionStatusListener.class);

  private final StepDefinition<?, ?> definition;
  private final JobContext context;

  public PartitionStatusListener(StepDefinition<?, ?> definition, JobContext context) {
    this.definition = definition;
    this.context = context;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    LOGGER.debug("[Worker] Starting partition for step: {}", definition.getName());
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    try {
      definition.onPartitionCompleted(context, stepExecution.getExecutionContext(), stepExecution.getStatus());

      if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
        LOGGER.info("[Worker] Partition for {} completed successfully", definition.getName());
      }

    } catch (Exception e) {
      LOGGER.error("[Worker] Error during onPartitionCompleted for step {}", definition.getName(), e);
      return ExitStatus.FAILED;
    }

    return stepExecution.getExitStatus();
  }
}
