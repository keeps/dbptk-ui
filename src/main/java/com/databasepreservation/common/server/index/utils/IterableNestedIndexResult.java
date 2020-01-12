package com.databasepreservation.common.server.index.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import com.databasepreservation.common.client.index.filter.Filter;

import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.utils.CloseableIterable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class IterableNestedIndexResult implements CloseableIterable<ViewerRow> {
  private static int PAGE_SIZE = -1;
  private static int RETRIES = -1;
  private static int SLEEP_BETWEEN_RETRIES = -1;

  private final NestedIndexResultIterator iterator;

  public IterableNestedIndexResult(final SolrClient solrClient, String databaseUUID, SolrQuery query, final Sorter sorter) {
    iterator = new NestedIndexResultIterator(solrClient, databaseUUID, query, sorter);

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
