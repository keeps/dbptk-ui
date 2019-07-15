package com.databasepreservation.main.desktop.client.common;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ComboBoxField extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ComboBoxFieldUiBinder extends UiBinder<Widget, ComboBoxField> {
  }

  private static ComboBoxFieldUiBinder binder = GWT.create(ComboBoxFieldUiBinder.class);

  @UiField
  FlowPanel flowPanelParent;

  @UiField
  Label comboboxLabel;

  @UiField
  ListBox combobox;

  public static ComboBoxField createInstance(String key) {
    return new ComboBoxField(key, null);
  }

  private ComboBoxField(String key, List<String> values) {
    initWidget(binder.createAndBindUi(this));

    if (key != null) {
      comboboxLabel.setText(key);
    } else {
      comboboxLabel.setVisible(false);
    }

    if (values != null) {
      for (String s : values) {
        combobox.addItem(s);
      }
    }
  }

  public void addChangeHandler(Command command) {
    combobox.addChangeHandler(event -> {
      command.execute();
    });
  }

  public void setComboBoxValue(String item, String value) {
    combobox.addItem(item, value);
  }

  public String getComboBoxValue() {
    return combobox.getSelectedValue();
  }

  public void select(int index) {
    combobox.setSelectedIndex(index);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), combobox);
  }

  public void setCSSMetadata(String cssParent, String cssKey, String cssValue) {

    if (cssParent != null) {
      flowPanelParent.addStyleName(cssParent);
    }
    comboboxLabel.addStyleName(cssKey);
    combobox.addStyleName(cssValue);
  }
}
