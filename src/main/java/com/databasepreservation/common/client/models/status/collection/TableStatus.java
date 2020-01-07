package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableStatus implements Serializable {

  private String uuid;
  private String id;
  private String name;
  private String description;
  private boolean hide;
  private List<ColumnStatus> columns;

  public TableStatus() {
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
}
