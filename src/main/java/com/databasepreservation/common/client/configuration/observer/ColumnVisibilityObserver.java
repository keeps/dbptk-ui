package com.databasepreservation.common.client.configuration.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ColumnVisibilityObserver {
  private List<IColumnVisibilityObserver> observerList = new ArrayList<>();

  public void addObserver(IColumnVisibilityObserver observer) {
    if (!this.observerList.contains(observer)) {
      this.observerList.add(observer);
    }
  }

  public void removeObserver(IColumnVisibilityObserver observer) {
    this.observerList.remove(observer);
  }

  public void setCollectionStatus(String tableId, Map<String, Boolean> map) {
    for (IColumnVisibilityObserver observer : this.observerList) {
      observer.updateColumnVisibility(tableId, map);
    }
  }
}
