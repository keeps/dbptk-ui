package com.databasepreservation.common.api.v1;

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

import org.apache.commons.lang.StringUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.StreamResponse;
import com.databasepreservation.common.api.utils.ViewerStreamingOutput;
import com.databasepreservation.common.api.v1.utils.IterableIndexResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ZipOutputStream;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.index.ExportRequest;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Resource used to export search results
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@Service
@Path(ExportsResource.ENDPOINT)
@Api(value = ExportsResource.SWAGGER_ENDPOINT)
public class ExportsResource {
  public static final String ENDPOINT = "/" + ViewerConstants.API_SERVLET + ViewerConstants.API_V1_EXPORT_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 exports";

  private static Logger LOGGER = LoggerFactory.getLogger(ExportsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/csv/{" + ViewerConstants.API_PATH_PARAM_DATABASE_UUID + "}/{" + ViewerConstants.API_PATH_PARAM_TABLE_UUID
    + "}")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Export as CSV", notes = "Export query results as CSV.", response = String.class, responseContainer = "CSVExport")
  public Response getCSVResultsPost(@PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @QueryParam(ViewerConstants.API_QUERY_PARAM_EXPORT) String paramExportRequest,
    @QueryParam(ViewerConstants.API_QUERY_PARAM_FILTER) String paramFindRequest,
    @PathParam(ViewerConstants.API_PATH_PARAM_TABLE_UUID) String tableUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    controllerAssistant.checkRoles(user);

    FindRequest findRequest = null;
    ExportRequest exportRequest = null;

    try {
      findRequest = JsonUtils.getObjectFromJson(paramFindRequest, FindRequest.class);
      exportRequest = JsonUtils.getObjectFromJson(paramExportRequest, ExportRequest.class);

      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      final ViewerTable table = database.getMetadata().getTable(tableUUID);

      if (Boolean.FALSE.equals(exportRequest.exportLOBs) && StringUtils.isBlank(exportRequest.zipFilename)) {
        if (findRequest.sublist == null) {
          final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter,
            findRequest.sorter, findRequest.fieldsToReturn);
          return ApiUtils.okResponse(new ViewerStreamingOutput(new IterableIndexResultsCSVOutputStream(allRows, table,
            findRequest.fieldsToReturn, exportRequest.filename, exportRequest.exportDescription, ','))
              .toStreamResponse());
        } else {
          final IndexResult<ViewerRow> rows = solrManager.findRows(databaseUUID, findRequest.filter, findRequest.sorter,
            findRequest.sublist, null, findRequest.fieldsToReturn);

          return ApiUtils
            .okResponse(new ViewerStreamingOutput(new ResultsCSVOutputStream(rows, table, findRequest.fieldsToReturn,
              exportRequest.filename, exportRequest.exportDescription, ',')).toStreamResponse());
        }
      } else {
        List<String> fields = findRequest.fieldsToReturn;
        fields.add(ViewerConstants.INDEX_ID);
        final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter,
          findRequest.sorter, fields);
        final IterableIndexResult clone = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
          fields);
        fields.remove(ViewerConstants.INDEX_ID);
        return ApiUtils.okResponse(new StreamResponse(new ZipOutputStream(databaseUUID, table, allRows, clone,
          exportRequest.zipFilename, exportRequest.filename, findRequest.fieldsToReturn, findRequest.sublist)));
      }
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      if (findRequest != null && exportRequest != null) {
        // register action
        controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
          ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID,
          ViewerConstants.CONTROLLER_FILTER_PARAM, findRequest.filter.toString(),
          ViewerConstants.CONTROLLER_SUBLIST_PARAM,
          findRequest.sublist == null ? Sublist.NONE.toString() : findRequest.sublist.toString(),
          ViewerConstants.CONTROLLER_EXPORT_PARAM, exportRequest.toString());
      }
    }
  }
}
