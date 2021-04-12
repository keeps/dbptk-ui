/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists.columns;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class ButtonColumn<T> extends Column<T, String> {
  public ButtonColumn() {
    super(new ButtonCell());
  }

  @Override
  public void render(Cell.Context context, T object, SafeHtmlBuilder sb) {
    String value = getValue(object);
    sb.appendHtmlConstant("<button class=\"btn btn-link-info\" type=\"button\" tabindex=\"-1\">");
    if (value != null) {
      sb.append(SafeHtmlUtils.fromString(value));
    }
    sb.appendHtmlConstant("</button>");
  }
}
