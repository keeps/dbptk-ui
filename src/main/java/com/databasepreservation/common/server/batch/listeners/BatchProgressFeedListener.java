package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.JobProgressAggregator;
import com.databasepreservation.common.server.controller.JobController;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BatchProgressFeedListener implements ChunkListener, StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(BatchProgressFeedListener.class);
  private final JobContext jobContext;

  public BatchProgressFeedListener(JobContext jobContext) {
    this.jobContext = jobContext;
  }

  @Override
  public void afterChunk(ChunkContext chunkContext) {
    StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();

    JobProgressAggregator aggregator = jobContext.getJobProgressAggregator();

    long globalProcessed = aggregator.addProgress(stepExecution.getId(), stepExecution.getReadCount());
    long globalTotal = aggregator.getTotal();

    long stepSkips = stepExecution.getReadSkipCount() + stepExecution.getProcessSkipCount()
      + stepExecution.getWriteSkipCount();
    long globalSkips = aggregator.addSkips(stepExecution.getId(), stepSkips);

    if (globalProcessed > 0) {
      LOGGER.debug("[PROGRESS] [{}] {}/{} items processed | {} skips", stepExecution.getStepName(), globalProcessed,
        globalTotal, globalSkips);
    }

    try {
      JobController.syncJobStateToSolr(stepExecution.getJobExecution());
    } catch (Exception e) {
      LOGGER.warn("[PROGRESS] Failed to save job progress to Solr for job execution id: {}. Error: {}",
        stepExecution.getJobExecution().getId(), e.getMessage());
    }
  }

  @Override
  public void afterChunkError(ChunkContext chunkContext) {
    LOGGER.error("[PROGRESS] ERROR: Chunk processing failed in step: {}", chunkContext.getStepContext().getStepName());
  }
}
