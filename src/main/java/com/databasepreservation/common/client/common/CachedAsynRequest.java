/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common;

import java.util.LinkedList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

public abstract class CachedAsynRequest<T> {

  private T cached = null;
  private boolean loadingFromServer = false;
  private final LinkedList<AsyncCallback<T>> waitQueue = new LinkedList<AsyncCallback<T>>();

  public abstract void getFromServer(MethodCallback<T> callback);

  public void clearCache() {
    this.cached = null;
  }

  public void setCached(T cached) {
    this.cached = cached;
  }

  public void request(AsyncCallback<T> callback) {
    if (cached != null) {
      callback.onSuccess(cached);
    } else {
      waitQueue.add(callback);
      ensureIsLoadingFromServer();
    }
  }

  private void ensureIsLoadingFromServer() {
    if (!loadingFromServer) {
      loadingFromServer = true;
      getFromServer(new MethodCallback<T>() {

        @Override
        public void onFailure(Method method, Throwable throwable) {
          while (!waitQueue.isEmpty()) {
            waitQueue.pop().onFailure(throwable);
          }
          loadingFromServer = false;
        }

        @Override
        public void onSuccess(Method method, T result) {
          cached = result;
          while (!waitQueue.isEmpty()) {
            waitQueue.pop().onSuccess(result);
          }
          loadingFromServer = false;

        }
      });

    }
  }
}
