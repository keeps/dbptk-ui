/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.utils.AdvancedSearchUtils;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.LongRangeFilterParameter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.ViewerJsonUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 * Simple class to hold search info to be serialized as Json
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchInfo implements Serializable {
  private Filter defaultFilter;
  private String currentFilter;
  private List<SearchField> fields;
  private Map<String, FilterParameter> fieldParameters;

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
  public SearchInfo(CollectionStatus status, ViewerTable viewerTable, Map<String, String> solrColumnAndValue, ViewerMetadata metadata) {
    defaultFilter = ViewerConstants.DEFAULT_FILTER;
    currentFilter = "";

    fields = AdvancedSearchUtils.getSearchFieldsFromTable(viewerTable, status, metadata);

    fieldParameters = new HashMap<>();
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
      fieldParameters.put(field.getId(), fieldParameter);
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

  public Map<String, FilterParameter> getFieldParameters() {
    return fieldParameters;
  }

  public void setFieldParameters(Map<String, FilterParameter> fieldParameters) {
    this.fieldParameters = fieldParameters;
  }

  public List<SearchField> getFields() {
    return fields;
  }

  public void setFields(List<SearchField> fields) {
    this.fields = fields;
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
      && searchInfo.fields != null && searchInfo.fieldParameters != null;
  }
}
