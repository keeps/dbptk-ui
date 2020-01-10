package com.databasepreservation.common.client.common.lists.cells;

import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RequiredEditableCell extends EditableCell {
  private static Template template;
  private String placeholder;

  interface Template extends SafeHtmlTemplates {
    // {0}, {1} relate to value, placeholder
    @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\" placeholder=\"{1}\" class=\"gwt-TextBox form-textbox\" required></input>")
    SafeHtml input(String value, String placeholder);
  }

  public RequiredEditableCell() {
    RequiredEditableCell.template = GWT.create(Template.class);
    this.placeholder = "";
  }

  public RequiredEditableCell(String placeholder) {
    RequiredEditableCell.template = GWT.create(Template.class);
    this.placeholder = placeholder;
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (value == null || value.isEmpty()) {
      sb.append(template.input("", placeholder));
    } else {
      sb.append(template.input(value, placeholder));
    }
  }
}
