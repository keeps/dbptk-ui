/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.fields;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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

  private HTML metadataValue = new HTML();

  public static MetadataField createInstance(String label, String value) {
    return new MetadataField(label, value);
  }

  public static MetadataField createInstance(String value) {
    return new MetadataField(null, value);
  }

  public static MetadataField createInstance(String label, SafeHtml safeHtml) {
    return new MetadataField(label, safeHtml);
  }

  private MetadataField(String label, SafeHtml safeHtml) {
    init(label);

    metadataValue.setHTML(safeHtml);
    metadataField.add(metadataValue);
  }

  private MetadataField(String label, String value) {
    init(label);

    metadataValue.setText(value);
    metadataField.add(metadataValue);
  }

  private void init(String label) {
    initWidget(binder.createAndBindUi(this));

    if (label != null) {
      metadataLabel.setText(label);
    } else {
      metadataLabel.setVisible(false);
    }
  }

  public void setCSS(String styleName) {
    metadataField.addStyleName(styleName);
  }

  public void setCSS(String cssParent, String cssValue) {
    if (cssParent != null) {
      metadataField.addStyleName(cssParent);
    }
    metadataValue.addStyleName(cssValue);
  }

  public void setCSS(String cssParent, String cssLabel, String cssValue) {

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

  public void updateText(SafeHtml html) {
    metadataValue.setHTML(html);
  }
}