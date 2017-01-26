/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.client.common.utils;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.UserLogin;
import com.databasepreservation.visualization.client.common.dialogs.Dialogs;
import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.databasepreservation.visualization.shared.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

import config.i18n.client.ClientMessages;

public class AsyncCallbackUtils {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final boolean treatCommonFailures(final Throwable caught) {
    boolean treatedError = false;
    if (caught instanceof StatusCodeException && ((StatusCodeException) caught).getStatusCode() == 0) {
      // check if browser is offline
      if (!JavascriptUtils.isOnline()) {
        Toast.showError(messages.browserOfflineError());
      } else {
        Toast.showError(messages.cannotReachServerError());
      }
      treatedError = true;
    } else if (caught instanceof AuthorizationDeniedException) {
      UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {
        @Override
        public void onFailure(Throwable caught2) {
          Dialogs.showInformationDialog(messages.dialogPermissionDenied(), caught.getMessage(), messages.dialogLogin(),
            new DefaultAsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                UserLogin.getInstance().login();
              }
            });
        }

        @Override
        public void onSuccess(User result) {
          if (result.isGuest()) {
            UserLogin.getInstance().login();
          } else {
            Dialogs.showInformationDialog(messages.dialogPermissionDenied(), caught.getMessage(),
              messages.dialogLogin(), new DefaultAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                  UserLogin.getInstance().login();
                }
              });
          }
        }
      }, true);
      treatedError = true;
    } else if (caught instanceof NotFoundException) {
      Dialogs.showInformationDialog(messages.dialogResourceNotFound(), caught.getMessage(),
        messages.dialogNotFoundGoToHome(), new DefaultAsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            HistoryManager.gotoHome();
          }
        });
      treatedError = true;
    }
    return treatedError;
  }

  public static final void defaultFailureTreatment(Throwable caught) {
    if (!treatCommonFailures(caught)) {
      Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
      new ClientLogger(AsyncCallbackUtils.class.getName()).error("AsyncCallback error - "
        + caught.getClass().getSimpleName() + ": " + caught.getMessage(), caught);
    }
  }

}
