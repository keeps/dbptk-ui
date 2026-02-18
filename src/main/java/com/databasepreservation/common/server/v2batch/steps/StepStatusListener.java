package com.databasepreservation.common.server.v2batch.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.v2batch.job.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class StepStatusListener implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(StepStatusListener.class);
  private final StepDefinition<?, ?> definition;
  private final JobContext jobContext;

  public StepStatusListener(StepDefinition<?, ?> definition, JobContext jobContext) {
    this.definition = definition;
    this.jobContext = jobContext;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    LOGGER.info("Starting step {}", stepExecution.getStepName());
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    try {
      LOGGER.info("Completed step {} with status {}", stepExecution.getStepName(), stepExecution.getStatus());
      definition.onStepCompleted(jobContext, stepExecution.getStatus());
      return stepExecution.getExitStatus();
    } catch (BatchJobException e) {
      LOGGER.error("Error while updating step status for step {}: {}", stepExecution.getStepName(), e.getMessage());
      // Spring Batch does not allow us to throw checked exceptions from this method,
      // so we wrap it in a RuntimeException
      throw new RuntimeException(e);
    }
  }
}
