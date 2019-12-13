package com.databasepreservation.common.client.exceptions;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class AuthorizationException extends RESTException {
  public AuthorizationException() {
  }

  public AuthorizationException(String message) {
    super(message);
  }

  public AuthorizationException(String message, int status) {
    super(message, status);
  }

  public AuthorizationException(Throwable cause) {
    super(cause);
  }

  public AuthorizationException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
