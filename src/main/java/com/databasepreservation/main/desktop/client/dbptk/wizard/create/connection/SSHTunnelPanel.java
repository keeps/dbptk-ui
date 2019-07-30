package com.databasepreservation.main.desktop.client.dbptk.wizard.create.connection;

import com.databasepreservation.main.desktop.client.common.GenericField;
import com.databasepreservation.main.desktop.shared.models.SSHConfiguration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SSHTunnelPanel extends Composite {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface SSHTunnelPanelUiBinder extends UiBinder<Widget, SSHTunnelPanel> {
  }

  private static SSHTunnelPanelUiBinder binder = GWT.create(SSHTunnelPanelUiBinder.class);

  @UiField
  CheckBox tunnelSSH;

  @UiField
  Label proxyHostLabel, proxyPortLabel, proxyUserLabel, proxyPasswordLabel;

  @UiField
  TextBox proxyHost, proxyPort, proxyUser;

  @UiField
  PasswordTextBox proxyPassword;

  private static SSHTunnelPanel instance = null;

  public static SSHTunnelPanel getInstance() {
    if (instance == null) {
      instance = new SSHTunnelPanel();
    }
    return instance;
  }

  private SSHTunnelPanel() {
    initWidget(binder.createAndBindUi(this));
    enable(false);

    tunnelSSH.setText(messages.useSSHTunnel());
    tunnelSSH.addValueChangeHandler(event -> {
      enable(event.getValue());
    });
    proxyPort.setText("22");
  }

  private void enable(boolean value) {
    setRequired(proxyHostLabel, value);
    setRequired(proxyPortLabel, value);
    setRequired(proxyUserLabel, value);
    setRequired(proxyPasswordLabel, value);

    setEnabled(proxyHostLabel, value);
    setEnabled(proxyPortLabel, value);
    setEnabled(proxyUserLabel, value);
    setEnabled(proxyPasswordLabel, value);

    proxyHost.setEnabled(value);
    proxyPort.setEnabled(value);
    proxyUser.setEnabled(value);
    proxyPassword.setEnabled(value);

    setRequiredInput(proxyHost);
    setRequiredInput(proxyPort);
    setRequiredInput(proxyUser);
    setRequiredInput(proxyPassword);
  }

  private void setEnabled(Widget widget, boolean enabled) {
    String disabledCSS = "gwt-Label-disabled";
    String enabledCSS = "gwt-Label";
    if (enabled) {
      widget.removeStyleName(disabledCSS);
      widget.addStyleName(enabledCSS);
    } else {
      widget.removeStyleName(enabledCSS);
      widget.addStyleName(disabledCSS);
    }
  }

  private void setRequired(Widget label, boolean required) {
    if (required) label.addStyleName("form-label-mandatory");
    else label.removeStyleName("form-label-mandatory");
  }

  private void setRequiredInput(TextBox input) {
    input.addStyleName("wizard-connection-validator");
    input.addKeyUpHandler(event -> {
      if(input.getValue().isEmpty())
        input.getElement().setAttribute("required", "required");
    });
  }

  public boolean isSSHTunnelEnabled() {
    return tunnelSSH.getValue();
  }

  public SSHConfiguration getSSHConfiguration() {
    String host = proxyHost.getText();
    String port = proxyPort.getText();
    String user = proxyUser.getText();
    String password = proxyPassword.getText();

    return new SSHConfiguration(host, port, user, password);
  }

  public void clear() {
    tunnelSSH.setValue(false);
    proxyHost.setText("");
    proxyPassword.setText("");
    proxyUser.setText("");
    proxyPort.setText("");
  }

  public void clearPassword() {
    proxyPassword.setText("");
  }
}