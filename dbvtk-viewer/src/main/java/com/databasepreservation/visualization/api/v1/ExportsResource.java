package com.databasepreservation.visualization.api.v1;

import java.io.InputStream;
import java.util.List;

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
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.api.utils.ApiUtils;
import com.databasepreservation.visualization.api.utils.DownloadUtils;
import com.databasepreservation.visualization.api.utils.StreamResponse;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.server.ViewerFactory;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.utils.SolrManager;
import com.databasepreservation.visualization.utils.UserUtility;

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
  public static final String ENDPOINT = ViewerSafeConstants.API_V1_EXPORT_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 exports";

  private static Logger LOGGER = LoggerFactory.getLogger(ExportsResource.class);

  @Context
  private HttpServletRequest request;

  // @GET
  // @Path("/{" + ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID + "}")
  // @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  // @ApiOperation(value = "Get metadata", notes = "Gets database metadata.",
  // response = ViewerMetadata.class, responseContainer = "Metadata")
  // public Response
  // getMetadata(@PathParam(ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID)
  // String databaseUUID,
  // @QueryParam(ViewerSafeConstants.API_QUERY_KEY_ACCEPT_FORMAT) String
  // acceptFormat) throws RODAException {
  // String mediaType = ApiUtils.getMediaType(acceptFormat, request);
  //
  // // get user
  // // RodaUser user = UserUtility.getApiUser(request);
  //
  // // delegate action to controller
  // // Jobs jobs = JobsHelper.getJobsFromIndexResult(user, start, limit);
  // ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(null,
  // ViewerDatabase.class, databaseUUID);
  //
  // return Response.ok(database.getMetadata(), mediaType).build();
  // }

  @GET
  @Path("/csv/{" + ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID + "}/{"
    + ViewerSafeConstants.API_PATH_PARAM_TABLE_UUID + "}")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Export as CSV", notes = "Export query results as CSV.", response = String.class, responseContainer = "CSVExport")
  public Response getCSVResultsPost(@PathParam(ViewerSafeConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @PathParam(ViewerSafeConstants.API_PATH_PARAM_TABLE_UUID) String tableUUID,
    @QueryParam(ViewerSafeConstants.API_QUERY_PARAM_FILTER) String filterParam,
    @QueryParam(ViewerSafeConstants.API_QUERY_PARAM_FIELDS) String fieldsListParam,
    @QueryParam(ViewerSafeConstants.API_QUERY_PARAM_SORTER) String sorterParam,
    @QueryParam(ViewerSafeConstants.API_QUERY_PARAM_SUBLIST) String subListParam) throws RODAException {
    // delegate action to controller
    // Jobs jobs = JobsHelper.getJobsFromIndexResult(user, start, limit);

    SolrManager solrManager = ViewerFactory.getSolrManager();

    Filter filter = JsonUtils.getObjectFromJson(filterParam, Filter.class);
    List<String> fields = JsonUtils.getListFromJson(fieldsListParam, String.class);
    Sorter sorter = JsonUtils.getObjectFromJson(sorterParam, Sorter.class);
    Sublist sublist = null;
    if (StringUtils.isNotBlank(subListParam)) {
      sublist = JsonUtils.getObjectFromJson(subListParam, Sublist.class);
    }

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    UserUtility.Authorization.checkTableAccessPermission(this.request, database, tableUUID);

    // TODO: use viewerTable to convert solrColumnNames into displayColumnNames
    InputStream rowsCSV = solrManager.findRowsCSV(tableUUID, filter, sorter, sublist, fields);

    return ApiUtils.okResponse(new StreamResponse("file.csv", MediaType.APPLICATION_OCTET_STREAM, DownloadUtils
      .stream(rowsCSV)));
  }
}
