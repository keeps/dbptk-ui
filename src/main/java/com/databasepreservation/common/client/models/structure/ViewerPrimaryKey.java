/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerPrimaryKey implements Serializable {
  @Serial
  private static final long serialVersionUID = -8910003658243755024L;
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
