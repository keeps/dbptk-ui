package com.databasepreservation.main.common.shared.client.common.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;

import com.databasepreservation.main.common.shared.client.common.utils.BrowserServiceUtils;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.tools.ViewerJsonUtils;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 * Simple class to hold search info to be serialized as Json
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchInfo implements Serializable {
  private Filter defaultFilter;
  private String currentFilter;
  private Map<String, Boolean> fieldVisibility;
  private List<SearchField> fields;
  private List<FilterParameter> fieldParameters;

  /**
   * Empty instance
   */
  public SearchInfo() {
  }

  /**
   * Instance to show foreign key related records
   * 
   * @param viewerTable
   *          the foreign table
   * @param solrColumnAndValue
   *          map of solr column names to column values, to be used as advanced
   *          search fields
   */
  public SearchInfo(ViewerTable viewerTable, Map<String, String> solrColumnAndValue) {
    defaultFilter = ViewerConstants.DEFAULT_FILTER;
    currentFilter = "";
    fieldVisibility = new HashMap<>();

    fields = BrowserServiceUtils.getSearchFieldsFromTable(viewerTable);

    fieldParameters = new ArrayList<>();
    for (SearchField field : fields) {
      String solrColumnName = field.getSearchFields().get(0);
      FilterParameter fieldParameter = null;

      String value = solrColumnAndValue.get(solrColumnName);
      if (ViewerStringUtils.isNotBlank(value)) {
        // try to handle different types in different ways
        if (field.getType().equals(ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL)) {
          fieldParameter = new LongRangeFilterParameter(solrColumnName, Long.valueOf(value), Long.valueOf(value));
        } else if (field.getType().equals(ViewerConstants.SEARCH_FIELD_TYPE_DATETIME)) {
          // TODO: handle DATETIME keys
        } else if (field.getType().equals(ViewerConstants.SEARCH_FIELD_TYPE_DATE)) {
          // TODO: handle DATE keys
        } else if (field.getType().equals(ViewerConstants.SEARCH_FIELD_TYPE_TIME)) {
          // TODO: handle TIME keys
        } else if (field.getType().equals(ViewerConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)) {
          // TODO: handle DATE INTERVAL keys
        } else if (field.getType().equals(ViewerConstants.SEARCH_FIELD_TYPE_BOOLEAN)) {
          // TODO: handle BOOLEAN keys
        }

        // default: set is as a BasicSearchFilterParameter
        if (fieldParameter == null) {
          fieldParameter = new BasicSearchFilterParameter(solrColumnName, solrColumnAndValue.get(solrColumnName));
        }
      }
      fieldParameters.add(fieldParameter);
    }
  }

  public String asJson() {
    return ViewerJsonUtils.getSearchInfoMapper().write(this);
  }

  public String asUrlEncodedJson() {
    return UriUtils.encode(asJson());
  }

  public String getCurrentFilter() {
    return currentFilter;
  }

  public void setCurrentFilter(String currentFilter) {
    this.currentFilter = currentFilter;
  }

  public Filter getDefaultFilter() {
    return defaultFilter;
  }

  public void setDefaultFilter(Filter defaultFilter) {
    this.defaultFilter = defaultFilter;
  }

  public List<FilterParameter> getFieldParameters() {
    return fieldParameters;
  }

  public void setFieldParameters(List<FilterParameter> fieldParameters) {
    this.fieldParameters = fieldParameters;
  }

  public List<SearchField> getFields() {
    return fields;
  }

  public void setFields(List<SearchField> fields) {
    this.fields = fields;
  }

  public Map<String, Boolean> getFieldVisibility() {
    return fieldVisibility;
  }

  public void setFieldVisibility(Map<String, Boolean> fieldVisibility) {
    this.fieldVisibility = fieldVisibility;
  }

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
