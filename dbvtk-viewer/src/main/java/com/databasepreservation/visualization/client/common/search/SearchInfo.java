package com.databasepreservation.visualization.client.common.search;

import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;

/**
 * Simple class to hold search info to be serialized as Json
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchInfo {
  public Filter defaultFilter;
  public String currentFilter;
  public Map<String, Boolean> fieldVisibility;
  public List<SearchField> fields;
  public List<FilterParameter> fieldParameters;

  /**
   * Checks if the search info is not null and is valid
   * 
   * @param searchInfo
   *          the search info to test
   * @return true if the search info is not null and valid; false otherwise
   */
  public static boolean isPresentAndValid(SearchInfo searchInfo) {
    return searchInfo != null && searchInfo.currentFilter != null && searchInfo.defaultFilter != null
      && searchInfo.fields != null && searchInfo.fieldParameters != null && searchInfo.fieldVisibility != null
      && searchInfo.fields.size() == searchInfo.fieldParameters.size();
  }
}
