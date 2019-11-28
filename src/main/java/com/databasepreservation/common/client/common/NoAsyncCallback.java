package com.databasepreservation.common.client.common;

public class NoAsyncCallback<T> extends DefaultAsyncCallback<T> {
  @Override
  public void onSuccess(T result) {
    // do nothing
  }
}
