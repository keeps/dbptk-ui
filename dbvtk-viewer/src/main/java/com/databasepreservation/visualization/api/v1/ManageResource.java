package com.databasepreservation.visualization.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.api.utils.ApiResponseMessage;
import com.databasepreservation.visualization.api.utils.ApiUtils;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.server.ViewerFactory;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.utils.SolrManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Resource used to manage databases in the viewer
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@Path(ManageResource.ENDPOINT)
@Api(value = ManageResource.SWAGGER_ENDPOINT)
public class ManageResource {
  public static final String ENDPOINT = ViewerSafeConstants.API_V1_MANAGE_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 manage";

  private static Logger LOGGER = LoggerFactory.getLogger(ManageResource.class);

  @Context
  private HttpServletRequest request;

  @DELETE
  @Path("/database/{" + ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete database", notes = "Removes a database from the visualization system.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response deleteDatabase(
    @ApiParam(value = "The database uuid", required = true) @PathParam(ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = ViewerSafeConstants.API_DELETE_MEDIA_TYPES) @QueryParam(ViewerSafeConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException, NotFoundException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    SolrManager solrManager = ViewerFactory.getSolrManager();

    ViewerDatabase database = solrManager.retrieve(null, ViewerDatabase.class, databaseUUID);
    if (database != null) {
      try {
        solrManager.removeDatabase(database);
      } catch (ViewerException e) {
        throw new RODAException("Error deleting the database", e);
      }
    }

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Database deleted"), mediaType).build();
  }
}
