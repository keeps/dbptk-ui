package com.databasepreservation.common.client.common.lists.columns;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class TooltipColumn<T> extends Column<T, SafeHtml> {
  public TooltipColumn() {
    super(new SafeHtmlCell());
  }

  @Override
  public void render(Cell.Context context, T object, SafeHtmlBuilder sb) {
    SafeHtml value = getValue(object);
    if (value != null) {
      sb.appendHtmlConstant("<div title=\"" + SafeHtmlUtils.htmlEscape(value.asString()) + "\">");
      sb.append(value);
      sb.appendHtmlConstant("</div");
    }
  }
}
