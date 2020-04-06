package com.databasepreservation.common.client.models.configuration.collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"advanced", "list", "facets"})
public class ViewerSearchConfiguration implements Serializable {

  private ViewerAdvancedConfiguration advanced;
  private ViewerListConfiguration list;
  private ViewerFacetsConfiguration facets;

  public ViewerSearchConfiguration() { }

  public ViewerAdvancedConfiguration getAdvanced() {
    return advanced;
  }

  public void setAdvanced(ViewerAdvancedConfiguration advanced) {
    this.advanced = advanced;
  }

  public ViewerListConfiguration getList() {
    return list;
  }

  public void setList(ViewerListConfiguration list) {
    this.list = list;
  }

  public ViewerFacetsConfiguration getFacets() {
    return facets;
  }

  public void setFacets(ViewerFacetsConfiguration facets) {
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
