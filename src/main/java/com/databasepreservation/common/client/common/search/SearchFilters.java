package com.databasepreservation.common.client.common.search;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SearchFilters {

  public static Filter allFilter() {
    return new Filter(new BasicSearchFilterParameter(searchField(), ViewerConstants.INDEX_WILDCARD));
  }

  public static String searchField() {
    return ViewerConstants.INDEX_SEARCH;
  }

  public static boolean shouldBeIncremental(final Filter filter) {
    return !filter.getParameters().isEmpty() && !SearchFilters.allFilter().equals(filter);
  }
}
