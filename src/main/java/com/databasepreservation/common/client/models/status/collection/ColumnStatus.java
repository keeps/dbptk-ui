package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

import com.databasepreservation.common.client.models.structure.ViewerType;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"id", "name", "customName", "description", "customDescription", "originalType", "typeName",
  "nullable", "type", "columnIndex", "nestedColumn", "order", "export", "search", "details"})
public class ColumnStatus implements Serializable, Comparable<ColumnStatus> {
  private String id;
  private String name;
  private String customName;
  private String description;
  private String customDescription;
  private String originalType;
  private String typeName;
  private String nullable;
  private ViewerType.dbTypes type;
  private int columnIndex;
  private NestedColumnStatus nestedColumns;
  private int order;
  private ExportStatus exportStatus;
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

  public String getOriginalType() {
    return originalType;
  }

  public void setOriginalType(String originalType) {
    this.originalType = originalType;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getNullable() {
    return nullable;
  }

  public void setNullable(String nullable) {
    this.nullable = nullable;
  }

  @JsonProperty("type")
	public ViewerType.dbTypes getType() {
		return type;
	}

	public void setType(ViewerType.dbTypes type) {
		this.type = type;
	}

  public int getColumnIndex() {
    return columnIndex;
  }

  public void setColumnIndex(int columnIndex) {
    this.columnIndex = columnIndex;
  }

  public NestedColumnStatus getNestedColumns() {
    return nestedColumns;
  }

  public void setNestedColumns(NestedColumnStatus nestedColumns) {
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

  @JsonProperty("export")
  public ExportStatus getExportStatus() {
    return exportStatus;
  }

  public void setExportStatus(ExportStatus exportStatus) {
    this.exportStatus = exportStatus;
  }

  public void setDetailsStatus(DetailsStatus detailsStatus) {
    this.detailsStatus = detailsStatus;
  }

  public void updateTableShowValue(boolean value) {
    this.getSearchStatus().getList().setShow(value);
  }

  public void updateDetailsShowValue(boolean value) {
    this.getDetailsStatus().setShow(value);
  }

  public void updateAdvancedSearchShowValue(boolean value) {
    this.getSearchStatus().getAdvanced().setFixed(value);
  }

  @Override
  public String toString() {
    return "ColumnStatus{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", customName='" + customName + '\''
      + ", description='" + description + '\'' + ", customDescription='" + customDescription + '\'' + ", originalType='"
      + originalType + '\'' + ", typeName='" + typeName + '\'' + ", nullable=" + nullable + ", nestedColumns="
      + nestedColumns + ", order=" + order + ", searchStatus=" + searchStatus + ", detailsStatus=" + detailsStatus
      + '}';
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int compareTo(@NotNull ColumnStatus o) {
    return (this.getOrder() - o.getOrder());
  }
}
