/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.desktop.client.dbptk.wizard.common.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.fields.FileUploadField;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.models.JSO.ExtensionFilter;
import com.databasepreservation.common.client.models.parameters.PreservationParameter;
import com.databasepreservation.common.client.models.wizard.connection.JDBCParameters;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.tools.PathUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardManager;
import com.databasepreservation.desktop.client.dbptk.wizard.download.DBMSWizardManager;
import com.databasepreservation.desktop.client.dbptk.wizard.upload.CreateWizardManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JDBCPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface JBDCPanelUiBinder extends UiBinder<Widget, JDBCPanel> {
  }

  private static JBDCPanelUiBinder binder = GWT.create(JBDCPanelUiBinder.class);

  private static Map<String, JDBCPanel> instances = new HashMap<>();
  private Map<String, TextBox> textBoxInputs = new HashMap<>();
  private Map<String, CheckBox> checkBoxInputs = new HashMap<>();
  private Map<String, FileUploadField> fileInputs = new HashMap<>();
  private String pathToDriver = null;
  private List<PreservationParameter> parameters;
  private TextBox focusElement = null;
  private String databaseUUID;
  private final String type;

  @UiField
  FlowPanel content;

  public static JDBCPanel getInstance(String connection, List<PreservationParameter> parameters, String databaseUUID,
    String type) {
    String code = databaseUUID + ViewerConstants.API_SEP + connection;
    instances.computeIfAbsent(code, k -> new JDBCPanel(parameters, databaseUUID, type));
    return instances.get(code);
  }

  private JDBCPanel(List<PreservationParameter> parameters, String databaseUUID, String type) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;
    this.parameters = parameters;
    this.type = type;

    for (PreservationParameter p : parameters) {
      buildGenericWidget(p);
    }
  }

  public JDBCParameters getValues() {
    JDBCParameters jdbcParameters = new JDBCParameters();

    HashMap<String, String> values = new HashMap<>();
    for (Map.Entry<String, TextBox> entry : textBoxInputs.entrySet()) {
      if (ViewerStringUtils.isNotBlank(entry.getValue().getText())) {
        values.put(entry.getKey(), entry.getValue().getText());
      }
    }

    for (Map.Entry<String, CheckBox> entry : checkBoxInputs.entrySet()) {
      if (entry.getValue().getValue()) {
        values.put(entry.getKey(), entry.getValue().getValue().toString());
      }
    }

    for (Map.Entry<String, FileUploadField> entry : fileInputs.entrySet()) {
      if (ViewerStringUtils.isNotBlank(entry.getValue().getPathLocation())) {
        values.put(entry.getKey(), entry.getValue().getPathLocation());
      }
    }

    jdbcParameters.setConnection(values);
    if (ViewerStringUtils.isNotBlank(pathToDriver)) {
      jdbcParameters.setDriver(true);
      jdbcParameters.setDriverPath(pathToDriver);
    }

    return jdbcParameters;
  }

  private void buildCheckboxWidget(String label, String helperText) {
    CheckBox checkbox = new CheckBox();
    checkbox.setText(label);
    checkbox.addStyleName("form-checkbox");
    checkbox.addValueChangeHandler(ValueChangeEvent::getValue);
    GenericField genericField = GenericField.createInstance(checkbox);

    FlowPanel helper = new FlowPanel();
    helper.addStyleName("form-helper");
    InlineHTML span = new InlineHTML();
    genericField.setCSSMetadata("form-row", "form-label-spaced");
    span.addStyleName("form-text-helper-checkbox text-muted");
    span.setText(helperText);
    genericField.addHelperText(span);
    helper.add(genericField);
    helper.add(span);
    content.add(helper);
  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;

    switch (parameter.getInputType()) {
      case ViewerConstants.INPUT_TYPE_PASSWORD:
        PasswordTextBox passwordTextBox = new PasswordTextBox();
        passwordTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), passwordTextBox);
        genericField = GenericField.createInstance(messages.connectionPageLabelsFor(parameter.getName()),
          passwordTextBox);
        if (parameter.isRequired()) {
          passwordTextBox.getElement().setAttribute("required", "required");
          passwordTextBox.addKeyUpHandler(event -> {
            if (event.getNativeKeyCode() != KeyCodes.KEY_TAB) {
              selfValidator(passwordTextBox);
            }
          });
        }
        break;
      case ViewerConstants.INPUT_TYPE_CHECKBOX:
        CheckBox checkbox = new CheckBox();
        checkbox.setText(messages.connectionPageLabelsFor(parameter.getName()));
        checkbox.addStyleName("form-checkbox");
        checkBoxInputs.put(parameter.getName(), checkbox);
        genericField = GenericField.createInstance(checkbox);
        break;
      case ViewerConstants.INPUT_TYPE_FILE_OPEN: {
        FileUploadField fileUploadField = FileUploadField.createInstance(
          messages.connectionPageLabelsFor(parameter.getName()), messages.connectionPageLabelForChooseFileLocation());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button form-button-jar");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
            ExtensionFilter mdb = new ExtensionFilter("MS Access", Arrays.asList("mdb", "accdb"));
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openFile"),
              Collections.singletonList(mdb));

            String path = JavascriptUtils.openFileDialog(options);
            if (path != null) {
              String displayPath = PathUtils.getFileName(path);
              fileUploadField.setPathLocation(displayPath, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });
        fileInputs.put(parameter.getName(), fileUploadField);
        FlowPanel helper = new FlowPanel();
        helper.addStyleName("form-helper");
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");

        span.setText(messages.connectionPageDescriptionsFor(parameter.getName()));
        fileUploadField.addHelperText(span);
        helper.add(fileUploadField);
        helper.add(span);
        content.add(helper);
        break;

      }
      case ViewerConstants.INPUT_TYPE_DRIVER:
        FileUploadField fileUploadField = FileUploadField.createInstance(
          messages.connectionPageLabelsFor(parameter.getName()), messages.connectionPageLabelForChooseDriverLocation());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button form-button-jar");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
            ExtensionFilter jar = new ExtensionFilter("JAR File", Collections.singletonList("jar"));
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openFile"),
              Collections.singletonList(jar));

            String path = JavascriptUtils.openFileDialog(options);
            if (path != null) {
              pathToDriver = path;
              String displayPath = PathUtils.getFileName(path);
              fileUploadField.setPathLocation(displayPath, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });
        FlowPanel helper = new FlowPanel();
        helper.addStyleName("form-helper");
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");

        span.setText(messages.connectionPageDescriptionsFor(parameter.getName()));
        fileUploadField.addHelperText(span);
        helper.add(fileUploadField);
        helper.add(span);
        content.add(helper);
        break;
      case ViewerConstants.INPUT_TYPE_FOLDER:
        break;
      case ViewerConstants.INPUT_TYPE_NUMBER:
      case ViewerConstants.INPUT_TYPE_TEXT:
      default:
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        if (focusElement == null) {
          focusElement = defaultTextBox;
        }
        genericField = GenericField.createInstance(messages.connectionPageLabelsFor(parameter.getName()),
          defaultTextBox);
        if (parameter.getDefaultValue() != null) {
          defaultTextBox.setText(parameter.getDefaultValue());
        }
        if (parameter.isRequired()) {
          defaultTextBox.getElement().setAttribute("required", "required");
          defaultTextBox.addKeyUpHandler(event -> {
            if (event.getNativeKeyCode() != KeyCodes.KEY_TAB) {
              selfValidator(defaultTextBox);
            }
          });
        }
        break;
    }

    if (genericField != null) {
      FlowPanel helper = new FlowPanel();
      helper.addStyleName("form-helper");
      InlineHTML span = new InlineHTML();
      genericField.setRequired(parameter.isRequired());
      genericField.setCSSMetadata("form-row", "form-label-spaced");
      if (genericField.getGenericFieldType().equals(CheckBox.class.getSimpleName())) {
        span.addStyleName("form-text-helper-checkbox text-muted");
      } else {
        span.addStyleName("form-text-helper text-muted");
      }

      span.setText(messages.connectionPageDescriptionsFor(parameter.getName()));
      genericField.addHelperText(span);
      helper.add(genericField);
      helper.add(span);
      content.add(helper);
    }
  }

  private void selfValidator(TextBox input) {
    if (input.getValue().isEmpty()) {
      input.addStyleName("wizard-connection-validator");
    } else {
      input.removeStyleName("wizard-connection-validator");
    }
    validate(type);
  }

  /**
   *
   * @return true if all fields required fields are filled, otherwise false
   */
  public boolean validate(String type) {
    ArrayList<PreservationParameter> arrayList = new ArrayList<>();
    WizardManager wizardManager;
    if (type.equals(ViewerConstants.UPLOAD_WIZARD_MANAGER)) {
      wizardManager = CreateWizardManager.getInstance();
    } else {
      wizardManager = DBMSWizardManager.getInstance(databaseUUID);
    }
    wizardManager.enableNext(true);

    for (PreservationParameter parameter : parameters) {
      if (parameter.isRequired()) {
        if (parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_TEXT)
          || parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_PASSWORD)) {
          final TextBox textBox = textBoxInputs.get(parameter.getName());
          if (ViewerStringUtils.isBlank(textBox.getText())) {
            arrayList.add(parameter);
            wizardManager.enableNext(false);
          }
        }
        if (parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_DRIVER)
          && ViewerStringUtils.isBlank(pathToDriver)) {
          arrayList.add(parameter);
        }

        if (parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_FILE_OPEN)) {
          final FileUploadField fileUploadField = fileInputs.get(parameter.getName());
          if (ViewerStringUtils.isBlank(fileUploadField.getPathLocation())) {
            arrayList.add(parameter);
          }
        }
      }
    }
    return arrayList.isEmpty();
  }

  public void clearInputs() {
    for (TextBox textBox : textBoxInputs.values()) {
      textBox.getElement().removeAttribute("required");
      textBox.setText("");
    }

    for (CheckBox checkBox : checkBoxInputs.values()) {
      checkBox.setValue(false);
    }

    for (FileUploadField fileUploadField : fileInputs.values()) {
      fileUploadField.setPathLocation("", "");
    }
  }

  public void clear() {
    instances.clear();
  }

  public void clearPasswords() {
    for (TextBox textBox : textBoxInputs.values()) {
      if (textBox instanceof PasswordTextBox) {
        textBox.setText("");
      }
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (focusElement != null) {
      focusElement.setFocus(true);
    }
  }
}