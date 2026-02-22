package com.databasepreservation.common.server.batch.components.readers;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.Pair;

/**
 * Generic ItemReader for Solr with built-in support for CursorMark-based
 * pagination and Spring Batch restartability.
 *
 * @param <T>
 *          The type of the item to be read. Must extend IsIndexed and
 *          Serializable.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrItemReader<T extends IsIndexed & Serializable> implements ItemStreamReader<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrItemReader.class);
  private static final int PAGE_SIZE = 100;
  private static final String CURSOR_KEY = "current.cursor.mark";

  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;
  private final Filter filter;
  private final List<String> fieldsToReturn;
  private final Class<T> targetClass;

  private String cursorMark = CursorMarkParams.CURSOR_MARK_START;
  private Iterator<T> currentBatchIterator;
  private boolean isFinished = false;

  public SolrItemReader(DatabaseRowsSolrManager solrClient, String databaseUUID, Filter filter,
    List<String> fieldsToReturn, Class<T> targetClass) {
    this.solrManager = solrClient;
    this.databaseUUID = databaseUUID;
    this.filter = filter;
    this.fieldsToReturn = fieldsToReturn;
    this.targetClass = targetClass;
  }

  /**
   * Reads the next item from the current batch or fetches a new batch from Solr.
   *
   * @return T next item or null if finished.
   * @throws Exception
   *           if an error occurs during reading.
   */
  @Override
  public T read() throws Exception {
    if (currentBatchIterator != null && currentBatchIterator.hasNext()) {
      return currentBatchIterator.next();
    }

    if (isFinished) {
      return null;
    }

    fetchNextBatch();
    return (currentBatchIterator != null && currentBatchIterator.hasNext()) ? currentBatchIterator.next() : null;
  }

  /**
   * Fetches the next page of results from Solr. Uses CursorMark for deep paging
   * if the target class is ViewerRow.
   */
  private void fetchNextBatch() throws Exception {
    IndexResult<T> result;
    String nextCursorMark = null;

    LOGGER.debug("Fetching batch for class {} with cursor: {}", targetClass.getSimpleName(), cursorMark);

    if (ViewerRow.class.isAssignableFrom(targetClass)) {
      Pair<IndexResult<ViewerRow>, String> page = solrManager.findRows(databaseUUID, filter, new Sorter(), PAGE_SIZE,
        cursorMark, fieldsToReturn, Collections.emptyMap());

      result = (IndexResult<T>) page.getFirst();
      nextCursorMark = page.getSecond();
    } else {
      result = solrManager.find(targetClass, filter, new Sorter(), null, null, fieldsToReturn);
      isFinished = true;
    }

    if (result == null || result.getResults().isEmpty() || cursorMark.equals(nextCursorMark)) {
      LOGGER.info("Reader for class {} reached the end of the results", targetClass.getSimpleName());
      isFinished = true;
      currentBatchIterator = Collections.emptyIterator();
      return;
    }

    this.currentBatchIterator = result.getResults().iterator();
    this.cursorMark = nextCursorMark;
  }

  /**
   * Restores the reader state from the execution context (Checkpointing).
   */
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (executionContext.containsKey(CURSOR_KEY)) {
      this.cursorMark = executionContext.getString(CURSOR_KEY);
      LOGGER.info("Restarting SolrItemReader<{}> from cursor checkpoint: {}", targetClass.getSimpleName(), cursorMark);
    } else {
      this.cursorMark = CursorMarkParams.CURSOR_MARK_START;
      LOGGER.debug("Starting new SolrItemReader<{}>", targetClass.getSimpleName());
    }
    this.isFinished = false;
  }

  /**
   * Saves the current cursorMark to the execution context.
   */
  @Override
  public void update(ExecutionContext executionContext) {
    if (!isFinished) {
      executionContext.putString(CURSOR_KEY, cursorMark);
    }
  }

  @Override
  public void close() {
    LOGGER.debug("Closing SolrItemReader<{}>", targetClass.getSimpleName());
  }
}
