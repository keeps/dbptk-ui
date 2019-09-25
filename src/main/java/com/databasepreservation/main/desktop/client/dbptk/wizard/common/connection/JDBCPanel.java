package com.databasepreservation.main.desktop.client.dbptk.wizard.common.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.desktop.FileUploadField;
import com.databasepreservation.main.common.shared.client.common.desktop.GenericField;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.common.shared.client.tools.PathUtils;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.download.DBMSWizardManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.upload.CreateWizardManager;
import com.databasepreservation.main.desktop.shared.models.Filter;
import com.databasepreservation.main.common.shared.models.JDBCParameters;
import com.databasepreservation.main.common.shared.models.PreservationParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
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

  private static HashMap<String, JDBCPanel> instances = new HashMap<>();
  private HashMap<String, TextBox> textBoxInputs = new HashMap<>();
  private HashMap<String, CheckBox> checkBoxInputs = new HashMap<>();
  private HashMap<String, FileUploadField> fileInputs = new HashMap<>();
  private String pathToDriver = null;
  private ArrayList<PreservationParameter> parameters;
  private TextBox focusElement = null;
  private String databaseUUID;
  private final String type;

  @UiField
  FlowPanel content;

  public static JDBCPanel getInstance(String connection, ArrayList<PreservationParameter> parameters, String databaseUUID, String type) {
    String code =  databaseUUID + ViewerConstants.API_SEP + connection;
    if (instances.get(code) == null) {
      JDBCPanel instance = new JDBCPanel(parameters, databaseUUID, type);
      instances.put(code, instance);
    }
    return instances.get(code);
  }

  private JDBCPanel(ArrayList<PreservationParameter> parameters, String databaseUUID, String type) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;
    this.parameters = parameters;
    this.type = type;

    for (PreservationParameter p : parameters) {
      buildGenericWidget(p);
    }
  }

  public JDBCParameters getValues() {
    JDBCParameters parameters = new JDBCParameters();

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

    parameters.setConnection(values);
    if (ViewerStringUtils.isNotBlank(pathToDriver)) {
      parameters.setDriver(true);
      parameters.setDriverPath(pathToDriver);
    }

    return parameters;
  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;

    switch (parameter.getInputType()) {
      case ViewerConstants.INPUT_TYPE_PASSWORD:
        PasswordTextBox passwordTextBox = new PasswordTextBox();
        passwordTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), passwordTextBox);
        genericField = GenericField.createInstance(messages.connectionPageLabelsFor(parameter.getName()), passwordTextBox);
        if(parameter.isRequired()) {
          passwordTextBox.getElement().setAttribute("required", "required");
          passwordTextBox.addKeyUpHandler(event -> {
            if(event.getNativeKeyCode() != KeyCodes.KEY_TAB){
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
      case ViewerConstants.INPUT_TYPE_FILE:
        FileUploadField fileUploadField = FileUploadField.createInstance(messages.connectionPageLabelsFor(parameter.getName()), messages.connectionPageLabelForChooseDriverLocation());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button form-button-jar");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(new Command() {
          @Override
          public void execute() {
            if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
              Filter jar = new Filter("JAR File", Collections.singletonList("jar"));
              JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openFile"), Collections.singletonList(jar));

              String path = JavascriptUtils.openFileDialog(options);
              if (path != null) {
                pathToDriver = path;
                String displayPath = PathUtils.getFileName(path);
                fileUploadField.setPathLocation(displayPath, path);
                fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
              }
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
      case ViewerConstants.INPUT_TYPE_FOLDER:
        break;
      case ViewerConstants.INPUT_TYPE_NUMBER:
      case ViewerConstants.INPUT_TYPE_TEXT:
        default: TextBox defaultTextBox = new TextBox();
          defaultTextBox.addStyleName("form-textbox");
          textBoxInputs.put(parameter.getName(), defaultTextBox);
          if(focusElement == null){
            focusElement = defaultTextBox;
          }
          genericField = GenericField.createInstance(messages.connectionPageLabelsFor(parameter.getName()), defaultTextBox);
          if (parameter.getDefaultValue() != null) {
            defaultTextBox.setText(parameter.getDefaultValue());
          }
          if (parameter.isRequired()) {
            defaultTextBox.getElement().setAttribute("required", "required");
            defaultTextBox.addKeyUpHandler(event -> {
              if(event.getNativeKeyCode() != KeyCodes.KEY_TAB){
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
    GWT.log(type);
    validate(type);
  }

  /**
   *
   * @return true if all fields required fields are filled, otherwise false
   */
  public boolean validate(String type) {
    ArrayList<PreservationParameter> arrayList = new ArrayList<>();
    WizardManager wizardManager;
    if(type.equals(ViewerConstants.UPLOAD_WIZARD_MANAGER)){
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
        if (parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_FILE)) {
          if (ViewerStringUtils.isBlank(pathToDriver)) {
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
      fileUploadField.setPathLocation("","");
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
    focusElement.setFocus(true);
  }
}