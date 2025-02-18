package com.databasepreservation.common.api.exceptions;

public class IllegalAccessException extends Exception {

  public IllegalAccessException() {
  }

  public IllegalAccessException(String message) {
    super(message);
  }

  public IllegalAccessException(Throwable cause) {
    super(cause);
  }

  public IllegalAccessException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
