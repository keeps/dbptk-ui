package com.databasepreservation.common.shared.ViewerStructure;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerPrimaryKey implements Serializable {
  private String name;

  private List<Integer> columnIndexesInViewerTable;

  private String description;

  public ViewerPrimaryKey() {
  }

  public List<Integer> getColumnIndexesInViewerTable() {
    return columnIndexesInViewerTable;
  }

  public void setColumnIndexesInViewerTable(List<Integer> columnIndexesInViewerTable) {
    this.columnIndexesInViewerTable = columnIndexesInViewerTable;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
