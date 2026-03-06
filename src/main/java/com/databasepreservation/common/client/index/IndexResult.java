/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.facets.FacetFieldResult;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonSubTypes({@JsonSubTypes.Type(value= ViewerDatabase.class, name="ViewerDatabase"), @JsonSubTypes.Type(SavedSearch.class), @JsonSubTypes.Type(ViewerJob.class)})
public class IndexResult<T extends Serializable> implements Serializable {

  private long offset;
  private long limit;
  private long totalCount;

  private List<T> results;
  private List<FacetFieldResult> facetResults;
  private Date date;
  private Map<String, Map<String, List<String>>> highlightingInfo;

  public IndexResult() {
    super();
    this.results = new ArrayList<>();
    this.facetResults = new ArrayList<>();
    date = new Date();
    this.highlightingInfo = new HashMap<>();
  }

  public IndexResult(long offset, long limit, long totalCount, List<T> results, List<FacetFieldResult> facetResults,
    Map<String, Map<String, List<String>>> highlightingInfo) {
    super();
    this.offset = offset;
    this.limit = limit;
    this.totalCount = totalCount;
    this.results = results;
    this.facetResults = facetResults;
    date = new Date();
    this.highlightingInfo = highlightingInfo;
  }

  /**
   * @return the offset
   */
  public long getOffset() {
    return offset;
  }

  /**
   * @return the limit
   */
  public long getLimit() {
    return limit;
  }

  /**
   * @return the totalCount
   */
  public long getTotalCount() {
    return totalCount;
  }

  /**
   * @return the results
   */
  public List<T> getResults() {
    return results;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public void setLimit(long limit) {
    this.limit = limit;
  }

  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }

  public List<FacetFieldResult> getFacetResults() {
    return facetResults;
  }

  public void setFacetResults(List<FacetFieldResult> facetResults) {
    this.facetResults = facetResults;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Map<String, Map<String, List<String>>> getHighlightingInfo() {
    return highlightingInfo;
  }

  public void setHighlightingInfo(Map<String, Map<String, List<String>>> highlightingInfo) {
    this.highlightingInfo = highlightingInfo;
  }

  @Override
  public String toString() {
    return "IndexResult [offset=" + offset + ", limit=" + limit + ", totalCount=" + totalCount + ", results=" + results
      + ", facetResults=" + facetResults + ", date=" + date + ", highlightingInfo=" + highlightingInfo + "]";
  }
}
