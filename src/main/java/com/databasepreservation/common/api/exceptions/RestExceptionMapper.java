/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.exceptions;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.api.utils.ApiResponseMessage;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.client.exceptions.RESTException;

@Provider
public class RestExceptionMapper implements ExceptionMapper<RESTException> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionMapper.class);

  @Inject
  private javax.inject.Provider<ContainerRequest> containerRequestProvider;

  @Override
  public Response toResponse(RESTException e) {
    ContainerRequest containerRequest = containerRequestProvider.get();
    String parameter = containerRequest.getProperty("acceptFormat") != null
      ? (String) containerRequest.getProperty("acceptFormat")
      : "";
    String header = containerRequest.getHeaderString("Accept");
    String mediaType = ApiUtils.getMediaType(parameter, header);

    ResponseBuilder responseBuilder;
    String message = e.getClass().getSimpleName() + ": " + e.getMessage();
    if (e.getCause() != null) {
      message += ", caused by " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
    }
    LOGGER.debug("Creating error response. MediaType: {}; Message: {}", mediaType, message, e);
    responseBuilder = Response.status(e.getStatus()).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));

    return responseBuilder.type(mediaType).build();
  }

}
