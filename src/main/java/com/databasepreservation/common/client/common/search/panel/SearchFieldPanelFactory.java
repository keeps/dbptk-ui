package com.databasepreservation.common.client.common.search.panel;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SearchField;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SearchFieldPanelFactory {

  public static SearchFieldPanel getSearchFieldPanel(SearchField searchField) {
    switch (searchField.getType()) {
      case ViewerConstants.SEARCH_FIELD_TYPE_DATE:
        return new DateSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_TIME:
        return new TimeSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_DATETIME:
        return new DateTimeSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC:
        return new NumericSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL:
        return new NumericIntervalSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_STORAGE:
        return new StorageSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_BOOLEAN:
        return new BooleanSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_NESTED:
        return new NestedSearchFieldPanel(searchField);
      case ViewerConstants.SEARCH_FIELD_TYPE_SUGGEST:
      default:
        return new TextSearchFieldPanel(searchField);
    }
  }
}
