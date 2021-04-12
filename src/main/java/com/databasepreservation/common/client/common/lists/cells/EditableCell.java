/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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
public class EditableCell extends TextInputCell {
  private static Template template;
  private String placeholder;

  interface Template extends SafeHtmlTemplates {
    // {0}, {1} relate to value, placeholder
    @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\" placeholder=\"{1}\" class=\"gwt-TextBox form-textbox\"></input>")
    SafeHtml input(String value, String placeholder);
  }

  public EditableCell() {
    EditableCell.template = GWT.create(Template.class);
    this.placeholder = "";
  }

  public EditableCell(String placeholder) {
    EditableCell.template = GWT.create(Template.class);
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

  @Override
  public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
    ValueUpdater<String> valueUpdater) {
    if (value == null) {
      value = "";
    }

    InputElement input = getInputElement(parent);
    Element target = event.getEventTarget().cast();
    if (!input.isOrHasChild(target)) {
      return;
    }

    String eventType = event.getType();
    if (BrowserEvents.FOCUS.equals(eventType)) {
      input.select();
    }

    super.onBrowserEvent(context, parent, value, event, valueUpdater);
  }
}
