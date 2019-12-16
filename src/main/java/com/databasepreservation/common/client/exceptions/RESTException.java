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
    super(message);
    this.status = getResponseStatusCode(cause);
  }

  public RESTException(Throwable cause) {
    super(cause.getMessage());
    this.status = getResponseStatusCode(cause);
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
