/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.configuration.observer;

import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface IColumnVisibilityObserver {
  void updateColumnVisibility(String tableId, Map<String, Boolean> columns);
}
