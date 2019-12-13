package com.databasepreservation.common.client.common.helpers;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
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
  private TextBox zipFilenameTextBox;
  private RadioButton radioButtonExportAll;
  private CheckBox checkBoxExportDescription;
  private CheckBox checkBoxExportLOBs;
  private boolean isZipHelper;
  private final boolean singleRow;
  private String defaultZipFilename;

  public HelperExportTableData(ViewerTable table, boolean singleRow) {
    this.singleRow = singleRow;
    this.filenameTextBox = new TextBox();
    this.zipFilenameTextBox = new TextBox();
    this.defaultZipFilename = table.getSchemaName() + "_" + table.getName() + ViewerConstants.ZIP_EXTENSION;
    zipFilenameTextBox.setText(defaultZipFilename);
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

  public boolean exportLobs() { return checkBoxExportLOBs.getValue(); }

  public String getZipFileName() { return zipFilenameTextBox.getText(); }

  public FlowPanel getWidget(boolean buildZipHelper) {
    isZipHelper = buildZipHelper;
    if (buildZipHelper) {
      return buildZipExport();
    } else {
      return buildCSVExport();
    }
  }

  public boolean isZipHelper() {
    return this.isZipHelper;
  }

  private FlowPanel buildCSVExport() {
    FlowPanel panel = new FlowPanel();

    if (ViewerConstants.SERVER.equals(ApplicationType.getType())) {
      GenericField genericFieldFilename = GenericField.createInstance(messages.csvExportDialogLabelForFilename(),
        filenameTextBox);
      filenameTextBox.addStyleName("form-textbox");
      panel.add(wrapHelperText(genericFieldFilename, messages.csvExportDialogHelpTextForFilename()));
    }

    addCommonParts(panel);

    return panel;
  }

  private FlowPanel buildZipExport() {
    FlowPanel panel = new FlowPanel();

    GenericField genericFieldZipFilename = GenericField.createInstance(messages.csvExportDialogLabelForZipFilename(),
        zipFilenameTextBox);
    zipFilenameTextBox.addStyleName("form-textbox");

    GenericField genericFieldFilename = GenericField.createInstance(messages.csvExportDialogLabelForFilename(),
      filenameTextBox);
    filenameTextBox.addStyleName("form-textbox");
    panel.add(wrapHelperText(genericFieldFilename, messages.csvExportDialogHelpTextForFilename()));

    addCommonParts(panel);

    checkBoxExportLOBs = new CheckBox();
    checkBoxExportLOBs.setValue(true);
    checkBoxExportLOBs.setText(messages.csvExportDialogLabelForExportLOBs());
    checkBoxExportLOBs.addStyleName("form-checkbox");
    GenericField genericFieldExportLOBs = GenericField.createInstance(checkBoxExportLOBs);
    panel.add(wrapHelperText(genericFieldExportLOBs, messages.csvExportDialogHelpTextForLOBs()));
    checkBoxExportLOBs.addValueChangeHandler(event -> {
      if (!event.getValue()) {
        zipFilenameTextBox.setText(null);
        panel.remove(panel.getWidgetCount()-1);
      } else {
        zipFilenameTextBox.setText(defaultZipFilename);
        panel.add(wrapHelperText(genericFieldZipFilename, messages.csvExportDialogHelpTextForZipFilename()));
      }
    });

    panel.add(wrapHelperText(genericFieldZipFilename, messages.csvExportDialogHelpTextForZipFilename()));

    return panel;
  }

  private void addCommonParts(FlowPanel panel) {
    if (!singleRow) {
      radioButtonExportAll = new RadioButton("export-size");
      radioButtonExportAll.setText(messages.csvExportDialogLabelForExportAllRadioButton());
      RadioButton radioButtonExportVisible = new RadioButton("export-size");
      radioButtonExportVisible.setText(messages.csvExportDialogLabelForExportVisibleRadioButton());
      radioButtonExportVisible.setValue(true);
      FlowPanel radioButtonsPanel = new FlowPanel();
      radioButtonsPanel.add(radioButtonExportAll);
      radioButtonsPanel.add(radioButtonExportVisible);
      radioButtonsPanel.addStyleName("form-radio-buttons");
      GenericField genericFieldExportAll = GenericField.createInstance(messages.csvExportDialogLabelForExportRows(), radioButtonsPanel);
      panel.add(wrapHelperText(genericFieldExportAll, messages.csvExportDialogHelpTextForExportSize()));
    }
    checkBoxExportDescription = new CheckBox();
    checkBoxExportDescription.setText(messages.csvExportDialogLabelForExportHeaderWithDescriptions());
    checkBoxExportDescription.addStyleName("form-checkbox");
    GenericField genericFieldExportDescription = GenericField.createInstance(checkBoxExportDescription);

    panel.add(wrapHelperText(genericFieldExportDescription, messages.csvExportDialogHelpTextForDescription()));
    panel.addStyleName("content");
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
