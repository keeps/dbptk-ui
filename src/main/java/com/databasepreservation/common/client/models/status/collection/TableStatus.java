package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"uuid", "id", "name", "customName", "description", "customDescription", "hide", "columns"})
public class TableStatus implements Serializable {

  private String uuid;
  private String id;
  private String name;
  private String customName;
  private String description;
  private String customDescription;
  private boolean hide;
  private List<ColumnStatus> columns;

  public TableStatus() {
    columns = new ArrayList<>();
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
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

  public boolean isHide() {
    return hide;
  }

  public void setHide(boolean hide) {
    this.hide = hide;
  }

  public List<ColumnStatus> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnStatus> columns) {
    this.columns = columns;
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

  public void addColumnStatus(ColumnStatus status) {
    this.columns.add(status);
  }

  @JsonIgnore
  public int getLastColumnOrder(){
    int lastIndex = 0;
    for (ColumnStatus column : columns) {
     if(column.getOrder() > lastIndex) lastIndex = column.getOrder();
    }
    return lastIndex;
  }
}
