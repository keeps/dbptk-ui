package com.databasepreservation.common.api.exceptions;

import java.io.IOException;

public class IllegalAccessException extends IOException {

  public IllegalAccessException() {
  }

  public IllegalAccessException(String message) {
    super(message);
  }

  public IllegalAccessException(Throwable cause) {
    super(cause);
  }
}
