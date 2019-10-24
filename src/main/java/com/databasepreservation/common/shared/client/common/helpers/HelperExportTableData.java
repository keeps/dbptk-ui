package com.databasepreservation.common.shared.client.common.helpers;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.client.common.desktop.GenericField;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class HelperExportTableData {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private TextBox filenameTextBox;
  private RadioButton radioButtonExportAll;
  private CheckBox checkBoxExportDescription;

  public HelperExportTableData(ViewerTable table) {
    this.filenameTextBox = new TextBox();
    filenameTextBox.setText(table.getSchemaName() + "_" + table.getName() + ViewerConstants.CSV_EXTENSION);
  }

  public String getFilename() {
    return filenameTextBox.getText();
  }

  public boolean exportAll() {
    return radioButtonExportAll.getValue();
  }

  public boolean exportDescription() {
    return checkBoxExportDescription.getValue();
  }

  public FlowPanel getWidget() {
    FlowPanel panel = new FlowPanel();
    GenericField genericFieldFilename = GenericField.createInstance(messages.csvExportDialogLabelForFilename(),
      filenameTextBox);
    filenameTextBox.addStyleName("form-textbox");
    radioButtonExportAll = new RadioButton("export-size");
    radioButtonExportAll.setText(messages.csvExportDialogLabelForExportAllRadioButton());
    RadioButton radioButtonExportVisible = new RadioButton("export-size");
    radioButtonExportVisible.setText(messages.csvExportDialogLabelForExportVisibleRadioButton());
    radioButtonExportVisible.setValue(true);
    FlowPanel radioButtonsPanel = new FlowPanel();
    radioButtonsPanel.add(radioButtonExportAll);
    radioButtonsPanel.add(radioButtonExportVisible);
    GenericField genericFieldExportAll = GenericField.createInstance("Export Rows:", radioButtonsPanel);
    checkBoxExportDescription = new CheckBox();
    checkBoxExportDescription.setText(messages.csvExportDialogLabelForExportHeaderWithDescriptions());
    checkBoxExportDescription.addStyleName("form-checkbox");
    GenericField genericFieldExportDescription = GenericField.createInstance(checkBoxExportDescription);

    panel.add(wrapHelperText(genericFieldFilename, messages.csvExportDialogHelpTextForFilename()));
    panel.add(wrapHelperText(genericFieldExportAll, messages.csvExportDialogHelpTextForExportSize()));
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
