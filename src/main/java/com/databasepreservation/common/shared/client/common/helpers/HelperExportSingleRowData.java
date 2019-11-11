package com.databasepreservation.common.shared.client.common.helpers;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.client.common.desktop.GenericField;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class HelperExportSingleRowData {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private TextBox filenameTextBox;
  private CheckBox checkBoxExportDescription;

  public HelperExportSingleRowData(ViewerTable table) {
    this.filenameTextBox = new TextBox();
    filenameTextBox.setText(table.getSchemaName() + "_" + table.getName() + "_single_row" + ViewerConstants.CSV_EXTENSION);
  }

  public String getFilename() {
    return filenameTextBox.getText();
  }

  public boolean exportDescription() {
    return checkBoxExportDescription.getValue();
  }

  public FlowPanel getWidget() {
    FlowPanel panel = new FlowPanel();

    if (ViewerConstants.SERVER.equals(ApplicationType.getType())) {
      GenericField genericFieldFilename = GenericField.createInstance(messages.csvExportDialogLabelForFilename(),
        filenameTextBox);
      filenameTextBox.addStyleName("form-textbox");
      panel.add(wrapHelperText(genericFieldFilename, messages.csvExportDialogHelpTextForFilename()));
    }

    checkBoxExportDescription = new CheckBox();
    checkBoxExportDescription.setText(messages.csvExportDialogLabelForExportHeaderWithDescriptions());
    checkBoxExportDescription.addStyleName("form-checkbox");
    GenericField genericFieldExportDescription = GenericField.createInstance(checkBoxExportDescription);

    panel.add(wrapHelperText(genericFieldExportDescription, messages.csvExportDialogHelpTextForDescription()));
    panel.addStyleName("content");

    return panel;
  }

  private FlowPanel wrapHelperText(GenericField genericField, String helpText) {
    FlowPanel helper = new FlowPanel();
    helper.addStyleName("form-helper");
    InlineHTML span = new InlineHTML();
    genericField.setCSSMetadata("form-row", "form-label-spaced");
    span.addStyleName("form-text-helper-checkbox text-muted");
    span.setText(helpText);
    genericField.addHelperText(span);
    helper.add(genericField);
    helper.add(span);

    return helper;
  }
}
