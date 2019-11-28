package com.databasepreservation.common.client.common;

import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.common.utils.AsyncCallbackUtils;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import java.util.function.Consumer;

/**
 * Asynchronous callback with a default failure handler
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class DefaultMethodCallback<T> implements MethodCallback<T> {

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

    // TODO resolve specific exceptions

    new ClientLogger(AsyncCallbackUtils.class.getName())
        .error("AsyncCallback error - " + message);
  }
}
