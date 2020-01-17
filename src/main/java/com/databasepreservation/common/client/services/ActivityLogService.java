package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Path(".." + ViewerConstants.ENDPOINT_ACTIVITY_LOG)
@Api(value = ActivityLogService.SWAGGER_ENDPOINT)
public interface ActivityLogService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 activity log";
  class Util {
    /**
     * @return the singleton instance
     */
    public static ActivityLogService get() {
      return GWT.create(ActivityLogService.class);
    }

    public static <T> ActivityLogService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> ActivityLogService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> ActivityLogService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @POST
  @Path("/find")
  @ApiOperation(value = "Finds logs", response = IndexResult.class)
  IndexResult<ActivityLogEntry> find(
    @ApiParam(value = "Find request to filter/limit the search", required = true, name = ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
    @ApiParam(value = "Locale") @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String locale);

  @GET
  @Path("/{logUUID}")
  @ApiOperation(value = "Retrieve a specific log detail", response = ActivityLogWrapper.class)
  ActivityLogWrapper retrieve( @ApiParam(value = "The log unique identifier") @PathParam("logUUID") String logUUID);
}
