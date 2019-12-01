package com.databasepreservation.common.server.index.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.databasepreservation.common.client.index.sort.Sorter;
import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.utils.CloseableIterable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IterableIndexResult implements CloseableIterable<ViewerRow> {
  private static int PAGE_SIZE = -1;
  private static int RETRIES = -1;
  private static int SLEEP_BETWEEN_RETRIES = -1;

  private final IndexResultIterator iterator;

  public IterableIndexResult(final SolrClient solrClient, String databaseUUID, final Filter filter, final Sorter sorter,
    final List<String> fieldsToReturn) {
    iterator = new IndexResultIterator(solrClient, databaseUUID, filter, sorter, fieldsToReturn);

    if (PAGE_SIZE > 0) {
      iterator.setPageSize(PAGE_SIZE);
    }

    if (RETRIES > 0) {
      iterator.setRetries(RETRIES);
    }

    if (SLEEP_BETWEEN_RETRIES > 0) {
      iterator.setSleepBetweenRetries(SLEEP_BETWEEN_RETRIES);
    }
  }

  @Override
  public Iterator<ViewerRow> iterator() {
    return iterator;
  }

  @Override
  public void close() throws IOException {
    // do nothing
  }

  public static void injectSearchPageSize(int pageSize) {
    PAGE_SIZE = pageSize;
  }

  public static void injectNumberOfRetries(int retries) {
    RETRIES = retries;
  }

  public static void injectSleepBetweenRetries(int sleepTime) {
    SLEEP_BETWEEN_RETRIES = sleepTime;
  }

  /**
   * @see IndexResultIterator#getTotalCount()
   */
  public long getTotalCount() {
    return iterator.getTotalCount();
  }
}
