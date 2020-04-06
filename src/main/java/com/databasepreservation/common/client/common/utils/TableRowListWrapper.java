package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TableRowListWrapper {
  private ViewerDatabase database;
  private ViewerTable table;
  private ViewerCollectionConfiguration status;
  private Boolean isNested;

  public TableRowListWrapper(ViewerDatabase database, ViewerTable table, ViewerCollectionConfiguration status, Boolean isNested) {
    this.database = database;
    this.table = table;
    this.status = status;
    this.isNested = isNested;
  }

  public ViewerDatabase getDatabase() {
    return database;
  }

  public void setDatabase(ViewerDatabase database) {
    this.database = database;
  }

  public ViewerTable getTable() {
    return table;
  }

  public void setTable(ViewerTable table) {
    this.table = table;
  }

  public ViewerCollectionConfiguration getStatus() {
    return status;
  }

  public void setStatus(ViewerCollectionConfiguration status) {
    this.status = status;
  }

  public Boolean isNested() {
    return isNested;
  }

  public void setNested(Boolean nested) {
    isNested = nested;
  }
}
