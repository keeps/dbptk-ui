package com.databasepreservation.common.client.common.lists.utils;

import com.databasepreservation.common.client.common.search.SearchFilters;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.filter.Filter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AsyncTableCellOptions<T extends IsIndexed> {
  private final Class<T> classToReturn;
  private final String listId;
  private Filter filter;

  public AsyncTableCellOptions(Class<T> classToReturn, String listId) {
    this.classToReturn = classToReturn;
    this.listId = listId;
    filter = SearchFilters.allFilter();
  }

  public Class<T> getClassToReturn() {
    return classToReturn;
  }

  public String getListId() {
    return listId;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }
}
