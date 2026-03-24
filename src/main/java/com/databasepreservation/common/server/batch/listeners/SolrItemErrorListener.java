package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.item.Chunk;

import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.BatchErrorExtractor;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrItemErrorListener<T, O> extends ItemListenerSupport<T, O> implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrItemErrorListener.class);
  private final DatabaseRowsSolrManager solrManager;
  private String jobUUID;
  private String stepName;
  private JobExecution jobExecution;

  public SolrItemErrorListener(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    this.jobUUID = stepExecution.getJobParameters().getString(BatchConstants.JOB_UUID_KEY);
    this.stepName = stepExecution.getStepName();
    this.jobExecution = stepExecution.getJobExecution();
  }

  @Override
  public void onReadError(Exception ex) {
    String cleanError = BatchErrorExtractor.extractMeaningfulError(ex);
    logAndAppendError(String.format("Read Error [%s]: %s", stepName, cleanError), ex);
  }

  @Override
  public void onProcessError(T item, Exception ex) {
    String identifier = (item instanceof IsIndexed) ? ((IsIndexed) item).getUuid() : "UnknownItem";
    String cleanError = BatchErrorExtractor.extractMeaningfulError(ex);
    logAndAppendError(String.format("Processing Error [%s] on item UUID '%s': %s", stepName, identifier, cleanError),
      ex);
  }

  @Override
  public void onWriteError(Exception ex, Chunk<? extends O> items) {
    String cleanError = BatchErrorExtractor.extractMeaningfulError(ex);
    logAndAppendError(String.format("Write Error [%s] on chunk (size %d): %s", stepName, items.size(), cleanError), ex);
  }

  private void logAndAppendError(String formattedMessage, Exception ex) {
    LOGGER.error("[ITEM ERROR] {}", formattedMessage, ex);
    if (jobUUID != null) {
      solrManager.appendBatchJobError(jobUUID, formattedMessage);
      if (jobExecution != null) {
        String cleanError = BatchErrorExtractor.extractMeaningfulError(ex);
        jobExecution.getExecutionContext().put("ERR_" + cleanError.hashCode(), true);
      }
    }
  }
}
