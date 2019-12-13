package com.databasepreservation.common.client.exceptions;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.NotFoundException;

import com.google.gwt.http.client.Response;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RESTException extends RuntimeException {

  private int status = Response.SC_INTERNAL_SERVER_ERROR;

  public RESTException() {
  }

  public RESTException(String message) {
    super(message);
  }

  public RESTException(String message, int status) {
    super(message);
    this.status = status;
  }

  public RESTException(Throwable cause) {
    super(cause.getMessage());
    if (cause instanceof AuthorizationDeniedException) {
      this.status = Response.SC_UNAUTHORIZED;
    } else if (cause instanceof NotFoundException) {
      this.status = Response.SC_NOT_FOUND;
    } else if (cause instanceof AlreadyExistsException) {
      this.status = Response.SC_CONFLICT;
    }
  }

  public RESTException(Throwable cause, int status) {
    super(cause.getMessage());
    this.status = status;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public RESTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
