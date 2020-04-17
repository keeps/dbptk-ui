package com.databasepreservation.desktop.client.dbptk.wizard.common.exportOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.fields.ComboBoxField;
import com.databasepreservation.common.client.common.fields.FileUploadField;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.models.JSO.ExtensionFilter;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.parameters.PreservationParameter;
import com.databasepreservation.common.client.models.wizard.export.ExportOptionsParameters;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Toast;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.regexp.shared.RegExp;
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

  private static final Map<String, SIARDExportOptionsCurrent> instances = new HashMap<>();
  private final Map<String, TextBox> textBoxInputs = new HashMap<>();
  private final Map<String, CheckBox> checkBoxInputs = new HashMap<>();
  private final Map<String, ComboBoxField> comboBoxInputs = new HashMap<>();
  private final Map<String, String> fileInputs = new HashMap<>();
  private final Module module;
  private final List<Label> externalLobsLabels = new ArrayList<>();
  private final Map<String, TextBox> externalLobsInputs = new HashMap<>();
  private CheckBox externalLobCheckbox;
  private int validationError = -1;
  private final String version;
  private final String defaultPath;

  public static SIARDExportOptionsCurrent getInstance(String key, List<Module> modules) {
    instances.computeIfAbsent(key, k -> new SIARDExportOptionsCurrent(key, modules, null));
    return instances.get(key);
  }

  public static SIARDExportOptionsCurrent getInstance(String key, List<Module> modules, String defaultPath) {
    instances.computeIfAbsent(key, k -> new SIARDExportOptionsCurrent(key, modules, defaultPath));
    return instances.get(key);
  }

  private SIARDExportOptionsCurrent(String version, List<Module> modules, String defaultPath) {
    initWidget(binder.createAndBindUi(this));

    this.version = version;
    this.module = modules.stream().filter(c -> c.getModuleName().equals(version)).findFirst().orElse(new Module());
    this.defaultPath = defaultPath;

    FlowPanel panel = new FlowPanel();

    for (PreservationParameter p : module.getParameters()) {
      if (p.getExportOption() != null) {
        if (p.getExportOption().equals(ViewerConstants.SIARD_EXPORT_OPTIONS)) {
          buildGenericWidget(p);
        } else if (p.getExportOption().equals(ViewerConstants.EXTERNAL_LOBS_EXPORT_OPTIONS)) {
          buildExternalLobs(p, panel);
        }
      }
    }
  }

  public ExportOptionsParameters getValues() {
    ExportOptionsParameters exportOptionsParameters = new ExportOptionsParameters();

    HashMap<String, String> exportParameters = new HashMap<>();

    for (PreservationParameter parameter : module.getParameters()) {
      switch (parameter.getInputType()) {
        case ViewerConstants.INPUT_TYPE_CHECKBOX:
          if (checkBoxInputs.get(parameter.getName()) != null) {
            final boolean value = checkBoxInputs.get(parameter.getName()).getValue();
            exportParameters.put(parameter.getName(), String.valueOf(value));
          }
          break;
        case ViewerConstants.INPUT_TYPE_TEXT:
          if (textBoxInputs.get(parameter.getName()) != null) {
            final String text = textBoxInputs.get(parameter.getName()).getText();
            exportParameters.put(parameter.getName(), text);
          }
          if (ViewerConstants.SIARDDK.equals(version)) {
            final String text = externalLobsInputs.get(parameter.getName()).getText();
            exportParameters.put(parameter.getName(), text);
          } else {
            if (externalLobCheckbox != null && externalLobCheckbox.getValue()) {
              if (externalLobsInputs.get(parameter.getName()) != null) {
                final String text = externalLobsInputs.get(parameter.getName()).getText();
                exportParameters.put(parameter.getName(), text);
              }
            }
          }
          break;
        case ViewerConstants.INPUT_TYPE_FOLDER:
        case ViewerConstants.INPUT_TYPE_FILE_OPEN:
        case ViewerConstants.INPUT_TYPE_FILE_SAVE:
          if (fileInputs.get(parameter.getName()) != null) {
            final String path = fileInputs.get(parameter.getName());
            exportOptionsParameters.setSiardPath(path);
            exportParameters.put(parameter.getName(), path);
          }
          break;
        case ViewerConstants.INPUT_TYPE_COMBOBOX:
          if (comboBoxInputs.get(parameter.getName()) != null) {
            exportParameters.put(parameter.getName(), comboBoxInputs.get(parameter.getName()).getSelectedValue());
          }
          break;
        case ViewerConstants.INPUT_TYPE_DEFAULT:
        case ViewerConstants.INPUT_TYPE_NONE:
        default:
          break;
      }
    }

    if (externalLobCheckbox != null && externalLobCheckbox.getValue() && version.equals(ViewerConstants.SIARD2)) {
      exportParameters.put(SIARD2ModuleFactory.PARAMETER_EXTERNAL_LOBS, "true");
    }

    exportOptionsParameters.setSiardVersion(version);
    exportOptionsParameters.setParameters(exportParameters);

    return exportOptionsParameters;
  }

  public int validate() {
    if (validateExternalLobs() != SIARDExportOptions.OK) {
      validationError = SIARDExportOptions.EXTERNAL_LOBS_ERROR;
      return SIARDExportOptions.EXTERNAL_LOBS_ERROR;
    }

    final List<PreservationParameter> requiredParameters = module.getRequiredParameters();

    for (PreservationParameter parameter : requiredParameters) {
      switch (parameter.getInputType()) {
        case ViewerConstants.INPUT_TYPE_TEXT:
          if (textBoxInputs.get(parameter.getName()) != null) {
            final TextBox textBox = textBoxInputs.get(parameter.getName());
            if (ViewerStringUtils.isBlank(textBox.getText())) {
              validationError = SIARDExportOptions.MISSING_FIELD;
              return SIARDExportOptions.MISSING_FIELD;
            } else {
              validationError = SIARDExportOptions.MISSING_FIELD;
              return SIARDExportOptions.MISSING_FIELD;
            }
          }
          break;
        case ViewerConstants.INPUT_TYPE_FOLDER:
        case ViewerConstants.INPUT_TYPE_FILE_OPEN:
        case ViewerConstants.INPUT_TYPE_FILE_SAVE:
          if (fileInputs.get(parameter.getName()) != null) {
            final String s = fileInputs.get(parameter.getName());
            if (ViewerStringUtils.isBlank(s)) {
              validationError = SIARDExportOptions.MISSING_FILE;
              return SIARDExportOptions.MISSING_FILE;
            } else {
              if (version.equals(ViewerConstants.SIARDDK)) {
                final RegExp compile = RegExp.compile("AVID.[A-ZÆØÅ]{2,4}.[1-9][0-9]*.[1-9][0-9]*");
                final boolean test = compile.test(s);
                if (!test) {
                  validationError = SIARDExportOptions.SIARDDK_FOLDER_NAME;
                  return SIARDExportOptions.SIARDDK_FOLDER_NAME;
                }
              }
            }
          } else {
            validationError = SIARDExportOptions.MISSING_FILE;
            return SIARDExportOptions.MISSING_FILE;
          }
          break;
        default:
      }
    }
    return SIARDExportOptions.OK;
  }

  public void clear() {
    instances.clear();
  }

  public void error() {
    if (validationError != -1) {
      Toast.showError(messages.errorMessagesExportOptionsTitle(), messages.errorMessagesExportOptions(validationError));
    }
  }

  private int validateExternalLobs() {
    if (ViewerConstants.SIARDDK.equals(version)) {
      return SIARDExportOptions.OK;
    }
    if (externalLobCheckbox != null && externalLobCheckbox.getValue()) {
      for (TextBox textBox : externalLobsInputs.values()) {
        final String text = textBox.getText();
        if (ViewerStringUtils.isBlank(text)) {
          return SIARDExportOptions.MISSING_FIELD;
        }
      }
    }
    return SIARDExportOptions.OK;
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

  private void buildExternalLobs(PreservationParameter parameter, FlowPanel panel) {
    GenericField genericField;

    switch (parameter.getInputType()) {
      case ViewerConstants.INPUT_TYPE_CHECKBOX:
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
      case ViewerConstants.INPUT_TYPE_TEXT:
        Label label = new Label();
        label.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        externalLobsLabels.add(label);
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox-external-lobs");
        defaultTextBox.setText(parameter.getDefaultValue());
        externalLobsInputs.put(parameter.getName(), defaultTextBox);
        Label labelEnd = new Label();
        labelEnd.setText(messages.wizardExportOptionsLabels(parameter.getName() + "-end"));
        externalLobsLabels.add(labelEnd);
        if (version.equals(ViewerConstants.SIARDDK)) {
          label.addStyleName("form-label");
          labelEnd.addStyleName("form-label");
        } else {
          label.addStyleName("form-label gwt-Label-disabled");
          labelEnd.addStyleName("form-label gwt-Label-disabled");
          defaultTextBox.setEnabled(false);
        }
        FlowPanel formHelper = new FlowPanel();
        formHelper.addStyleName("form-helper");
        FlowPanel formRow = new FlowPanel();
        formRow.addStyleName("form-row");
        formRow.add(label);
        formRow.add(defaultTextBox);
        formRow.add(labelEnd);
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");
        span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));
        formHelper.add(formRow);
        formHelper.add(span);
        panel.add(formHelper);
        panel.addStyleName("form-lobs");
        content.add(panel);
        break;
      default:
        break;
    }
  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;
    String spanCSSClass = "form-text-helper text-muted";

    switch (parameter.getInputType()) {
      case ViewerConstants.INPUT_TYPE_COMBOBOX:
        ComboBoxField comboBoxField = ComboBoxField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()));
        parameter.getPossibleValues().forEach(key -> comboBoxField.setComboBoxValue(messages.wizardExportOptionsForPossibleValues(key), key));
        comboBoxField.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");
        comboBoxField.select(parameter.getDefaultIndex());
        comboBoxInputs.put(parameter.getName(), comboBoxField);
        genericField = GenericField.createInstance(comboBoxField);
        break;

      case ViewerConstants.INPUT_TYPE_CHECKBOX:
        CheckBox checkbox = new CheckBox();
        checkbox.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        checkbox.addStyleName("form-checkbox");
        checkBoxInputs.put(parameter.getName(), checkbox);
        genericField = GenericField.createInstance(checkbox);
        spanCSSClass = "form-text-helper-checkbox text-muted";
        break;
      case ViewerConstants.INPUT_TYPE_FILE_SAVE:
      case ViewerConstants.INPUT_TYPE_FILE_OPEN:
        FileUploadField fileUploadField = FileUploadField
          .createInstance(messages.wizardExportOptionsLabels(parameter.getName()), messages.basicActionBrowse());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
            JavaScriptObject.createArray();
            ExtensionFilter extensionFilter = new ExtensionFilter()
              .createFilterTypeFromDBPTK(parameter.getFileFilter());
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.emptyList(),
              Collections.singletonList(extensionFilter));
            String path = null;
            if (parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_FILE_SAVE)) {
              path = JavascriptUtils.saveFileDialog(options);
            }
            if (parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_FILE_OPEN)) {
              path = JavascriptUtils.openFileDialog(options);
            }
            if (path != null) {
              fileInputs.put(parameter.getName(), path);
              fileUploadField.setPathLocation(path, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });
        if (!version.equals(ViewerConstants.SIARDDK)) {
          if (defaultPath != null) {
            fileInputs.put(parameter.getName(), defaultPath);
            fileUploadField.setPathLocation(defaultPath, defaultPath);
          }
        }
        FlowPanel helper = new FlowPanel();
        helper.addStyleName("form-helper");
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");
        span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));
        helper.add(fileUploadField);
        helper.add(span);
        content.add(helper);
        break;
      case ViewerConstants.INPUT_TYPE_FOLDER:
        FileUploadField folder = FileUploadField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()),
          messages.basicActionBrowse());
        folder.setParentCSS("form-row");
        folder.setLabelCSS("form-label-spaced");
        folder.setButtonCSS("btn btn-link form-button");
        folder.setRequired(parameter.isRequired());
        folder.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
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
        FlowPanel helperFolder = new FlowPanel();
        helperFolder.addStyleName("form-helper");
        InlineHTML spanFolder = new InlineHTML();
        spanFolder.addStyleName("form-text-helper text-muted");
        spanFolder.setText(messages.wizardExportOptionsHelperText(parameter.getName()));
        helperFolder.add(folder);
        helperFolder.add(spanFolder);
        content.add(helperFolder);
        break;
      case ViewerConstants.INPUT_TYPE_NONE:
        genericField = null;
        break;
      case ViewerConstants.INPUT_TYPE_NUMBER:
      case ViewerConstants.INPUT_TYPE_TEXT:
      default:
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        genericField = GenericField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()),
          defaultTextBox);
        break;
    }

    if (genericField != null) {
      FlowPanel helper = new FlowPanel();
      helper.addStyleName("form-helper");
      InlineHTML span = new InlineHTML();
      span.addStyleName(spanCSSClass);
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