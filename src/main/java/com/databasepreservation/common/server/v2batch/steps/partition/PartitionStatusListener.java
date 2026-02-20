package com.databasepreservation.common.server.v2batch.steps.partition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.v2batch.job.JobContext;
import com.databasepreservation.common.server.v2batch.steps.StepDefinition;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PartitionStatusListener implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(PartitionStatusListener.class);
  private final StepDefinition<?, ?> definition;
  private final JobContext jobContext;

  public PartitionStatusListener(StepDefinition<?, ?> definition, JobContext jobContext) {
    this.definition = definition;
    this.jobContext = jobContext;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    String partitionName = stepExecution.getExecutionContext().getString("name", "Unknown");
    LOGGER.info("Starting partition step {} - {}", stepExecution.getStepName(), partitionName);
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    try {
      String partitionName = stepExecution.getExecutionContext().getString("name", "Unknown");
      LOGGER.info("Completed partition step {} - {} with status {}", stepExecution.getStepName(), partitionName,
        stepExecution.getStatus());
      definition.onPartitionCompleted(jobContext, stepExecution.getExecutionContext(), stepExecution.getStatus());
      return stepExecution.getExitStatus();
    } catch (BatchJobException e) {
      LOGGER.error("Error while updating partition status for step {}: {}", stepExecution.getStepName(),
        e.getMessage());
      // Spring Batch does not allow us to throw checked exceptions from this method,
      // so we wrap it in a RuntimeException
      throw new RuntimeException(e);
    }
  }
}
