package com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.FileUploadField;
import com.databasepreservation.main.desktop.client.common.GenericField;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExportOptionsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDExportOptionsCurrent extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDExportOptionsCurrentUiBinder extends UiBinder<Widget, SIARDExportOptionsCurrent> {
  }

  private static SIARDExportOptionsCurrentUiBinder binder = GWT.create(SIARDExportOptionsCurrentUiBinder.class);

  @UiField
  FlowPanel content;

  private static HashMap<String, SIARDExportOptionsCurrent> instances = new HashMap<>();
  private HashMap<String, TextBox> textBoxInputs = new HashMap<>();
  private HashMap<String, CheckBox> checkBoxInputs = new HashMap<>();
  private HashMap<String, String> fileInputs = new HashMap<>();
  private DBPTKModule dbptkModule;
  private ArrayList<PreservationParameter> parameters;
  private ArrayList<Label> externalLobsLabels = new ArrayList<>();
  private HashMap<String, TextBox> externalLobsInputs = new HashMap<>();
  private CheckBox externalLobCheckbox;
  private String version;

  public static SIARDExportOptionsCurrent getInstance(String key, DBPTKModule dbptkModule) {
    if (instances.get(key) == null) {
      instances.put(key, new SIARDExportOptionsCurrent(key, dbptkModule));
    }
    return instances.get(key);
  }

  private SIARDExportOptionsCurrent(String version, DBPTKModule dbptkModule) {
    initWidget(binder.createAndBindUi(this));

    this.version = version;
    this.parameters = dbptkModule.getParameters(version);
    this.dbptkModule = dbptkModule;

    for (PreservationParameter p : parameters) {
      if (p.getExportOption() != null) {
        if (p.getExportOption().equals(ViewerConstants.SIARD_EXPORT_OPTIONS)) {
          buildGenericWidget(p);
        } else if (p.getExportOption().equals(ViewerConstants.EXTERNAL_LOBS_EXPORT_OPTIONS)) {
            buildExternalLobs(p);
        }
      }
    }
  }

  public ExportOptionsParameters getValues() {
    ExportOptionsParameters exportOptionsParameters = new ExportOptionsParameters();

    HashMap<String, String> exportParameters = new HashMap<>();

    for (PreservationParameter parameter : parameters) {
      switch (parameter.getInputType()) {
        case "CHECKBOX":
          if (checkBoxInputs.get(parameter.getName()) != null) {
            final boolean value = checkBoxInputs.get(parameter.getName()).getValue();
            exportParameters.put(parameter.getName(), String.valueOf(value));
          }
          break;
        case "TEXT":
          if (textBoxInputs.get(parameter.getName()) != null) {
            final String text = textBoxInputs.get(parameter.getName()).getText();
            exportParameters.put(parameter.getName(), text);
          }
          if (externalLobCheckbox.getValue()) {
            if (externalLobsInputs.get(parameter.getName()) != null) {
              final String text = externalLobsInputs.get(parameter.getName()).getText();
              exportParameters.put(parameter.getName(), text);
            }
          }
          break;
        case "FILE":
          if (fileInputs.get(parameter.getName()) != null) {
            final String path = fileInputs.get(parameter.getName());
            exportOptionsParameters.setSiardPath(path);
            exportParameters.put(parameter.getName(), path);
          }
          break;
        case "DEFAULT":
          if (parameter.getName().equals("pretty-xml")) {
            exportParameters.put(parameter.getName(), "true");
          }
        case "NONE":
        default:
          break;
      }
    }

    exportOptionsParameters.setSIARDVersion(version);
    exportOptionsParameters.setParameters(exportParameters);

    return exportOptionsParameters;
  }

  public boolean validate() {
    if (!validateExternalLobs())
      return false;

    final ArrayList<PreservationParameter> requiredParameters = dbptkModule.getRequiredParameters();

    for (PreservationParameter parameter : requiredParameters) {
      switch (parameter.getInputType()) {
        case "TEXT":
          if (textBoxInputs.get(parameter.getName()) != null) {
            final TextBox textBox = textBoxInputs.get(parameter.getName());
            if (ViewerStringUtils.isBlank(textBox.getText())) {
              return false;
            } else {
              return false;
            }
          }
          break;
        case "FILE":
          if (fileInputs.get(parameter.getName()) != null) {
            final String s = fileInputs.get(parameter.getName());
            if (ViewerStringUtils.isBlank(s)) {
              return false;
            }
          } else {
            return false;
          }
          break;
        default:
          ;
      }
    }
    return true;
  }

  public void clear() {
    instances.clear();
  }

  public void error() {
    Toast.showError("MISSING FIELDS");
  }

  private boolean validateExternalLobs() {
    if (externalLobCheckbox.getValue()) {
      for (TextBox textBox : externalLobsInputs.values()) {
        final String text = textBox.getText();
        if (ViewerStringUtils.isBlank(text)) {
          return false;
        }
      }
    }
    return true;
  }

  private void updateCheckboxExternalLobs(boolean value) {
    if (value) { // selected
      for (Label label : externalLobsLabels) {
        label.addStyleName("gwt-Label");
        label.removeStyleName("gwt-Label-disabled");
      }

      for (TextBox textBox : externalLobsInputs.values()) {
        textBox.setEnabled(true);
      }
    } else {
      for (Label label : externalLobsLabels) {
        label.removeStyleName("gwt-Label");
        label.addStyleName("gwt-Label-disabled");
      }

      for (TextBox textBox : externalLobsInputs.values()) {
        textBox.setEnabled(false);
      }
    }
  }

  private void buildExternalLobs(PreservationParameter parameter) {
    GenericField genericField;

    switch (parameter.getInputType()) {
      case "CHECKBOX":
        externalLobCheckbox = new CheckBox();
        externalLobCheckbox.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        externalLobCheckbox.addStyleName("form-checkbox");
        externalLobCheckbox.addValueChangeHandler(event -> {
          updateCheckboxExternalLobs(event.getValue());
        });
        genericField = GenericField.createInstance(externalLobCheckbox);
        genericField.setRequired(parameter.isRequired());
        genericField.setCSSMetadata("form-row", "form-label-spaced");
        content.add(genericField);
        break;
      case "TEXT":
        Label label = new Label();
        label.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        externalLobsLabels.add(label);
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox-external-lobs");
        externalLobsInputs.put(parameter.getName(), defaultTextBox);
        Label label_end = new Label();
        label_end.setText(messages.wizardExportOptionsLabels(parameter.getName() + "-end"));
        externalLobsLabels.add(label_end);
        if (version.equals("siard-dk")) {
          label.addStyleName("form-label");
          label_end.addStyleName("form-label");
        } else {
          label.addStyleName("form-label gwt-Label-disabled");
          label_end.addStyleName("form-label gwt-Label-disabled");
          defaultTextBox.setEnabled(false);
        }
        FlowPanel formHelper = new FlowPanel();
        formHelper.addStyleName("form-helper");
        FlowPanel formRow = new FlowPanel();
        formRow.addStyleName("form-row");
        formRow.add(label);
        formRow.add(defaultTextBox);
        formRow.add(label_end);
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");
        span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));
        formHelper.add(formRow);
        formHelper.add(span);
        content.add(formHelper);
        break;
      default:
        break;
    }
  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;

    switch (parameter.getInputType()) {
      case "CHECKBOX":
        CheckBox checkbox = new CheckBox();
        checkbox.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        checkbox.addStyleName("form-checkbox");
        checkBoxInputs.put(parameter.getName(), checkbox);
        genericField = GenericField.createInstance(checkbox);
        break;
      case "FILE":
        FileUploadField fileUploadField = FileUploadField
          .createInstance(messages.wizardExportOptionsLabels(parameter.getName()), messages.siardExportBrowseButton());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
            String path = JavascriptUtils.saveFileDialog();
            if (path != null) {
              fileInputs.put(parameter.getName(), path);
              fileUploadField.setPathLocation(path, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          } else {
            fileInputs.put(parameter.getName(), "/home/mguimaraes/Desktop/test.siard");
          }
        });
        FlowPanel helper = new FlowPanel();
        helper.addStyleName("form-helper");
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");
        span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));

        helper.add(fileUploadField);
        helper.add(span);
        content.add(helper);
        break;
      case "FOLDER":
        FileUploadField folder = FileUploadField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()),
          messages.siardExportBrowseButton());
        folder.setParentCSS("form-row");
        folder.setLabelCSS("form-label-spaced");
        folder.setButtonCSS("btn btn-link form-button");
        folder.setRequired(parameter.isRequired());
        folder.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openDirectory"),
              Collections.emptyList());
            String path = JavascriptUtils.openFileDialog(options);
            if (path != null) {
              fileInputs.put(parameter.getName(), path);
              folder.setPathLocation(path, path);
              folder.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });
        content.add(folder);
        break;
      case "COMBOBOX":
      case "NONE":
        genericField = null;
        break;
      case "NUMBER":
      case "TEXT":
      default:
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        genericField = GenericField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()), defaultTextBox);
        break;
    }

    if (genericField != null) {
      FlowPanel helper = new FlowPanel();
      helper.addStyleName("form-helper");
      InlineHTML span = new InlineHTML();
      span.addStyleName("form-text-helper text-muted");
      span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));

      genericField.setRequired(parameter.isRequired());
      genericField.setCSSMetadata("form-row", "form-label-spaced");
      genericField.addHelperText(span);
      helper.add(genericField);
      helper.add(span);
      content.add(helper);
    }
  }
}