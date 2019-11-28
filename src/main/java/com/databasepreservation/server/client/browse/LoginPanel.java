package com.databasepreservation.server.client.browse;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class LoginPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static LoginPanelUiBinder uiBinder = GWT.create(LoginPanelUiBinder.class);

  interface LoginPanelUiBinder extends UiBinder<Widget, LoginPanel> {
  }

  private static LoginPanel instance = null;

  public static LoginPanel getInstance() {
    if (instance == null) {
      instance = new LoginPanel();
    }
    return instance;
  }

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  Button login;

  @UiField
  Label error;

  private boolean loggingIn = false;

  public LoginPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    addAttachHandler(event -> {
      if (event.isAttached()) {
        username.setFocus(true);
      }
    });
  }

  @UiHandler("login")
  void handleLogin(ClickEvent e) {
    doLogin();
  }

  @UiHandler("username")
  void handleUsernameKeyPress(KeyPressEvent event) {
    tryToLoginWhenEnterIsPressed(event);
  }

  @UiHandler("password")
  void handlePasswordKeyPress(KeyPressEvent event) {
    tryToLoginWhenEnterIsPressed(event);
  }

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forLogin());
  }

  private void tryToLoginWhenEnterIsPressed(KeyPressEvent event) {
    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      doLogin();
    }
  }

  private void doLogin() {
    if (loggingIn) {
      return;
    }
    loggingIn = true;

    String usernameText = username.getText();
    String passwordText = password.getText();
    error.setText("");

    if (usernameText.trim().length() == 0 || passwordText.trim().length() == 0) {
      error.setText(messages.fillUsernameAndPasswordMessage());
      loggingIn = false;
    } else {

      UserLogin.getInstance().login(usernameText, passwordText, new MethodCallback<User>() {
        @Override
        public void onFailure(Method method, Throwable exception) {
          if (exception instanceof GenericException) {
            error.setText(exception.getMessage());
          } else {
            error.setText(messages.couldNotLoginWithTheProvidedCredentials());
          }
          loggingIn = false;
        }

        @Override
        public void onSuccess(Method method, User response) {
          HistoryManager.returnFromLogin();
          loggingIn = false;
        }
      });
    }
  }
}
