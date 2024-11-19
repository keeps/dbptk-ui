/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.exceptions;

import com.databasepreservation.common.exceptions.AuthorizationException;
import com.databasepreservation.common.exceptions.SavedSearchException;
import com.google.gwt.http.client.Response;
import org.roda.core.data.exceptions.*;

import java.io.Serial;

/**
 * @author António Lindo <alindo@keep.pt>
 */

public class RESTException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 4667937307148805083L;

  private Throwable cause;

  public RESTException() {
  }

  public RESTException(Throwable cause) {
    super();
    this.cause = cause;
  }

  private static String getCauseMessage(Throwable e) {
    StringBuilder message = new StringBuilder();
    Throwable cause = e;

    while (cause != null) {
      message.append(" caused by ").append(cause.getClass().getSimpleName()).append(": ");
      if (cause.getMessage() != null) {
        message.append(cause.getMessage());
      }
      cause = cause.getCause();
    }
    return message.toString();
  }

  @Override
  public synchronized Throwable getCause() {
    return cause;
  }

  public int getStatus() {
    if (cause instanceof AuthorizationDeniedException || cause instanceof AuthorizationException) {
      return Response.SC_UNAUTHORIZED;
    } else if (cause instanceof NotFoundException) {
      return Response.SC_NOT_FOUND;
    } else if (cause instanceof AlreadyExistsException) {
      return Response.SC_CONFLICT;
    } else if (cause instanceof SavedSearchException || cause instanceof GenericException
      || cause instanceof RequestNotValidException) {
      return Response.SC_BAD_REQUEST;
    }
    return Response.SC_INTERNAL_SERVER_ERROR;
  }

}
