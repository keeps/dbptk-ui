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

public class TextAreaInputCell extends TextInputCell {
	private static final ClientMessages messages = GWT.create(ClientMessages.class);
	private static Template template;
  private String rows;
	interface Template extends SafeHtmlTemplates {
    @Template("<textarea type=\"text\" cols=\"30\" rows=\"{0}\" class=\"form-textbox form-textarea\" tabindex=\"-1\">{1}</textarea>")
    SafeHtml input(String rows, String value);
	}

  public TextAreaInputCell(String rows) {
    this.rows = rows;
    template = GWT.create(Template.class);
  }

	public TextAreaInputCell() {
    rows = "1";
		template = GWT.create(Template.class);
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {

		if(value == null || value.isEmpty() ){
      sb.append(template.input(rows, ""));
		} else {
      sb.append(template.input(rows, value));
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
