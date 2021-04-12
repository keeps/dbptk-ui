/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_DATABASE)
@Api(value = DatabaseService.SWAGGER_ENDPOINT)
public interface DatabaseService extends DirectRestService {
  String SWAGGER_ENDPOINT = "v1 database";

  class Util {
    /**
     * @return the singleton instance
     */
    public static DatabaseService get() {
      return GWT.create(DatabaseService.class);
    }

    public static <T> DatabaseService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> DatabaseService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> DatabaseService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  /*******************************************************************************
   * Database Resource
   *******************************************************************************/
  @POST
  @Path("/find")
  @ApiOperation(value = "Finds databases", notes = "", response = ViewerDatabase.class, responseContainer = "IndexResult")
  IndexResult<ViewerDatabase> find(@ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest filter,
                                   @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @POST
  @Path("/")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Creates a database", notes = "", response = String.class)
  String create(@ApiParam("path") String path);

  @GET
  @Path("/{databaseUUID}")
  @ApiOperation(value = "Retrieves a specific database", notes = "", response = ViewerDatabase.class)
  ViewerDatabase retrieve(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("/{databaseUUID}")
  @ApiOperation(value = "Deletes a specific database", notes = "", response = Boolean.class)
  Boolean delete(@PathParam("databaseUUID") String databaseUUID);
}
