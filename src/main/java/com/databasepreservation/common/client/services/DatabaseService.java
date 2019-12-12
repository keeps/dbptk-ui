package com.databasepreservation.common.client.services;

import com.databasepreservation.common.client.models.ProgressData;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.google.gwt.core.client.GWT;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.exceptions.AuthorizationDeniedException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_DATABASE)
public interface DatabaseService extends DirectRestService {

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
  @ApiOperation(value = "retrieves the schema information", notes = "", response = String.class, responseContainer = "database metadata")
  String generateUUID();

  @POST
  @Path("/schemaInformation")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "retrieves the schema information", notes = "", response = ViewerMetadata.class, responseContainer = "database metadata")
  ViewerMetadata getSchemaInformation(@ApiParam(value = "connection parameters") final ConnectionParameters connectionParameters);

  @POST
  @Path("/executeQuery")
  @ApiOperation(value = "retrieves the first 5 rows of the query execution", notes = "", response = List.class, responseContainer = "database metadata")
  List<List<String>> validateCustomViewQuery(@ApiParam(value = "connection parameters") ConnectionParameters parameters, @QueryParam("q") String query);


  @GET
  @Path("/progress/{databaseuuid}")
  @ApiOperation(value = "retrieves the first 5 rows of the query execution", notes = "", response = ProgressData.class, responseContainer = "database metadata")
  ProgressData getProgressData(@PathParam("databaseuuid") String databaseuuid);

  @POST
  @Path("/find")
  @ApiOperation(value = "retrieves DBPTK export modules", notes = "", response = IndexResult.class, responseContainer = "IndexResult")
  IndexResult<ViewerDatabase> findDatabases(@ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest filter,
                                            @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);


  @GET
  @Path("/find/{databaseUUID}/{id}")
  @ApiOperation(value = "retrieves DBPTK export modules", notes = "", response = ViewerDatabase.class, responseContainer = "IndexResult")
  ViewerDatabase retrieve(@PathParam("databaseUUID") String databaseUUID, @PathParam("id") String id);

  @DELETE
  @Path("/delete/{databaseUUID}")
  @ApiOperation(value = "retrieves DBPTK export modules", notes = "", response = Boolean.class, responseContainer = "IndexResult")
  Boolean deleteDatabase(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("/delete/rows/{databaseUUID}")
  @ApiOperation(value = "retrieves DBPTK export modules", notes = "", response = Boolean.class, responseContainer = "IndexResult")
  Boolean deleteSolrData(@PathParam("databaseUUID") String databaseUUID);


  @POST
  @Path("find/rows/{databaseUUID}")
  @ApiOperation(value = "retrieves DBPTK export modules", notes = "", response = IndexResult.class, responseContainer = "IndexResult")
  IndexResult<ViewerRow> findRows(@PathParam("databaseUUID") String databaseUUID,
                                  @ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
                                  @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);


  @GET
  @Path("find/rows/{databaseUUID}/{rowUUID}")
  @ApiOperation(value = "retrieves DBPTK export modules", notes = "", response = ViewerRow.class, responseContainer = "IndexResult")
  ViewerRow retrieveRows(@PathParam("databaseUUID") String databaseUUID, @PathParam("rowUUID") String rowUUID);
}
