/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.CursorMarkParams;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DatabaseResultIterator<T extends IsIndexed> implements Iterator<T> {

  public static final int DEFAULT_PAGE_SIZE = 1000;
  public static final int DEFAULT_RETRIES = 100;
  public static final int DEFAULT_SLEEP_BETWEEN_RETRIES = 10000;
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResultIterator.class);
  private final SolrClient index;
  private final Class<T> classToRetrieve;
  private final Filter filter;
  private final Sorter sorter;
  private final List<String> fieldsToReturn;
  private final List<Filter> filterQueries;
  private int pageSize = DEFAULT_PAGE_SIZE;
  private int retries = DEFAULT_RETRIES;
  private int sleepBetweenRetries = DEFAULT_SLEEP_BETWEEN_RETRIES;
  private IndexResult<T> result = null;
  private int indexInResult = 0;
  private String cursorMark = CursorMarkParams.CURSOR_MARK_START;
  private String nextCursorMark = CursorMarkParams.CURSOR_MARK_START;
  private T next = null;

  public DatabaseResultIterator(SolrClient index, Class<T> classToRetrieve, Filter filter, Sorter sorter,
    List<String> fieldsToReturn, List<Filter> filterQueries) {
    this.index = index;
    this.filter = filter;
    this.filterQueries = filterQueries;
    this.classToRetrieve = classToRetrieve;
    this.sorter = sorter;
    this.fieldsToReturn = fieldsToReturn;

    getCurrentAndPrepareNext();
  }

  private T getCurrentAndPrepareNext() {
    T current = next;

    // ensure index result is renewed
    if (result == null || result.getResults().size() == indexInResult) {
      indexInResult = 0;

      cursorMark = nextCursorMark;
      result = null;
      nextCursorMark = null;
      int availableRetries = retries;

      do {
        try {
          Pair<IndexResult<T>, String> page = SolrUtils.find(index, classToRetrieve, filter, sorter, pageSize,
            cursorMark, fieldsToReturn, new HashMap<>(), filterQueries);
          result = page.getFirst();
          nextCursorMark = page.getSecond();

        } catch (GenericException | RequestNotValidException e) {
          if (availableRetries > 0) {
            availableRetries--;
            LOGGER.warn("Error getting next page from Solr, retrying in {}ms...", sleepBetweenRetries);
            try {
              Thread.sleep(sleepBetweenRetries);
            } catch (InterruptedException e1) {
              // do nothing
            }
          } else {
            LOGGER.error("Error getting next page from Solr, no more retries.", e);
            throw new NoSuchElementException("Error getting next item in list: " + e.getMessage());
          }
        }
      } while (result == null);
    }

    if (indexInResult < result.getResults().size()) {
      this.next = result.getResults().get(indexInResult++);
    } else {
      this.next = null;
    }

    return current;
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public T next() {
    return getCurrentAndPrepareNext();
  }

  /**
   * @param pageSize
   *          the pageSize to set
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * @return the retries
   */
  public int getRetries() {
    return retries;
  }

  /**
   * @param retries
   *          the retries to set
   */
  public void setRetries(int retries) {
    this.retries = retries;
  }

  /**
   * @return the sleepBetweenRetries
   */
  public int getSleepBetweenRetries() {
    return sleepBetweenRetries;
  }

  /**
   * @param sleepBetweenRetries
   *          the sleepBetweenRetries to set
   */
  public void setSleepBetweenRetries(int sleepBetweenRetries) {
    this.sleepBetweenRetries = sleepBetweenRetries;
  }

  /**
   * Gets the total count of objects as reported by underlying Solr requests.
   *
   * @return
   */
  public long getTotalCount() {
    return result != null ? result.getTotalCount() : -1;
  }
}
