package com.databasepreservation.main.desktop.client.dbptk.wizard.create.connection;

import com.databasepreservation.main.desktop.shared.models.SSHConfiguration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
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
  CheckBox SSHTunnel;

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

    SSHTunnel.setText(messages.useSSHTunnel());

    enable(false);

    SSHTunnel.addValueChangeHandler(event -> {
      enable(event.getValue());
    });
  }

  private void enable(boolean value) {

    setLabelEnable(value);
    proxyHost.setEnabled(value);
    proxyPort.setEnabled(value);
    proxyUser.setEnabled(value);
    proxyPassword.setEnabled(value);
  }

  private void setLabelEnable(boolean value) {
    String disabledCSS = "gwt-Label-disabled";
    String enabledCSS = "gwt-Label";

    if (value) {
      proxyHostLabel.removeStyleName(disabledCSS);
      proxyHostLabel.addStyleName(enabledCSS);
      proxyPortLabel.removeStyleName(disabledCSS);
      proxyPortLabel.addStyleName(enabledCSS);
      proxyUserLabel.removeStyleName(disabledCSS);
      proxyUserLabel.addStyleName(enabledCSS);
      proxyPasswordLabel.removeStyleName(disabledCSS);
      proxyPasswordLabel.addStyleName(enabledCSS);
    } else {
      proxyHostLabel.removeStyleName(enabledCSS);
      proxyHostLabel.addStyleName(disabledCSS);
      proxyPortLabel.removeStyleName(enabledCSS);
      proxyPortLabel.addStyleName(disabledCSS);
      proxyUserLabel.removeStyleName(enabledCSS);
      proxyUserLabel.addStyleName(disabledCSS);
      proxyPasswordLabel.removeStyleName(enabledCSS);
      proxyPasswordLabel.addStyleName(disabledCSS);
    }
  }

  public boolean isSSHTunnelEnabled() {
    return SSHTunnel.getValue();
  }

  public SSHConfiguration getSSHConfiguration() {
    String host = proxyHost.getText();
    String port = proxyPort.getText();
    String user = proxyUser.getText();
    String password = proxyPassword.getText();

    return new SSHConfiguration(host, port, user, password);
  }
}