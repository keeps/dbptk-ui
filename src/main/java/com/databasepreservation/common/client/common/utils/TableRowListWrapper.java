package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TableRowListWrapper {
  private ViewerDatabase database;
  private ViewerTable table;
  private CollectionStatus status;

  public TableRowListWrapper(ViewerDatabase database, ViewerTable table, CollectionStatus status) {
    this.database = database;
    this.table = table;
    this.status = status;
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

  public CollectionStatus getStatus() {
    return status;
  }

  public void setStatus(CollectionStatus status) {
    this.status = status;
  }
}
