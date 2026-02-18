package com.databasepreservation.common.server.batchv2.infra;

import com.databasepreservation.common.server.batchv2.registry.JobProgressAggregatorV2;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ProgressListener implements ChunkListener {
  private final JobProgressAggregatorV2 progressAggregator;

  public ProgressListener(JobProgressAggregatorV2 progressAggregator) {
    this.progressAggregator = progressAggregator;
  }

  @Override
  public void afterChunk(ChunkContext context) {
    StepContext stepContext = context.getStepContext();
    StepExecution stepExecution = stepContext.getStepExecution();

    long readCount = stepExecution.getReadCount();

    String tableId = (String) stepContext.getJobParameters().get("tableId");

    progressAggregator.updateProgress(tableId, readCount);
  }

  @Override
  public void afterChunkError(ChunkContext context) {
  }
}
