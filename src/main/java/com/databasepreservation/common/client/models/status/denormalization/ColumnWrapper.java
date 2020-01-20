package com.databasepreservation.common.client.models.status.denormalization;

import com.databasepreservation.common.client.models.structure.ViewerColumn;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ColumnWrapper {
  String uuid;
  String referencedTableName;
  ViewerColumn column;

  public ColumnWrapper(String referencedTableName, ViewerColumn column) {
    this(null, referencedTableName, column);
  }

  public ColumnWrapper(String uuid, String referencedTableName, ViewerColumn column) {
    this.uuid = uuid;
    this.referencedTableName = referencedTableName;
    this.column = column;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getReferencedTableName() {
    return referencedTableName;
  }

  public void setReferencedTableName(String referencedTableName) {
    this.referencedTableName = referencedTableName;
  }

  public ViewerColumn getColumn() {
    return column;
  }

  public void setColumn(ViewerColumn column) {
    this.column = column;
  }
}
