package com.databasepreservation.visualization.client.common.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.shared.BrowserServiceUtils;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.shared.client.Tools.ViewerJsonUtils;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 * Simple class to hold search info to be serialized as Json
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchInfo implements Serializable{
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
    defaultFilter = ViewerSafeConstants.DEFAULT_FILTER;
    currentFilter = "";
    fieldVisibility = new HashMap<>();

    fields = BrowserServiceUtils.getSearchFieldsFromTable(viewerTable);

    fieldParameters = new ArrayList<>();
    for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
      String solrColumnName = viewerColumn.getSolrName();
      if (solrColumnAndValue.containsKey(solrColumnName)) {
        fieldParameters.add(new BasicSearchFilterParameter(solrColumnName, solrColumnAndValue.get(solrColumnName)));
      } else {
        fieldParameters.add(null);
      }
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
