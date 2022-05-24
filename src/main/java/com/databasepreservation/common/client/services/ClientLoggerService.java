/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_CLIENT_LOGGER)
@Tag(name = ClientLoggerService.SWAGGER_ENDPOINT)
public interface ClientLoggerService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 client logger";

  class Util {
    /**
     * @return the singleton instance
     */
    public static ClientLoggerService get() {
      return GWT.create(ClientLoggerService.class);
    }

    public static <T> ClientLoggerService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> ClientLoggerService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> ClientLoggerService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @POST
  @Path("/log")
  void log(
      @Parameter(name = "Log type", schema = @Schema(allowableValues = "debug, error, fatal, info, trace, warn"), required = true) @QueryParam("type") String type,
      @Parameter(required = true) @QueryParam("classname") String classname, @QueryParam("object") String object);

  @POST
  @Path("/detailedLog")
  void detailedLog(
      @Parameter(name = "Log type", schema = @Schema(allowableValues = "debug, error, fatal, info, trace, warn"), required = true) @QueryParam("type") String type,
      @Parameter(required = true) @QueryParam("classname") String classname, @QueryParam("object") String object,
      Throwable error);
}
