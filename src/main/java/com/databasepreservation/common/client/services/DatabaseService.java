package com.databasepreservation.common.client.services;

import java.util.List;
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
import com.databasepreservation.common.client.models.ProgressData;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
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
  public static final String SWAGGER_ENDPOINT = "v1 database";

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

  @GET
  @Path("/generateUUID")
  @Produces({MediaType.TEXT_PLAIN})
  String generateUUID();

  @POST
  @Path("/schemaInformation")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Retrieves the schema information", notes = "", response = ViewerMetadata.class)
  ViewerMetadata getSchemaInformation(@ApiParam(value = "connection parameters") final ConnectionParameters connectionParameters);

  @POST
  @Path("/executeQuery")
  @ApiOperation(value = "Retrieves the first 5 rows of the query execution", notes = "", response = List.class, responseContainer = "List")
  List<List<String>> validateCustomViewQuery(@ApiParam(value = "connection parameters") ConnectionParameters parameters, @QueryParam("q") String query);


  @GET
  @Path("/progress/{databaseUUID}")
  @ApiOperation(value = "Retrieves the first 5 rows of the query execution", notes = "", response = ProgressData.class)
  ProgressData getProgressData(@PathParam("databaseUUID") String databaseUUID);

  @POST
  @Path("/find")
  @ApiOperation(value = "Finds all the databases", notes = "", response = ViewerDatabase.class, responseContainer = "IndexResult")
  IndexResult<ViewerDatabase> findDatabases(@ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest filter,
                                            @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);


  @GET
  @Path("/find/{databaseUUID}/{id}")
  @ApiOperation(value = "Retrieves a specific database", notes = "", response = ViewerDatabase.class)
  ViewerDatabase retrieve(@PathParam("databaseUUID") String databaseUUID, @PathParam("id") String id);

  @DELETE
  @Path("/delete/{databaseUUID}")
  @ApiOperation(value = "Deletes a specific database", notes = "", response = Boolean.class)
  Boolean deleteDatabase(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("/delete/rows/{databaseUUID}")
  @ApiOperation(value = "Deletes the row data for a specific database", notes = "", response = Boolean.class)
  Boolean deleteSolrData(@PathParam("databaseUUID") String databaseUUID);


  @POST
  @Path("find/rows/{databaseUUID}")
  @ApiOperation(value = "Find all rows for a specific database", notes = "", response = ViewerRow.class, responseContainer = "IndexResult")
  IndexResult<ViewerRow> findRows(@PathParam("databaseUUID") String databaseUUID,
                                  @ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
                                  @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);


  @GET
  @Path("find/rows/{databaseUUID}/{rowUUID}")
  @ApiOperation(value = "Retrieves a specific row within a specific database", notes = "", response = ViewerRow.class)
  ViewerRow retrieveRow(@PathParam("databaseUUID") String databaseUUID, @PathParam("rowUUID") String rowUUID);

  @GET
  @Path("/denormalize/{databaseuuid}")
  @ApiOperation(value = "retrieves the first 5 rows of the query execution", notes = "", response = ProgressData.class, responseContainer = "database metadata")
  Boolean denormalize(@PathParam("databaseuuid") String databaseuuid);
}
