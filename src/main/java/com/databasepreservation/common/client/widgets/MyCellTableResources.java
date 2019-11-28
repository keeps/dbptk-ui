/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.widgets;

import com.google.gwt.user.cellview.client.CellTable;

public interface MyCellTableResources extends CellTable.Resources {
  /**
   * The styles applied to the table.
   */
  interface TableStyle extends CellTable.Style {
  }

  @Override
  @Source({CellTable.Style.DEFAULT_CSS, "MyCellTable.css"})
  TableStyle cellTableStyle();

}
