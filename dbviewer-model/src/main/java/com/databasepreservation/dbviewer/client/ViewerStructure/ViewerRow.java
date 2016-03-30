package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRow implements Serializable, IsIndexed {
  private String UUID;
  private List<ViewerCell> cells;

  public ViewerRow() {
    cells = new ArrayList<>();
  }

  public ViewerCell cellAt(int index) {
    if (index < cells.size()) {
      return cells.get(index);
    } else {
      return null;
    }
  }

  public List<ViewerCell> getCells() {
    return cells;
  }

  public void setCells(List<ViewerCell> cells) {
    this.cells = cells;
  }

  @Override
  public String getUUID() {
    return UUID;
  }

  public void setUUID(String UUID) {
    this.UUID = UUID;
  }
}
