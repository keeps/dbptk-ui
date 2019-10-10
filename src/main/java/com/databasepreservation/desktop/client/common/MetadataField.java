package com.databasepreservation.desktop.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MetadataField extends Composite {

  interface MetadataFieldUiBinder extends UiBinder<Widget, MetadataField> {
  }

  private static MetadataFieldUiBinder binder = GWT.create(MetadataFieldUiBinder.class);

  @UiField
  FlowPanel metadataField;

  @UiField
  Label metadataKey, metadataValue;

  public static MetadataField createInstance(String key, String value) {
    return new MetadataField(key, value);
  }

  public static MetadataField createInstance(String value) {
    return new MetadataField(null, value);
  }

  private MetadataField(String key, String value) {
    initWidget(binder.createAndBindUi(this));

    if (key != null) {
      metadataKey.setText(key);
    } else {
      metadataKey.setVisible(false);
    }

    metadataValue.setText(value);
  }

  public void setCSSMetadata(String cssParent, String cssKey, String cssValue) {

    if (cssParent != null) {
      metadataField.addStyleName(cssParent);
    }
    metadataKey.addStyleName(cssKey);
    metadataValue.addStyleName(cssValue);
  }

  public Label getMetadataValue(){
    return metadataValue;
  }

  public void updateText(String text) {
    metadataValue.setText(text);
  }
}