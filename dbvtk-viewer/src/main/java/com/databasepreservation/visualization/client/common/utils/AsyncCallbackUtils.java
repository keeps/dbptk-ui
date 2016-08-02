/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.client.common.utils;

import org.roda.core.data.exceptions.AuthorizationDeniedException;

import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.databasepreservation.visualization.shared.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.StatusCodeException;

import config.i18n.client.ClientMessages;

public class AsyncCallbackUtils {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final boolean treatCommonFailures(Throwable caught) {
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
      // UserLogin.getInstance().login();
      // treatedError = true;
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
