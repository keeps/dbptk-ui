package com.databasepreservation.common.client.common;

import java.util.function.Consumer;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.common.api.v1.AuthenticationResource;
import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.AsyncCallbackUtils;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * Asynchronous callback with a default failure handler
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class DefaultMethodCallback<T> implements MethodCallback<T> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static <T> MethodCallback<T> get(final Consumer<T> consumer) {
    return new DefaultMethodCallback<T>() {
      @Override
      public void onSuccess(Method method, T t) {
        consumer.accept(t);
      }
    };
  }

  public static <T> MethodCallback<T> get(final Consumer<T> consumer, final Consumer<String> errorHandler) {
    return new MethodCallback<T>() {
      @Override
      public void onFailure(Method method, Throwable throwable) {
        final JSONValue parse = JSONParser.parseStrict(method.getResponse().getText());
        String message = parse.isObject().get("message").isString().stringValue();
        new ClientLogger(AsyncCallbackUtils.class.getName()).error("AsyncCallback error - " + message);
        errorHandler.accept(message);
      }

      @Override
      public void onSuccess(Method method, T t) {
        consumer.accept(t);
      }
    };
  }

  @Override
  public void onFailure(Method method, Throwable throwable) {
    final JSONValue parse = JSONParser.parseStrict(method.getResponse().getText());
    String message = parse.isObject().get("message").isString().stringValue();

    GWT.log(method.getResponse().getText());

    // TODO resolve specific exceptions
    if (method.getResponse().getStatusCode() == Response.SC_UNAUTHORIZED) {
      // TODO open dialog to states that is unauthorized and ask to login if currently
      // not logged in (guest) or to ask the administrator to add permissions to your
      // user.
      AuthenticationResource.Util.call((User user) -> {
        if (user.isGuest()) {
          UserLogin.getInstance().showSuggestLoginDialog();
        } else {
          Dialogs.showInformationDialog(messages.loginDialogTitle(), messages.loginMissingPermissions(), messages.basicActionClose(),
            "btn btn-close", new AsyncCallback<Void>() {
              @Override
              public void onFailure(Throwable caught) {
                HistoryManager.gotoHome();
              }

              @Override
              public void onSuccess(Void result) {
                HistoryManager.gotoHome();
              }
            });
        }
      }).getAuthenticatedUser();

    } else if (method.getResponse().getStatusCode() == Response.SC_NOT_FOUND) {
      Dialogs.showInformationDialog(messages.loginDialogTitle(), messages.objectNotFound(), messages.basicActionClose(),
          "btn btn-close", new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
              HistoryManager.gotoHome();
            }

            @Override
            public void onSuccess(Void result) {
              HistoryManager.gotoHome();
            }
          });
    }

    new ClientLogger(AsyncCallbackUtils.class.getName())
        .error("AsyncCallback error - " + message);
  }
}
