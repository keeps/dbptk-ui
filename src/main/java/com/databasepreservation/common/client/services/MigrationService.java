/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.List;
import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.wizard.CreateSIARDParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionResponse;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_MIGRATION)
@Api(value = MigrationService.SWAGGER_ENDPOINT)
public interface MigrationService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 migration";

  class Util {
    /**
     * @return the singleton instance
     */
    public static MigrationService get() {
      return GWT.create(MigrationService.class);
    }

    public static <T> MigrationService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> MigrationService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> MigrationService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @GET
  @Path("/siard/modules")
  @ApiOperation(value = "Retrieves the DBPTK developer SIARD migration modules", notes = "", response = Module.class, responseContainer = "List")
  List<Module> getSiardModules(@QueryParam("type") String type, @QueryParam("moduleName") String moduleName);

  @GET
  @Path("/dbms/modules")
  @ApiOperation(value = "Retrieves the DBPTK developer DBMS migration modules", notes = "", response = Module.class, responseContainer = "List")
  List<Module> getDBMSModules(@QueryParam("type") String type, @QueryParam("moduleName") String moduleName);

  @GET
  @Path("/filter/modules")
  @ApiOperation(value = "Retrieves the DBPTK developer filter modules", notes = "", response = Module.class, responseContainer = "List")
  List<Module> getFilterModules(@QueryParam("moduleName") String moduleName);

  @POST
  @Path("/dbms/test/connection")
  @ApiOperation(value = "Tests the connection to the database", notes = "", response = ConnectionResponse.class)
  ConnectionResponse testConnection(
    @ApiParam(value = "DBMS connection parameters") final ConnectionParameters connectionParameters);

  @POST
  @Path("/dbms/test/query")
  @ApiOperation(value = "Retrieves the first 5 rows of the query execution", notes = "", response = String.class, responseContainer = "List")
  List<List<String>> testQuery(@ApiParam(value = "connection parameters") ConnectionParameters parameters,
    @QueryParam("query") String query);

  @POST
  @Path("/dbms/metadata")
  @ApiOperation(value = "Retrieves the metadata information associated with the database schema", notes = "", response = ViewerMetadata.class)
  ViewerMetadata getMetadata(
    @ApiParam(value = "connection parameters") final ConnectionParameters connectionParameters);

  @POST
  @Path("/run")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Performs the migration operation", notes = "", response = String.class)
  String run(@QueryParam("databaseUUID") String databaseUUID, @ApiParam("parameters") CreateSIARDParameters parameters);
}
