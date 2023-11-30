package com.databasepreservation.common.server.index.utils;

import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.utils.CloseableIterable;
import org.apache.solr.client.solrj.SolrClient;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IterableDatabaseResult<T extends IsIndexed> implements CloseableIterable<T> {

  private static int PAGE_SIZE = -1;
  private static int RETRIES = -1;
  private static int SLEEP_BETWEEN_RETRIES = -1;

  private final DatabaseResultIterator<T> iterator;

  public IterableDatabaseResult(final SolrClient solrClient, final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final List<String> fieldsToReturn) {
    iterator = new DatabaseResultIterator<>(solrClient, returnClass, filter, sorter, fieldsToReturn);

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

  public static void injectSearchPageSize(int pageSize) {
    PAGE_SIZE = pageSize;
  }

  public static void injectNumberOfRetries(int retries) {
    RETRIES = retries;
  }

  public static void injectSleepBetweenRetries(int sleepTime) {
    SLEEP_BETWEEN_RETRIES = sleepTime;
  }

  @Override
  public Iterator<T> iterator() {
    return iterator;
  }

  @Override
  public void close() throws IOException {
    // do nothing
  }

  /**
   * @see IndexResultIterator#getTotalCount()
   */
  public long getTotalCount() {
    return iterator.getTotalCount();
  }

}
