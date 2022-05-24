/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_CONTEXT)
@Tag(name = ContextService.SWAGGER_ENDPOINT)
public interface ContextService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 context";

  class Util {
    /**
     * @return the singleton instance
     */
    public static ContextService get() {
      return GWT.create(ContextService.class);
    }

    public static <T> ContextService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> ContextService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }
  }

  @GET
  @Path("/environment")
  @Produces({MediaType.TEXT_PLAIN})
  @Operation(summary = "Retrieves the environment", hidden = true)
  String getEnvironment();

  @GET
  @Path("/clientMachineHost")
  @Produces({MediaType.TEXT_PLAIN})
  @Operation(summary = "Retrieves the client machine host", hidden = true)
  String getClientMachine();

  @GET
  @Path("/shared/properties")
  @Operation(summary = "Retrieves the shared properties", hidden = true)
  Map<String, List<String>> getSharedProperties(
    @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);
}
