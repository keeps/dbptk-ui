/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"advanced", "list", "facets"})
public class SearchStatus implements Serializable {

  private AdvancedStatus advanced;
  private ListStatus list;
  private FacetsStatus facets;

  public SearchStatus() { }

  public AdvancedStatus getAdvanced() {
    return advanced;
  }

  public void setAdvanced(AdvancedStatus advanced) {
    this.advanced = advanced;
  }

  public ListStatus getList() {
    return list;
  }

  public void setList(ListStatus list) {
    this.list = list;
  }

  public FacetsStatus getFacets() {
    return facets;
  }

  public void setFacets(FacetsStatus facets) {
    this.facets = facets;
  }

  @Override
  public String toString() {
    return "SearchStatus{" +
        "advanced=" + advanced +
        ", list=" + list.toString() +
        ", facets=" + facets +
        '}';
  }
}
