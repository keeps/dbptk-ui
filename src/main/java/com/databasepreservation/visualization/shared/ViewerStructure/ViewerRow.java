package com.databasepreservation.visualization.shared.ViewerStructure;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRow extends IsIndexed {
  private String UUID;
  private String tableId;
  private Map<String, ViewerCell> cells;

  public ViewerRow() {
    cells = new HashMap<>();
  }

  @Override
  public String getUUID() {
    return UUID;
  }

  public void setUUID(String UUID) {
    this.UUID = UUID;
  }

  /**
   * @return Map of solrColumnName to cell value as string
   */
  public Map<String, ViewerCell> getCells() {
    return cells;
  }

  /**
   * @param cells
   *          Map of solrColumnName to value as String
   */
  public void setCells(Map<String, ViewerCell> cells) {
    this.cells = cells;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }
}
