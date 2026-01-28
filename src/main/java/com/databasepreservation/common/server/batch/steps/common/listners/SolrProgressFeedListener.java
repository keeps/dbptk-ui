package com.databasepreservation.common.server.batch.steps.common.listners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.server.batch.steps.common.JobProgressAggregator;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrProgressFeedListener implements ChunkListener, StepExecutionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrProgressFeedListener.class);

  private final DatabaseRowsSolrManager solrManager;
  private final JobProgressAggregator aggregator;
  private String jobUUID;
  private long lastReadCount = 0;
  private long startTime;

  public SolrProgressFeedListener(DatabaseRowsSolrManager solrManager, JobProgressAggregator aggregator) {
    this.solrManager = solrManager;
    this.aggregator = aggregator;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    this.jobUUID = stepExecution.getJobParameters().getString(ViewerConstants.INDEX_ID);
    this.lastReadCount = 0;
  }

  @Override
  public void beforeChunk(ChunkContext context) {
    this.startTime = System.currentTimeMillis();
  }

  @Override
  public void afterChunk(ChunkContext context) {
    long chunkTime = System.currentTimeMillis() - this.startTime;

    long currentReadCount = context.getStepContext().getStepExecution().getReadCount();
    long delta = currentReadCount - lastReadCount;
    this.lastReadCount = currentReadCount;

    long globalProcessed = aggregator.addProgress(delta);
    long globalTotal = aggregator.getTotal();

    if (jobUUID != null) {
      try {
        LOGGER.info("Chunk processed: {} items in {} ms. Total progress: {}/{}", delta, chunkTime, globalProcessed,
          globalTotal);
        solrManager.editBatchJob(jobUUID, globalTotal, globalProcessed);
      } catch (Exception e) {
        LOGGER.warn("Failed to update Solr progress", e);
      }
    }
  }

  @Override
  public void afterChunkError(ChunkContext context) {
    LOGGER.error("Error occurred during chunk processing");
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    return null;
  }
}
