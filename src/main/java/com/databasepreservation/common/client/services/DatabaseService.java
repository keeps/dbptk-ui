/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.Set;
import java.util.function.Consumer;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_DATABASE)
@Tag(name = DatabaseService.SWAGGER_ENDPOINT)
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
  @Operation(summary = "Finds databases")
  IndexResult<ViewerDatabase> find(@Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest filter,
    @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @POST
  @Path("/")
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Creates a database")
  String create(@Parameter(name = "path") String path);

  @GET
  @Path("/{databaseUUID}")
  @Operation(summary = "Retrieves a specific database")
  ViewerDatabase retrieve(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("/{databaseUUID}")
  @Operation(summary = "Deletes a specific database")
  Boolean delete(@PathParam("databaseUUID") String databaseUUID);

  @GET
  @Path("/{databaseUUID}/permissions")
  @Operation(summary = "Gets the internal database configuration")
  Set<String> getDatabasePermissions(@PathParam("databaseUUID") String databaseUUID);

  @PUT
  @Path("/{databaseUUID}/permissions")
  @Operation(summary = "Updates database permissions")
  Set<String> updateDatabasePermissions(@PathParam("databaseUUID") String databaseUUID,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) Set<String> permissions);
}
