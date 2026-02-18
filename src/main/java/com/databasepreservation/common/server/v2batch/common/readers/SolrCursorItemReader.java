package com.databasepreservation.common.server.v2batch.common.readers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.Pair;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrCursorItemReader implements ItemReader<ViewerRow>, ItemStream {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrCursorItemReader.class);

  private static final int PAGE_SIZE = 10;
  private static final int CHUNK_SIZE = PAGE_SIZE;

  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;
  private final Filter filter;
  private final List<String> fieldsToReturn;

  private String cursorMark = CursorMarkParams.CURSOR_MARK_START;
  private String nextCursorMark;

  private Iterator<ViewerRow> currentBatchIterator;
  private boolean isFinished = false;

  public SolrCursorItemReader(DatabaseRowsSolrManager solrClient, String databaseUUID, Filter filter,
    List<String> fieldsToReturn) {
    this.solrManager = solrClient;
    this.databaseUUID = databaseUUID;
    this.filter = filter;
    this.fieldsToReturn = fieldsToReturn;
  }

  public static int getChunkSize() {
    return CHUNK_SIZE;
  }

  @Override
  public synchronized ViewerRow read() throws Exception {
    if (currentBatchIterator != null && currentBatchIterator.hasNext()) {
      return currentBatchIterator.next();
    }

    if (isFinished) {
      return null;
    }

    fetchNextBatch();

    if (currentBatchIterator != null && currentBatchIterator.hasNext()) {
      return currentBatchIterator.next();
    } else {
      return null;
    }
  }

  private void fetchNextBatch() throws Exception {
    LOGGER.debug("[Reader] Fetching batch from Solr. Cursor: {}", cursorMark);
    Pair<IndexResult<ViewerRow>, String> page = solrManager.findRows(databaseUUID, filter, new Sorter(), PAGE_SIZE,
      cursorMark, fieldsToReturn, Collections.emptyMap());

    IndexResult<ViewerRow> result = page.getFirst();
    this.nextCursorMark = page.getSecond();

    List<ViewerRow> rows = result.getResults();


    LOGGER.info("[Reader@{} - ThreadId: {}] Querying Solr for filter: {}",
            Integer.toHexString(System.identityHashCode(this)), // ID único do objeto na memória
            Thread.currentThread().getName(),
            this.filter);


    if (rows == null || rows.isEmpty() || cursorMark.equals(nextCursorMark)) {
      LOGGER.info("[Reader] No more rows to fetch. Ending reader execution.");
      isFinished = true;
      currentBatchIterator = Collections.emptyIterator();
      return;
    }

    LOGGER.debug("[Reader] Fetched {} rows in this batch.", rows.size());
    this.currentBatchIterator = rows.iterator();
    this.cursorMark = this.nextCursorMark;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    this.cursorMark = CursorMarkParams.CURSOR_MARK_START;
    this.isFinished = false;
    this.currentBatchIterator = null;

    LOGGER.info("[Reader] Opening reader for database {} with filter: {}", databaseUUID, filter);
  }

  @Override
  public void update(ExecutionContext executionContext) {

  }

  @Override
  public void close() {

  }

}
