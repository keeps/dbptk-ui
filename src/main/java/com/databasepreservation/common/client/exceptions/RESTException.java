/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.exceptions;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;

import com.google.gwt.http.client.Response;
import org.roda.core.data.exceptions.RequestNotValidException;

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

  public RESTException(String message, Throwable cause) {
    super(message + getCauseMessage(cause));
    this.status = getResponseStatusCode(cause);
  }

  public RESTException(Throwable cause) {
    super("Remote exception" + getCauseMessage(cause));
    this.status = getResponseStatusCode(cause);
  }

  public RESTException(String message, int status) {
    super(message);
    this.status = status;
  }

  public RESTException(Throwable cause, int status) {
    super("Remote exception" + getCauseMessage(cause));
    this.status = status;
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

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public RESTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  private int getResponseStatusCode(Throwable cause) {
    if (cause instanceof AuthorizationDeniedException) {
      return Response.SC_UNAUTHORIZED;
    } else if (cause instanceof NotFoundException) {
      return Response.SC_NOT_FOUND;
    } else if (cause instanceof AlreadyExistsException) {
      return Response.SC_CONFLICT;
    } else if (cause instanceof SavedSearchException) {
      return Response.SC_BAD_REQUEST;
    } else if (cause instanceof GenericException) {
      return Response.SC_BAD_REQUEST;
    } else if (cause instanceof RequestNotValidException) {
      return Response.SC_BAD_REQUEST;
    }
    return Response.SC_INTERNAL_SERVER_ERROR;
  }
}
