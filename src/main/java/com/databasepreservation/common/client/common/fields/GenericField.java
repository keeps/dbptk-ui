package com.databasepreservation.common.client.common.fields;

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
public class GenericField extends Composite {
  interface GenericFieldUiBinder extends UiBinder<Widget, GenericField> {
  }

  private static GenericFieldUiBinder binder = GWT.create(GenericFieldUiBinder.class);

  private String type;

  @UiField
  FlowPanel genericField;

  @UiField
  Label genericKey;

  public static GenericField createInstance(String key, Widget value) {
    return new GenericField(key, value);
  }

  public static GenericField createInstance(Widget value) {
    return new GenericField(null, value);
  }

  private GenericField(String key, Widget value) {
    initWidget(binder.createAndBindUi(this));

    if (key != null) {
      genericKey.setText(key);
    } else {
      genericKey.setVisible(false);
    }

    type = value.getClass().getSimpleName();

    genericField.add(value);
  }

  public void setRequired(boolean required) {
    if (required) genericKey.addStyleName("form-label-mandatory");
  }

  public void setCSSMetadata(String cssParent, String cssKey) {

    if (cssParent != null) {
      genericField.addStyleName(cssParent);
    }
    genericKey.addStyleName(cssKey);
  }

  public void addHelperText(Widget span) {
    genericField.add(span);
  }

  public void setLabelVisible(boolean value) {
    genericKey.setVisible(value);
  }

  public String getGenericFieldType() {
    return this.type;
  }
}