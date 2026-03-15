package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.JobProgressAggregator;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrProgressFeedListener implements ChunkListener, StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrProgressFeedListener.class);
  private final DatabaseRowsSolrManager solrManager;
  private final JobContext jobContext;

  public SolrProgressFeedListener(DatabaseRowsSolrManager solrManager, JobContext jobContext) {
    this.solrManager = solrManager;
    this.jobContext = jobContext;
  }

  @Override
  public void afterChunk(ChunkContext chunkContext) {
    StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
    String jobUUID = stepExecution.getJobParameters().getString(BatchConstants.JOB_UUID_KEY);

    JobProgressAggregator aggregator = jobContext.getJobProgressAggregator();

    long globalProcessed = aggregator.addProgress(stepExecution.getId(), stepExecution.getReadCount());
    long globalTotal = aggregator.getTotal();

    long skipCount = stepExecution.getReadSkipCount() + stepExecution.getProcessSkipCount()
      + stepExecution.getWriteSkipCount();

    if (jobUUID != null && globalProcessed > 0) {
      try {
        LOGGER.debug("[PROGRESS] [{}] {}/{} items processed", stepExecution.getStepName(), globalProcessed,
          globalTotal);
        solrManager.editBatchJob(jobUUID, globalTotal, globalProcessed, skipCount, jobContext);
      } catch (Exception e) {
        LOGGER.warn("[PROGRESS] WARNING: Failed to update Solr progress for job {}", jobUUID, e);
      }
    }
  }

  @Override
  public void afterChunkError(ChunkContext chunkContext) {
    LOGGER.error("[PROGRESS] ERROR: Chunk processing failed in step: {}", chunkContext.getStepContext().getStepName());
  }
}
