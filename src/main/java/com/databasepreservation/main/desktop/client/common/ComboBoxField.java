package com.databasepreservation.main.desktop.client.common;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
  FlowPanel metadataField;

  @UiField
  Label metadataKey;

  @UiField
  ListBox metadataValue;

  public static ComboBoxField createInstance(String key) {
    return new ComboBoxField(key, null);
  }

  public static ComboBoxField createInstance(String key, List<String> values) {
    return new ComboBoxField(key, values);
  }

  public static ComboBoxField createInstance(List<String> values) {
    return new ComboBoxField(null, values);
  }

  private ComboBoxField(String key, List<String> values) {
    initWidget(binder.createAndBindUi(this));

    if (key != null) {
      metadataKey.setText(key);
    } else {
      metadataKey.setVisible(false);
    }

    if (values != null) {
      for (String s : values) {
        metadataValue.addItem(s);
      }
    }

  }

  public void setComboBoxValue(String value) {
    metadataValue.addItem(value);
  }

  public void setComboBoxValues(List<String> values) {
    for (String s : values) {
      metadataValue.addItem(s);
    }
  }

  public String getComboBoxValue() {
    return metadataValue.getSelectedValue();
  }

  public void setCSSMetadata(String cssParent, String cssKey, String cssValue) {

    if (cssParent != null) {
      metadataField.addStyleName(cssParent);
    }
    metadataKey.addStyleName(cssKey);
    metadataValue.addStyleName(cssValue);
  }
}
