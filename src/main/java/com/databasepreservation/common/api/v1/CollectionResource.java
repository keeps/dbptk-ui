package com.databasepreservation.common.api.v1;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.stereotype.Service;

import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.DownloadUtils;
import com.databasepreservation.common.api.utils.StreamResponse;
import com.databasepreservation.common.api.utils.ViewerStreamingOutput;
import com.databasepreservation.common.api.v1.utils.IterableIndexResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ZipOutputStream;
import com.databasepreservation.common.api.v1.utils.ZipOutputStreamSingleRow;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.exceptions.SavedSearchException;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.LobPathManager;
import com.databasepreservation.common.utils.UserUtility;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_DATABASE)
public class CollectionResource implements CollectionService {
  @Context
  private HttpServletRequest request;

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/report")
  @ApiOperation(value = "Downloads the migration report for a specific database")
  public Response getReport(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      java.nio.file.Path reportPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID);
      String filename = reportPath.getFileName().toString();
      if (!Files.exists(reportPath)) {
        throw new NotFoundException("Missing report file: " + filename);
      }

      InputStream reportStream = Files.newInputStream(reportPath);

      return ApiUtils.okResponse(DownloadUtils.getReportResourceStreamResponse(reportPath, reportStream));
    } catch (NotFoundException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
  }

  @Override
  public String createCollection(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      return SIARDController.loadFromLocal(database.getPath(), databaseUUID);
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public ProgressData getProgressData(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);
    try {
      return ProgressData.getInstance(databaseUUID);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public Boolean deleteCollection(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      final String collectionName = SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
      if (SolrClientFactory.get().deleteCollection(collectionName)) {
        Filter savedSearchFilter = new Filter(new SimpleFilterParameter(SOLR_SEARCHES_DATABASE_UUID, databaseUUID));
        SolrUtils.delete(ViewerFactory.getSolrClient(), SolrDefaultCollectionRegistry.get(SavedSearch.class),
          savedSearchFilter);

        ViewerFactory.getSolrManager().markDatabaseCollection(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
        return true;
      }
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
    return false;
  }

  /*******************************************************************************
   * Collection Resource - Config Sub-resource
   ******************************************************************************/
  @Override
  public List<CollectionStatus> getCollectionConfiguration(String databaseUUID, String collectionUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      final java.nio.file.Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
      final java.nio.file.Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);
      final java.nio.file.Path collectionStatusFile = databaseDirectoryPath
        .resolve(ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + collectionUUID + ".json");
      return Collections.singletonList(JsonUtils.readObjectFromFile(collectionStatusFile, CollectionStatus.class));
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public Boolean updateCollectionConfiguration(String databaseUUID, String collectionUUID, CollectionStatus status) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      ViewerFactory.getConfigurationManager().updateCollectionStatus(databaseUUID, status);
    } catch (ViewerException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }

    return true;
  }

  /*******************************************************************************
   * Collection Resource - Config Sub-resource - Denormalization Sub-resource
   ******************************************************************************/
  @Override
  public DenormalizeConfiguration getDenormalizeConfigurationFile(String databaseUUID, String collectionUUID,
    String tableUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      java.nio.file.Path path = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
        .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION);
      if (Files.exists(path)) {
        return JsonTransformer.readObjectFromFile(path, DenormalizeConfiguration.class);
      } else {
        ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
        ViewerTable table = database.getMetadata().getTable(tableUUID);
        return new DenormalizeConfiguration(databaseUUID, table);
      }
    } catch (ViewerException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public Boolean createDenormalizeConfigurationFile(String databaseUUID, String collectionUUID, String tableUUID,
    DenormalizeConfiguration configuration) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      JsonTransformer.writeObjectToFile(configuration,
        ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
          .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION));
      ViewerFactory.getConfigurationManager().addDenormalization(databaseUUID,
        ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID);
    } catch (GenericException | ViewerException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
    return true;
  }

  @Override
  public Boolean deleteDenormalizeConfigurationFile(String databaseUUID, String collectionUUID, String tableUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      ViewerFactory.getConfigurationManager().removeDenormalization(databaseUUID,
        ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID);
      java.nio.file.Path path = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
        .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION);
      if (Files.exists(path)) {
        Files.delete(path);
      }
    } catch (GenericException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
    return true;
  }

  @Override
  public Boolean run(String databaseUUID, String collectionUUID, String tableUUID) {
    return true;
  }

  /*******************************************************************************
   * Collection Resource - Data Sub-resource
   ******************************************************************************/
  @Override
  public IndexResult<ViewerRow> findRows(String databaseUUID, String collectionUUID, String schema, String table,
    FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    long count = 0;

    try {
      final IndexResult<ViewerRow> viewerRowIndexResult = ViewerFactory.getSolrManager().findRows(databaseUUID,
        FilterUtils.filterByTable(findRequest.filter, schema + "." + table), findRequest.sorter, findRequest.sublist,
        findRequest.facets, findRequest.fieldsToReturn, findRequest.extraParameters);
      count = viewerRowIndexResult.getTotalCount();
      return viewerRowIndexResult;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_FILTER_PARAM, JsonUtils.getJsonFromObject(findRequest.filter),
        ViewerConstants.CONTROLLER_SUBLIST_PARAM, JsonUtils.getJsonFromObject(findRequest.sublist),
        ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  @Override
  public ViewerRow retrieveRow(String databaseUUID, String collectionUUID, String schema, String table,
    String rowIndex) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      final ViewerRow viewerRow = ViewerFactory.getSolrManager().retrieveRows(databaseUUID, rowIndex);
      if (viewerRow.getTableId().equals(schema + "." + table)) {
        return viewerRow;
      } else {
        throw new NotFoundException("Row not found");
      }
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_ROW_ID_PARAM, rowIndex);
    }
  }

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{rowIndex}/{columnIndex}")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Downloads a LOB for a specific row within a database", notes = "download the specified LOB.", response = Response.class)
  public Response exportLOB(@PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @PathParam(ViewerConstants.API_PATH_PARAM_COLLECTION_UUID) String collectionUUID,
    @PathParam("schema") String schema, @PathParam("table") String table, @PathParam("rowIndex") String rowIndex,
    @PathParam("columnIndex") Integer columnIndex,
    @QueryParam(ViewerConstants.API_PATH_PARAM_LOB_FILENAME) String filename) {

    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    try {
      ViewerRow row = solrManager.retrieveRows(databaseUUID, rowIndex);
      if (row != null && row.getTableId().equals(schema + "." + table)) {
        try {
          return ApiUtils.okResponse(new StreamResponse(filename, MediaType.APPLICATION_OCTET_STREAM,
            DownloadUtils.stream(Files.newInputStream(LobPathManager.getPath(ViewerFactory.getViewerConfiguration(),
              databaseUUID, row.getTableId(), columnIndex, rowIndex)))));
        } catch (IOException e) {
          throw new GenericException("There was an IO problem retrieving the LOB.", e);
        }
      } else {
        throw new NotFoundException("LOB not found.");
      }
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, schema + "." + table, ViewerConstants.CONTROLLER_ROW_ID_PARAM,
        rowIndex, ViewerConstants.CONTROLLER_COLUMN_ID_PARAM, columnIndex, ViewerConstants.CONTROLLER_FILENAME_PARAM,
        filename);
    }
  }

  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/find/export")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Export the rows as CSV", notes = "", response = Response.class)
  public Response exportFindToCSV(
    @ApiParam(value = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @ApiParam(value = "The collection unique identifier", required = true) @PathParam("collectionUUID") String collectionUUID,
    @ApiParam(value = "The schema name", required = true) @PathParam("schema") String schema,
    @ApiParam(value = "The table name", required = true) @PathParam("table") String table,
    @ApiParam(value = "Find request to filter/limit the search") @QueryParam("f") String findRequestJson,
    @ApiParam(value = "The CSV filename") @QueryParam("filename") String filename,
    @ApiParam(value = "The Zip filename") @QueryParam("zipFilename") String zipFilename,
    @ApiParam(value = "Export description", allowableValues = "true, false") @QueryParam("descriptions") boolean exportDescription,
    @ApiParam(value = "Export LOBs", allowableValues = "true, false") @QueryParam("lobs") boolean exportLobs) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    FindRequest findRequest = null;

    try {
      findRequest = JsonUtils.getObjectFromJson(findRequestJson, FindRequest.class);

      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      final ViewerTable viewerTable = database.getMetadata().getTableById(schema + "." + table);

      FilterUtils.filterByTable(findRequest.filter, viewerTable.getId());

      if (Boolean.FALSE.equals(exportLobs) && StringUtils.isBlank(zipFilename)) {
        return handleCSVExport(solrManager, databaseUUID, viewerTable, findRequest, filename, exportDescription);
      } else {
        return handleCSVExportWithLobs(solrManager, databaseUUID, viewerTable, findRequest, zipFilename, filename,
          exportDescription);
      }
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      if (findRequest != null) {
        Object[] list = new Object[] {ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
          ViewerConstants.CONTROLLER_TABLE_ID_PARAM, schema + "." + table, ViewerConstants.CONTROLLER_FILTER_PARAM,
          JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_EXPORT_DESCRIPTIONS_PARAM,
          exportDescription, ViewerConstants.CONTROLLER_EXPORT_LOBS_PARAM, exportLobs,
          ViewerConstants.CONTROLLER_FILENAME_PARAM, filename, ViewerConstants.CONTROLLER_SUBLIST_PARAM,
          findRequest.sublist == null ? JsonUtils.getJsonFromObject(Sublist.NONE)
            : JsonUtils.getJsonFromObject(findRequest.sublist)};

        if (StringUtils.isNotBlank(zipFilename)) {
          list = appendValue(list, ViewerConstants.CONTROLLER_ZIP_FILENAME_PARAM);
          list = appendValue(list, zipFilename);
        }
        // register action
        controllerAssistant.registerAction(user, state, list);
      }
    }
  }

  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{rowIndex}/export")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Export the a single row as CSV", notes = "", response = Response.class)
  public Response exportSingleRowToCSV(
    @ApiParam(value = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @ApiParam(value = "The collection unique identifier", required = true) @PathParam("collectionUUID") String collectionUUID,
    @ApiParam(value = "The schema name", required = true) @PathParam("schema") String schema,
    @ApiParam(value = "The table name", required = true) @PathParam("table") String table,
    @ApiParam(value = "The index of the row", required = true) @PathParam("rowIndex") String rowIndex,
    @ApiParam(value = "The CSV filename", required = true) @QueryParam("filename") String filename,
    @ApiParam(value = "The Zip filename") @QueryParam("zipFilename") String zipFilename,
    @ApiParam(value = "Export description", allowableValues = "true, false", required = true) @QueryParam("descriptions") boolean exportDescription,
    @ApiParam(value = "Export LOBs", allowableValues = "true, false", required = true) @QueryParam("lobs") boolean exportLobs) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    controllerAssistant.checkRoles(user);

    try {
      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      final ViewerTable viewerTable = database.getMetadata().getTableById(schema + "." + table);
      final ViewerRow viewerRow = solrManager.retrieveRows(databaseUUID, rowIndex);

      if (viewerRow != null && viewerRow.getTableId().equals(schema + "." + table)) {
        if (Boolean.FALSE.equals(exportLobs) && StringUtils.isBlank(zipFilename)) {
          return handleSingleCSVExportWithoutLOBs(databaseUUID, viewerTable, viewerRow, filename, exportDescription);
        } else {
          return handleSingleCSVExportWithLOBs(databaseUUID, viewerTable, viewerRow, filename, zipFilename,
            exportDescription);
        }
      } else {
        throw new NotFoundException("Table not found.");
      }
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      Object[] list = new Object[] {ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, schema + "." + table, ViewerConstants.CONTROLLER_ROW_ID_PARAM,
        rowIndex, ViewerConstants.CONTROLLER_EXPORT_DESCRIPTIONS_PARAM, exportDescription,
        ViewerConstants.CONTROLLER_EXPORT_LOBS_PARAM, exportLobs, ViewerConstants.CONTROLLER_FILENAME_PARAM, filename};

      if (StringUtils.isNotBlank(zipFilename)) {
        list = appendValue(list, ViewerConstants.CONTROLLER_ZIP_FILENAME_PARAM);
        list = appendValue(list, zipFilename);
      }
      // register action
      controllerAssistant.registerAction(user, state, list);
    }
  }

  private Response handleSingleCSVExportWithoutLOBs(String databaseUUID, ViewerTable table, ViewerRow row,
    String filename, boolean exportDescriptions) throws GenericException {
    final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
      .getConfigurationCollection(databaseUUID, databaseUUID);

    final List<String> fieldsToReturn = configurationCollection.getFieldsToReturn(table.getId());
    return ApiUtils.okResponse(new ViewerStreamingOutput(
      new ResultsCSVOutputStream(row, table, fieldsToReturn, filename, exportDescriptions, ',')).toStreamResponse());
  }

  private Response handleSingleCSVExportWithLOBs(String databaseUUID, ViewerTable table, ViewerRow row, String filename,
    String zipFilename, boolean exportDescriptions) throws GenericException {
    final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
      .getConfigurationCollection(databaseUUID, databaseUUID);

    final List<String> fieldsToReturn = configurationCollection.getFieldsToReturn(table.getId());
    return ApiUtils.okResponse(new StreamResponse(new ZipOutputStreamSingleRow(databaseUUID, table, row, zipFilename,
      filename, fieldsToReturn, exportDescriptions)));
  }

  private Response handleCSVExport(DatabaseRowsSolrManager solrManager, final String databaseUUID,
    final ViewerTable table, final FindRequest findRequest, final String filename, final boolean exportDescriptions)
    throws GenericException, RequestNotValidException {
    if (findRequest.sublist == null) {
      final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
        findRequest.fieldsToReturn);
      return ApiUtils.okResponse(new ViewerStreamingOutput(new IterableIndexResultsCSVOutputStream(allRows, table,
        findRequest.fieldsToReturn, filename, exportDescriptions, ',')).toStreamResponse());
    } else {
      final IndexResult<ViewerRow> rows = solrManager.findRows(databaseUUID, findRequest.filter, findRequest.sorter,
        findRequest.sublist, null, findRequest.fieldsToReturn);

      return ApiUtils.okResponse(new ViewerStreamingOutput(
        new ResultsCSVOutputStream(rows, table, findRequest.fieldsToReturn, filename, exportDescriptions, ','))
          .toStreamResponse());
    }
  }

  private Response handleCSVExportWithLobs(DatabaseRowsSolrManager solrManager, final String databaseUUID,
    final ViewerTable table, final FindRequest findRequest, final String zipFilename, final String filename,
    final boolean exportDescription) {
    List<String> fields = findRequest.fieldsToReturn;
    fields.add(ViewerConstants.INDEX_ID);
    final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields);
    final IterableIndexResult clone = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields);
    fields.remove(ViewerConstants.INDEX_ID);
    return ApiUtils.okResponse(new StreamResponse(new ZipOutputStream(databaseUUID, table, allRows, clone, zipFilename,
      filename, findRequest.fieldsToReturn, findRequest.sublist, exportDescription)));
  }

  private Object[] appendValue(Object[] obj, Object newObj) {
    ArrayList<Object> temp = new ArrayList<>(Arrays.asList(obj));
    temp.add(newObj);
    return temp.toArray();
  }

  /*******************************************************************************
   * Collection Resource - SavedSearch Sub-resource
   ******************************************************************************/
  @Override
  public String saveSavedSearch(String databaseUUID, String collectionUUID, String tableUUID, String name,
    String description, SearchInfo searchInfo) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    String searchInfoJson = JsonUtils.getJsonFromObject(searchInfo);

    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setUuid(SolrUtils.randomUUID());
    savedSearch.setName(name);
    savedSearch.setDescription(description);
    savedSearch.setDatabaseUUID(databaseUUID);
    savedSearch.setTableUUID(tableUUID);
    savedSearch.setTableName(tableUUID);
    savedSearch.setSearchInfoJson(searchInfoJson);

    try {
      ViewerFactory.getSolrManager().addSavedSearch(savedSearch);
      return savedSearch.getUuid();
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_NAME_PARAM, name,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_DESCRIPTION_PARAM, description,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_PARAM, JsonUtils.getJsonFromObject(savedSearch));
    }
  }

  @Override
  public IndexResult<SavedSearch> findSavedSearches(String databaseUUID, String collectionUUID, FindRequest findRequest,
    String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);
    long count = 0;

    try {
      final IndexResult<SavedSearch> savedSearchIndexResult = ViewerFactory.getSolrManager().find(SavedSearch.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
      count = savedSearchIndexResult.getTotalCount();
      return savedSearchIndexResult;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_FILTER_PARAM, JsonUtils.getJsonFromObject(findRequest.filter),
        ViewerConstants.CONTROLLER_SUBLIST_PARAM, JsonUtils.getJsonFromObject(findRequest.sublist),
        ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  @Override
  public SavedSearch retrieveSavedSearch(String databaseUUID, String collectionUUID, String savedSearchUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM, savedSearchUUID);
    }
  }

  @Override
  public void updateSavedSearch(String databaseUUID, String collectionUUID, String savedSearchUUID, String name,
    String description) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      ViewerFactory.getSolrManager().editSavedSearch(databaseUUID, savedSearchUUID, name, description);
    } catch (SavedSearchException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM, savedSearchUUID,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_NAME_PARAM, name,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_DESCRIPTION_PARAM, description);
    }
  }

  @Override
  public void deleteSavedSearch(String databaseUUID, String collectionUUID, String savedSearchUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      ViewerFactory.getSolrManager().deleteSavedSearch(savedSearchUUID);
    } catch (SavedSearchException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM, savedSearchUUID);
    }
  }
}
