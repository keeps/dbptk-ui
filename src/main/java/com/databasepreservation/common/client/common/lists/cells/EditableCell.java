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

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class EditableCell extends TextInputCell {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);


  private static Template template;

  interface Template extends SafeHtmlTemplates
  {
    // {0}, {1}, {2} relate to value, placeholder, style
    @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\" placeholder=\"{1}\" class=\"gwt-TextBox form-textbox\"></input>")
    SafeHtml input(String value, String placeholder);
  }

  public EditableCell() {
    template = GWT.create(Template.class);
  }



  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {

    if(value == null || value.isEmpty() ){
      sb.append(template.input("", messages.metadataDoesNotContainDescription()));
    } else {
      sb.append(template.input(value, messages.metadataDoesNotContainDescription()));
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
