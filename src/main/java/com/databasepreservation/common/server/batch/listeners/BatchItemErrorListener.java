package com.databasepreservation.common.server.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.item.Chunk;

import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.server.batch.core.BatchErrorExtractor;
import com.databasepreservation.common.server.controller.JobController;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BatchItemErrorListener<T, O> extends ItemListenerSupport<T, O> implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(BatchItemErrorListener.class);
  private String stepName;
  private JobExecution jobExecution;

  public BatchItemErrorListener() {
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
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

    if (jobExecution != null) {
      JobController.appendErrorToContext(jobExecution, formattedMessage);

      String cleanError = BatchErrorExtractor.extractMeaningfulError(ex);
      jobExecution.getExecutionContext().put("ERR_" + cleanError.hashCode(), true);
    }
  }
}
