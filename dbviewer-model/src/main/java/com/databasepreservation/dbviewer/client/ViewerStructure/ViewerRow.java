package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRow implements Serializable, IsIndexed {
  private String UUID;
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

  public Map<String, ViewerCell> getCells() {
    return cells;
  }

  public void setCells(Map<String, ViewerCell> cells) {
    this.cells = cells;
  }
}
