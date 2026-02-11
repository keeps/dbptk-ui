package com.databasepreservation.common.server.batch.workflow;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.batch.steps.common.StepExecutionPolicy;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PolicyBasedDecider implements JobExecutionDecider {

  private final StepExecutionPolicy strategy;

  public PolicyBasedDecider(StepExecutionPolicy strategy) {
    this.strategy = strategy;
  }

  @Override
  public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
    String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    CollectionStatus status = (CollectionStatus) jobExecution.getExecutionContext().get("COLLECTION_STATUS_CONFIG");

    if (strategy.shouldExecute(databaseUUID, status)) {
      return new FlowExecutionStatus("CONTINUE");
    } else {
      return new FlowExecutionStatus("SKIP");
    }

  }
}
