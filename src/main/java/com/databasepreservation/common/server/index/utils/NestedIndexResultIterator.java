/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.utils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CursorMarkParams;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import com.databasepreservation.common.client.index.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerRow;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class NestedIndexResultIterator implements Iterator<ViewerRow> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NestedIndexResultIterator.class);

  public static final int DEFAULT_PAGE_SIZE = 1000;
  public static final int DEFAULT_RETRIES = 100;
  public static final int DEFAULT_SLEEP_BETWEEN_RETRIES = 10000;

  private int pageSize = DEFAULT_PAGE_SIZE;
  private int retries = DEFAULT_RETRIES;
  private int sleepBetweenRetries = DEFAULT_SLEEP_BETWEEN_RETRIES;

  private IndexResult<ViewerRow> result = null;
  private int indexInResult = 0;
  private String cursorMark = CursorMarkParams.CURSOR_MARK_START;
  private String nextCursorMark = CursorMarkParams.CURSOR_MARK_START;

  private final SolrClient index;
  private final String databaseUUID;
  private final SolrQuery query;
  private final Sorter sorter;

  private ViewerRow next = null;

  public NestedIndexResultIterator(SolrClient index, String databaseUUID, SolrQuery query, Sorter sorter) {
    this.index = index;
    this.databaseUUID = databaseUUID;
    this.query = query;
    this.sorter = sorter;

    getCurrentAndPrepareNext();
  }

  private ViewerRow getCurrentAndPrepareNext() {
    ViewerRow current = next;

    // ensure index result is renewed
    if (result == null || result.getResults().size() == indexInResult) {
      indexInResult = 0;

      cursorMark = nextCursorMark;
      result = null;
      nextCursorMark = null;
      int availableRetries = retries;

      do {
        try {
          Pair<IndexResult<ViewerRow>, String> page = SolrUtils.findRows(index, databaseUUID, query, sorter, pageSize,
            cursorMark);
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
  public ViewerRow next() {
    return getCurrentAndPrepareNext();
  }

  /**
   * @return the pageSize
   */
  public int getPageSize() {
    return pageSize;
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
