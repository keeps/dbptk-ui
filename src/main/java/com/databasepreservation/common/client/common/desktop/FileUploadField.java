package com.databasepreservation.common.client.common.desktop;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FileUploadField extends Composite {
  interface GenericFieldUiBinder extends UiBinder<Widget, FileUploadField> {
  }

  private static GenericFieldUiBinder binder = GWT.create(GenericFieldUiBinder.class);

  @UiField
  FlowPanel genericField;

  @UiField
  Label label, path;

  @UiField
  Button upload;

  public static FileUploadField createInstance(String label, String buttonText) {
    return new FileUploadField(label, buttonText);
  }

  private FileUploadField(String labelText, String buttonText) {
    initWidget(binder.createAndBindUi(this));

    if (labelText != null) {
      label.setText(labelText);
    } else {
      label.setVisible(false);
    }

    upload.setText(buttonText);
    path.setVisible(false);
  }

  public void setRequired(boolean required) {
    if (required)
      label.addStyleName("form-label-mandatory");
  }

  public void setParentCSS(String css) {
    genericField.addStyleName(css);
  }

  public void setLabelCSS(String css) {
    label.addStyleName(css);
  }

  public void setButtonCSS(String css) {
    upload.addStyleName(css);
  }

  public void setInformationPathCSS(String css) {
    path.addStyleName(css);
  }

  public void addHelperText(Widget span) {
    genericField.add(span);
  }

  public void setPathLocation(String toDisplay, String tooltip) {
    path.setTitle(tooltip);
    path.setText(toDisplay);
    path.setVisible(true);
  }

  public String getPathLocation() {
    return path.getTitle();
  }

  public void buttonAction(Command cmd) {
    upload.addClickHandler(event -> {
      cmd.execute();
    });
  }
}