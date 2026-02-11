package com.databasepreservation.common.server.batch.workflow.tasklets;

import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.batch.steps.common.JobProgressAggregator;
import com.databasepreservation.common.server.batch.steps.common.StepWorkloadEstimator;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class GlobalEstimationTasklet implements Tasklet {
  private final List<StepWorkloadEstimator> estimators;
  private final JobProgressAggregator aggregator;
  private final DatabaseRowsSolrManager solrManager;

  public GlobalEstimationTasklet(List<StepWorkloadEstimator> estimators, JobProgressAggregator aggregator,
    DatabaseRowsSolrManager solrManager) {
    this.estimators = estimators;
    this.aggregator = aggregator;
    this.solrManager = solrManager;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    String databaseUUID = (String) chunkContext.getStepContext().getJobParameters()
      .get(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);

    CollectionStatus status = (CollectionStatus) chunkContext.getStepContext().getStepExecution().getJobExecution()
      .getExecutionContext().get("COLLECTION_STATUS_CONFIG");

    long grandTotal = 0;

    for (StepWorkloadEstimator estimator : estimators) {
      grandTotal += estimator.estimate(databaseUUID, status);
    }

    aggregator.reset(grandTotal);

    String jobUUID = chunkContext.getStepContext().getStepExecution().getJobParameters()
      .getString(ViewerConstants.INDEX_ID);
    if (jobUUID != null) {
      solrManager.editBatchJob(jobUUID, grandTotal, 0);
    }

    return RepeatStatus.FINISHED;
  }

}
