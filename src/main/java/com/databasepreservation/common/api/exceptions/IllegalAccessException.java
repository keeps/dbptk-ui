/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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
