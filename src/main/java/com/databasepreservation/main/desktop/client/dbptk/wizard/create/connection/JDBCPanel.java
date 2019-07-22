package com.databasepreservation.main.desktop.client.dbptk.wizard.create.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.common.shared.client.tools.PathUtils;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.desktop.client.common.FileUploadField;
import com.databasepreservation.main.desktop.client.common.GenericField;
import com.databasepreservation.main.desktop.shared.models.Filter;
import com.databasepreservation.main.desktop.shared.models.JDBCParameters;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.springframework.web.util.JavaScriptUtils;

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

  @UiField
  FlowPanel content;

  public static JDBCPanel getInstance(String connection, ArrayList<PreservationParameter> parameters) {
    if (instances.get(connection) == null) {
      JDBCPanel instance = new JDBCPanel(parameters);
      instances.put(connection, instance);
    }

    return instances.get(connection);
  }

  private JDBCPanel(ArrayList<PreservationParameter> parameters) {
    initWidget(binder.createAndBindUi(this));

    this.parameters = parameters;

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
      case "PASSWORD":
        PasswordTextBox passwordTextBox = new PasswordTextBox();
        passwordTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), passwordTextBox);
        genericField = GenericField.createInstance(messages.connectionLabels(parameter.getName()), passwordTextBox);
        if(parameter.isRequired()) {
          passwordTextBox.getElement().setAttribute("required", "required");
        }
        break;
      case "CHECKBOX":
        CheckBox checkbox = new CheckBox();
        checkbox.setText(messages.connectionLabels(parameter.getName()));
        checkbox.addStyleName("form-checkbox");
        checkBoxInputs.put(parameter.getName(), checkbox);
        genericField = GenericField.createInstance(checkbox);
        break;
      case "FILE":
        FileUploadField fileUploadField = FileUploadField.createInstance(messages.connectionLabels(parameter.getName()), messages.chooseDriverLocation());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");
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
        content.add(fileUploadField);
        break;
      case "FOLDER":
        break;
      case "NUMBER":
      case "TEXT":
        default: TextBox defaultTextBox = new TextBox();
          defaultTextBox.addStyleName("form-textbox");
          textBoxInputs.put(parameter.getName(), defaultTextBox);
          genericField = GenericField.createInstance(messages.connectionLabels(parameter.getName()), defaultTextBox);
          if(parameter.isRequired())
            defaultTextBox.getElement().setAttribute("required", "required");
          break;
    }

    if (genericField != null) {
      genericField.setRequired(parameter.isRequired());
      genericField.setCSSMetadata("form-row", "form-label-spaced");
      content.add(genericField);
    }
  }

  public ArrayList<PreservationParameter> validate() {
    ArrayList<PreservationParameter> arrayList = new ArrayList<>();

    for (PreservationParameter parameter : parameters) {
      if (parameter.isRequired()) {
        if (parameter.getInputType().equals("TEXT")) {
          final TextBox textBox = textBoxInputs.get(parameter.getName());
          if (ViewerStringUtils.isBlank(textBox.getText())) {
            arrayList.add(parameter);
            textBox.addStyleName("wizard-connection-validator");
            textBox.getElement().setAttribute("required", "required");
          }
        }
        if (parameter.getInputType().equals("FILE")) {
          if (ViewerStringUtils.isBlank(pathToDriver)) {
            arrayList.add(parameter);
          }
        }
      }
    }
    return arrayList;
  }

  public void clear() {
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

  public void clearPasswords() {
    for (TextBox textBox : textBoxInputs.values()) {
      if (textBox instanceof PasswordTextBox) {
        textBox.setText("");
      }
    }
  }
}