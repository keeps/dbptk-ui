package com.databasepreservation.common.client.models.denormalize;

import java.io.Serializable;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ColumnsToIncludeConfiguration implements Serializable {
  private String columnName;
  private String displayFormat;
  private Long order;
  private String tableUUID;
  private ForeignKeyConfiguration foreignKey;

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public String getDisplayFormat() {
    return displayFormat;
  }

  public void setDisplayFormat(String displayFormat) {
    this.displayFormat = displayFormat;
  }

  public Long getOrder() {
    return order;
  }

  public void setOrder(Long order) {
    this.order = order;
  }

  public String getTableUUID() {
    return tableUUID;
  }

  public void setTableUUID(String tableUUID) {
    this.tableUUID = tableUUID;
  }

  public ForeignKeyConfiguration getForeignKey() {
    return foreignKey;
  }

  public void setForeignKey(ForeignKeyConfiguration foreignKey) {
    this.foreignKey = foreignKey;
  }
}
