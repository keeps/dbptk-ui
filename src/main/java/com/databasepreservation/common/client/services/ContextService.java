package com.databasepreservation.common.client.services;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.ApiOperation;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_CONTEXT)
@Api(value = ContextService.SWAGGER_ENDPOINT, hidden = true)
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
  @ApiOperation(value = "Retrieves the environment", notes = "", response = String.class)
  String getEnvironment();

  @GET
  @Path("/clientMachineHost")
  @Produces({MediaType.TEXT_PLAIN})
  @ApiOperation(value = "Retrieves the client machine host", notes = "", response = String.class)
  String getClientMachine();

  @GET
  @Path("/shared/properties")
  @ApiOperation(value = "Retrieves the shared properties", notes = "", response = Map.class)
  Map<String, List<String>> getSharedProperties(
    @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);
}
