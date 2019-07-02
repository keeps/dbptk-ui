package com.databasepreservation.main.desktop.client.dbptk.wizard.create.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.desktop.client.common.GenericField;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JDBCPanel extends Composite {
  private static final String JDBCPANEL_URL_KEY = "url";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface JBDCPanelUiBinder extends UiBinder<Widget, JDBCPanel> {
  }

  private static JBDCPanelUiBinder binder = GWT.create(JBDCPanelUiBinder.class);

  private static HashMap<String, JDBCPanel> instances = new HashMap<>();
  private HashMap<String, TextBox> inputs = new HashMap<>();

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

    for (PreservationParameter p : parameters) {
      buildGenericWidget(p);
    }

    TextBox textBox = new TextBox();
    textBox.addStyleName("form-textbox");
    inputs.put(JDBCPANEL_URL_KEY, textBox);
    final GenericField genericField = GenericField.createInstance(messages.connectionURLLabel(), textBox);
    genericField.setCSSMetadata("form-row", "form-label form-label-spaced");
    content.add(genericField);
  }

  public HashMap<String, String> getValues() {
    HashMap<String, String> values = new HashMap<>();
    for (Map.Entry<String, TextBox> entry : inputs.entrySet()) {
      values.put(entry.getKey(), entry.getValue().getText());
    }

    return values;
  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField;

    if (parameter.hasArgument()) {
      if (parameter.getName().equals("password")) {
        PasswordTextBox passwordTextBox = new PasswordTextBox();
        passwordTextBox.addStyleName("form-textbox");
        inputs.put(parameter.getName(), passwordTextBox);
        genericField = GenericField.createInstance(messages.connectionLabels(parameter.getName()), passwordTextBox);
      } else {
        TextBox textBox = new TextBox();
        textBox.addStyleName("form-textbox");
        inputs.put(parameter.getName(), textBox);
        genericField = GenericField.createInstance(messages.connectionLabels(parameter.getName()), textBox);
      }

      genericField.setCSSMetadata("form-row", "form-label form-label-spaced");
    } else {
      CheckBox checkbox = new CheckBox();
      checkbox.setText(parameter.getName());
      // inputs.put(parameter.getName(), checkbox);

      genericField = GenericField.createInstance(checkbox);
    }

    content.add(genericField);
  }
}