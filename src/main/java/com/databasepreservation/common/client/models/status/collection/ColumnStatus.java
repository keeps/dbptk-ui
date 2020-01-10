package com.databasepreservation.common.client.models.status.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"id", "name", "customName", "description", "customDescription", "nestedColumn", "order", "search", "details"})
public class ColumnStatus implements Serializable {

  private String id;
  private String name;
  private String customName;
  private String description;
  private String customDescription;
  private List<String> nestedColumns = new ArrayList<>();
  private int order;
  private SearchStatus searchStatus;
  private DetailsStatus detailsStatus;

  public ColumnStatus() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getNestedColumns() {
    return nestedColumns;
  }

  public void setNestedColumns(List<String> nestedColumns) {
    this.nestedColumns = nestedColumns;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public String getCustomName() {
    return customName;
  }

  public void setCustomName(String customName) {
    this.customName = customName;
  }

  public String getCustomDescription() {
    return customDescription;
  }

  public void setCustomDescription(String customDescription) {
    this.customDescription = customDescription;
  }

  @JsonProperty("search")
  public SearchStatus getSearchStatus() {
    return searchStatus;
  }

  public void setSearchStatus(SearchStatus searchStatus) {
    this.searchStatus = searchStatus;
  }

  @JsonProperty("details")
  public DetailsStatus getDetailsStatus() {
    return detailsStatus;
  }

  public void setDetailsStatus(DetailsStatus detailsStatus) {
    this.detailsStatus = detailsStatus;
  }

  public void updateHidingAttribute(boolean value) {
    this.getSearchStatus().getList().setHide(value);
  }

  @Override
  public String toString() {
    return "ColumnStatus{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", customName='" + customName + '\'' +
        ", description='" + description + '\'' +
        ", customDescription='" + customDescription + '\'' +
        ", nestedColumn=" + nestedColumns.toString() +
        ", order=" + order +
        ", searchStatus=" + searchStatus.toString() +
        ", detailsStatus=" + detailsStatus +
        '}';
  }
}
