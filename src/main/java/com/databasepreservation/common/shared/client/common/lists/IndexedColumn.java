package com.databasepreservation.common.shared.client.common.lists;

import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;

/**
 * Column used for dynamic table
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IndexedColumn extends Column<List<String>, String> {
  private final int index;

  public IndexedColumn(int index) {
    super(new TextCell());
    this.index = index;
  }

  @Override
  public String getValue(List<String> object) {
    return object.get(index);
  }

  public int getIndex() {
    return this.index;
  }
}
