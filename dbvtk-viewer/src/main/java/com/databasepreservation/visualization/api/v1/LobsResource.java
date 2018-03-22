package com.databasepreservation.visualization.api.v1;

import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.api.utils.ApiUtils;
import com.databasepreservation.visualization.api.utils.DownloadUtils;
import com.databasepreservation.visualization.api.utils.StreamResponse;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.server.ViewerFactory;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.utils.LobPathManager;
import com.databasepreservation.visualization.utils.SolrManager;
import com.databasepreservation.visualization.utils.UserUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Resource used to export search results
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@Path(LobsResource.ENDPOINT)
@Api(value = LobsResource.SWAGGER_ENDPOINT)
public class LobsResource {
  public static final String ENDPOINT = ViewerSafeConstants.API_V1_LOBS_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 lobs";

  private static Logger LOGGER = LoggerFactory.getLogger(LobsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/{" + ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID + "}/{" + ViewerSafeConstants.API_PATH_PARAM_TABLE_UUID
    + "}/{" + ViewerSafeConstants.API_PATH_PARAM_ROW_UUID + "}/{" + ViewerSafeConstants.API_PATH_PARAM_COLUMN_ID + "}")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Download LOB", notes = "download the specified LOB.", response = String.class, responseContainer = "LOB")
  public Response getLOB(@PathParam(ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @PathParam(ViewerSafeConstants.API_PATH_PARAM_TABLE_UUID) String tableUUID,
    @PathParam(ViewerSafeConstants.API_PATH_PARAM_ROW_UUID) String rowUUID,
    @PathParam(ViewerSafeConstants.API_PATH_PARAM_COLUMN_ID) Integer columnID) throws RODAException {
    SolrManager solrManager = ViewerFactory.getSolrManager();

    UserUtility.Authorization.checkDatabaseAccessPermission(this.request, databaseUUID);

    ViewerRow row = solrManager.retrieveRows(ViewerRow.class, databaseUUID, rowUUID);

    if (row != null) {
      String fileName = rowUUID + "-" + columnID + ".bin";
      try {
        return ApiUtils.okResponse(new StreamResponse(fileName, MediaType.APPLICATION_OCTET_STREAM,
          DownloadUtils.stream(Files.newInputStream(LobPathManager.getPath(ViewerFactory.getViewerConfiguration(),
            databaseUUID, tableUUID, columnID, rowUUID)))));
      } catch (IOException e) {
        throw new RODAException("There was an IO problem retrieving the LOB.", e);
      }
    } else {
      throw new NotFoundException("LOB not found.");
    }
  }
}
