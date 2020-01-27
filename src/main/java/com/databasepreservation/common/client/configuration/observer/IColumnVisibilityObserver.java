package com.databasepreservation.common.client.configuration.observer;

import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface IColumnVisibilityObserver {
  void updateColumnVisibility(String tableId, Map<String, Boolean> columns);
}
