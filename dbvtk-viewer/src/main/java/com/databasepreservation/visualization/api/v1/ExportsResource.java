package com.databasepreservation.visualization.api.v1;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.exceptions.RODAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.ViewerConstants;
import com.databasepreservation.visualization.api.utils.ApiUtils;
import com.databasepreservation.visualization.api.utils.DownloadUtils;
import com.databasepreservation.visualization.api.utils.StreamResponse;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.visualization.shared.ViewerFactory;
import com.databasepreservation.visualization.utils.SolrManager;
import com.databasepreservation.visualization.utils.SolrUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Resource used to export search results
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@Path(ExportsResource.ENDPOINT)
@Api(value = ExportsResource.SWAGGER_ENDPOINT)
public class ExportsResource {
  public static final String ENDPOINT = "/v1/exports";
  public static final String SWAGGER_ENDPOINT = "v1 exports";

  private static Logger LOGGER = LoggerFactory.getLogger(ExportsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/{" + ViewerConstants.API_PATH_PARAM_DATABASE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get metadata", notes = "Gets database metadata.", response = ViewerMetadata.class, responseContainer = "Metadata")
  public Response getMetadata(@PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @QueryParam(ViewerConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    // RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    // Jobs jobs = JobsHelper.getJobsFromIndexResult(user, start, limit);
    ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(null, ViewerDatabase.class, databaseUUID);

    return Response.ok(database.getMetadata(), mediaType).build();
  }

  @GET
  @Path("/csv/{" + ViewerConstants.API_PATH_PARAM_TABLE_UUID + "}")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Export as CSV", notes = "Export query results as CSV.", response = String.class, responseContainer = "CSVExport")
  public Response getCSVResults(@PathParam(ViewerConstants.API_PATH_PARAM_TABLE_UUID) String tableUUID,
    @QueryParam("q") String query, @QueryParam("fq") String filterQuery, @QueryParam("fl") String fields,
    @QueryParam("sort") String sort, @QueryParam("start") String start, @QueryParam("rows") String rows)
    throws RODAException {
    // delegate action to controller
    // Jobs jobs = JobsHelper.getJobsFromIndexResult(user, start, limit);

    if (StringUtils.isBlank(fields)) {
      fields = "col*";
    }

    SolrManager solrManager = ViewerFactory.getSolrManager();
    String collection = SolrUtils.getTableCollectionName(tableUUID);

    InputStream rowsCSV = solrManager.findRowsCSV(null, collection, query, filterQuery, fields, sort, start, rows);

    return ApiUtils.okResponse(new StreamResponse("file.csv", MediaType.APPLICATION_OCTET_STREAM, DownloadUtils
      .stream(rowsCSV)));
  }
}
