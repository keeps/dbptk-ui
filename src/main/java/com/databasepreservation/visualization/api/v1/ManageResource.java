package com.databasepreservation.visualization.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.api.utils.ApiResponseMessage;
import com.databasepreservation.visualization.api.utils.ApiUtils;
import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.server.SIARDController;
import com.databasepreservation.visualization.server.ViewerConfiguration;
import com.databasepreservation.visualization.server.ViewerFactory;
import com.databasepreservation.visualization.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.visualization.shared.ViewerConstants;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.utils.UserUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Resource used to manage databases in the viewer. This resource should be
 * protected (via OnOffFilter) and only accessible from specific IP addresses
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@Service
@Path(ManageResource.ENDPOINT)
@Api(value = ManageResource.SWAGGER_ENDPOINT)
public class ManageResource {
  public static final String ENDPOINT = ViewerConstants.API_V1_MANAGE_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 manage";

  private static Logger LOGGER = LoggerFactory.getLogger(ManageResource.class);

  @Context
  private HttpServletRequest request;

  @DELETE
  @Path("/database/{" + ViewerConstants.API_PATH_PARAM_DATABASE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete database", notes = "Removes a database from the visualization system.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response deleteDatabase(
    @ApiParam(value = "The database uuid", required = true) @PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = ViewerConstants.API_DELETE_MEDIA_TYPES) @QueryParam(ViewerConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // check for authorization, to protect against unauthorized access attempts
    UserUtility.Authorization.checkDatabaseManagementPermission(request);

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    if (database != null) {
      try {
        solrManager.removeDatabase(database, ViewerConfiguration.getInstance().getLobPath());
      } catch (ViewerException e) {
        throw new RODAException("Error deleting the database", e);
      }
    } else {
      throw new NotFoundException("Database not found");
    }

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Database deleted"), mediaType).build();
  }

  @POST
  @Path("/database/{" + ViewerConstants.API_PATH_PARAM_DATABASE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Add database", notes = "Creates a database in the visualization system.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response createDatabase(
    @ApiParam(value = "The database uuid", required = true) @PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @ApiParam(value = "Local filename (absolute or relative to manage.upload.basePath)", required = true) @QueryParam(ViewerConstants.API_FILE) String fileName,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = ViewerConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(ViewerConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // check for authorization, to protect against unauthorized access attempts
    UserUtility.Authorization.checkDatabaseManagementPermission(request);

    // migrate siard
    new Thread(() -> {
      try {
        SIARDController.loadFromLocal(fileName, databaseUUID);
      } catch (GenericException e) {
        LOGGER.error("Could not create database", e);
      }
    }).start();

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Database being created"), mediaType).build();
  }
}
