package com.databasepreservation.common.shared.client.common.fields;

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
  Label metadataLabel;

  @UiField
  Label metadataValue;

  public static MetadataField createInstance(String label, String value) {
    return new MetadataField(label, value);
  }

  public static MetadataField createInstance(String value) {
    return new MetadataField(null, value);
  }

  private MetadataField(String label, String value) {
    initWidget(binder.createAndBindUi(this));

    if (label != null) {
      metadataLabel.setText(label);
    } else {
      metadataLabel.setVisible(false);
    }

    metadataValue.setText(value);
  }

  public void setCSS(String styleName) {
    metadataField.addStyleName(styleName);
  }

  public void setCSSMetadata(String cssParent, String cssLabel, String cssValue) {

    if (cssParent != null) {
      metadataField.addStyleName(cssParent);
    }
    metadataLabel.addStyleName(cssLabel);
    metadataValue.addStyleName(cssValue);
  }

  public Label getMetadataValue(){
    return metadataValue;
  }

  public void updateText(String text) {
    metadataValue.setText(text);
  }
}