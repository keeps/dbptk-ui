package com.databasepreservation.common.client.models.configuration.collection;

import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.common.collect.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TableConfiguration implements Serializable {
  private String uuid;
  private String id;
  private String name;
  private String description;
  private Boolean hide;
  private List<ColumnConfiguration> column;
  private String relatedTables;

  public TableConfiguration(ViewerTable table){
    setUuid(table.getUuid());
    setId(table.getId());
    setName(table.getName());
    setDescription(table.getDescription());
    setHide(false);
    setColumn(new ArrayList<>());
  }

  public TableConfiguration() {
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

  public Boolean getHide() {
    return hide;
  }

  public void setHide(Boolean hide) {
    this.hide = hide;
  }

  public List<ColumnConfiguration> getColumn() {
    return column;
  }

  public void setColumn(List<ColumnConfiguration> column) {
    this.column = column;
  }

  public String getRelatedTables() {
    return relatedTables;
  }

  public void setRelatedTables(String relatedTables) {
    this.relatedTables = relatedTables;
  }
}
