package com.databasepreservation.common.api.v1;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.DownloadUtils;
import com.databasepreservation.common.api.utils.StreamResponse;
import com.databasepreservation.common.api.utils.ViewerStreamingOutput;
import com.databasepreservation.common.api.v1.utils.IterableIndexResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ZipOutputStream;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.exceptions.SavedSearchException;
import com.databasepreservation.common.client.index.FindNestedRequest;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.models.progress.ValidationProgressData;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.DatabaseService;
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
public class DatabaseResource implements DatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResource.class);
  @Context
  private HttpServletRequest request;

  @Override
  public String createCollection(String databaseUUID) throws RESTException {
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
  public ValidationProgressData getValidationProgressData(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ValidationProgressData.getInstance(databaseUUID);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public IndexResult<ViewerDatabase> findDatabases(FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    if (ViewerConfiguration.getInstance().getApplicationEnvironment().equals(ViewerConstants.SERVER)) {
      if (user.isAdmin()) {
        return getViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
      } else {
        List<String> fieldsToReturn = new ArrayList<>();
        fieldsToReturn.add(ViewerConstants.INDEX_ID);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA);
        FilterParameter parameter = new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS,
          ViewerDatabaseStatus.AVAILABLE.name());
        Filter databasesReadyFilter = new Filter(parameter);
        FindRequest request = new FindRequest(findRequest.classToReturn, databasesReadyFilter, findRequest.sorter,
          findRequest.sublist, findRequest.facets, findRequest.exportFacets, fieldsToReturn);
        return getViewerDatabaseIndexResult(request, fieldsToReturn, controllerAssistant, user, state);
      }
    } else {
      return getViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
    }
  }

  @Override
  public String createDatabase(String path) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      return SIARDController.loadMetadataFromLocal(path);
    } catch (GenericException e) {
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  private IndexResult<ViewerDatabase> getViewerDatabaseIndexResult(FindRequest findRequest,
    ControllerAssistant controllerAssistant, User user, LogEntryState state) {
    long count = 0;
    try {
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
      count = result.getTotalCount();
      return result;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
        JsonUtils.getJsonFromObject(findRequest.sublist), ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  private IndexResult<ViewerDatabase> getViewerDatabaseIndexResult(FindRequest findRequest, List<String> fieldsToReturn,
    ControllerAssistant controllerAssistant, User user, LogEntryState state) {
    long count = 0;
    try {
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, fieldsToReturn);
      count = result.getTotalCount();
      return result;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
        JsonUtils.getJsonFromObject(findRequest.sublist), ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  @Override
  public ViewerDatabase retrieve(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
  }

  @Override
  public Boolean deleteDatabase(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);

    try {
      return SIARDController.deleteAll(databaseUUID);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
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
   * Collection Resource - Table Sub-resource
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

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/tables/{tableUUID}/lob")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Downloads a LOB for a specific row within a database", notes = "download the specified LOB.", response = Response.class)
  public Response getLOB(@PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
                         @PathParam(ViewerConstants.API_PATH_PARAM_COLLECTION_UUID) String collectionUUID,
                         @PathParam(ViewerConstants.API_PATH_PARAM_TABLE_UUID) String tableUUID,
                         @QueryParam(ViewerConstants.API_PATH_PARAM_ROW_UUID) String rowUUID,
                         @QueryParam(ViewerConstants.API_PATH_PARAM_COLUMN_ID) Integer columnID,
                         @QueryParam(ViewerConstants.API_PATH_PARAM_LOB_FILENAME) String filename) {

    ControllerAssistant controllerAssistant = new ControllerAssistant() {
    };
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    try {
      ViewerRow row = solrManager.retrieveRows(databaseUUID, rowUUID);
      if (row != null) {
        try {
          return ApiUtils.okResponse(new StreamResponse(filename, MediaType.APPLICATION_OCTET_STREAM,
              DownloadUtils.stream(Files.newInputStream(LobPathManager.getPath(ViewerFactory.getViewerConfiguration(),
                  databaseUUID, tableUUID, columnID, rowUUID)))));
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
          ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID, ViewerConstants.CONTROLLER_ROW_ID_PARAM, rowUUID,
          ViewerConstants.CONTROLLER_COLUMN_ID_PARAM, columnID, ViewerConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }

  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/tables/{tableUUID}/export")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Export the row or rows as CSV", notes = "", response = Response.class)
  public Response exportToCSV(
    @ApiParam(value = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @ApiParam(value = "The collection unique identifier", required = true) @PathParam("collectionUUID") String collectionUUID,
    @ApiParam(value = "The table unique identifier", required = true) @PathParam("tableUUID") String tableUUID,
    @ApiParam(value = "Find request to filter/limit the search") @QueryParam("f") String findRequestJson,
    @ApiParam(value = "The CSV filename") @QueryParam("filename") String filename,
    @ApiParam(value = "The Zip filename") @QueryParam("zipFilename") String zipFilename,
    @ApiParam(value = "Export description", allowableValues = "true, false") @QueryParam("descriptions") boolean exportDescription,
    @ApiParam(value = "Export LOBs", allowableValues = "true, false") @QueryParam("lobs") boolean exportLobs,
    @ApiParam(value = "Export only one record", allowableValues = "true, false") @QueryParam("singleRecord") boolean record) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    controllerAssistant.checkRoles(user);

    FindRequest findRequest = null;

    try {
      findRequest = JsonUtils.getObjectFromJson(findRequestJson, FindRequest.class);

      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      final ViewerTable table = database.getMetadata().getTable(tableUUID);

      if (Boolean.FALSE.equals(exportLobs) && StringUtils.isBlank(zipFilename)) {
        return handleCSVExport(solrManager, databaseUUID, table, findRequest, filename, exportDescription);
      } else {
        return handleCSVExportWithLobs(solrManager, databaseUUID, table, findRequest, zipFilename, filename);
      }
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      if (findRequest != null) {
        // register action
        controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
          databaseUUID, ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID, ViewerConstants.CONTROLLER_FILTER_PARAM,
          JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
          findRequest.sublist == null ? JsonUtils.getJsonFromObject(Sublist.NONE)
            : JsonUtils.getJsonFromObject(findRequest.sublist));
      }
    }
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
    final ViewerTable table, final FindRequest findRequest, final String zipFilename, final String filename) {
    List<String> fields = findRequest.fieldsToReturn;
    fields.add(ViewerConstants.INDEX_ID);
    final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields);
    final IterableIndexResult clone = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields);
    fields.remove(ViewerConstants.INDEX_ID);
    return ApiUtils.okResponse(new StreamResponse(new ZipOutputStream(databaseUUID, table, allRows, clone, zipFilename,
      filename, findRequest.fieldsToReturn, findRequest.sublist)));
  }

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

  @Override
  public IndexResult<ViewerRow> findRows(String databaseUUID, String collectionUUID, FindRequest findRequest,
    String localeString) throws RESTException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    long count = 0;

    try {
      final IndexResult<ViewerRow> viewerRowIndexResult = ViewerFactory.getSolrManager().findRows(databaseUUID,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, findRequest.fieldsToReturn, findRequest.extraParameters);
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
  public ViewerRow retrieveRow(String databaseUUID, String collectionUUID, String rowUUID) throws RESTException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().retrieveRows(databaseUUID, rowUUID);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_ROW_ID_PARAM, rowUUID);
    }
  }

  /*******************************************************************************
   * Collection Resource - SavedSearch Sub-resource
   ******************************************************************************/
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

  @Override
  public void deleteSIARDFile(String databaseUUID, String siardUUID) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    String path = "";
    try {
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      path = database.getPath();
      SIARDController.deleteSIARDFileFromPath(database.getPath(), databaseUUID);
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public ViewerDatabase getSiard(String databaseUUID, String siardUUID) {
    return retrieve(databaseUUID);
  }

  @Override
  public void deleteValidationReport(String databaseUUID, String path) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      SIARDController.deleteValidatorReportFileFromPath(path, databaseUUID);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_REPORT_PATH_PARAM, path);
    }
  }

  @Override
  public ViewerMetadata updateMetadataInformation(String databaseUUID, String siardUUID, String path,
    SIARDUpdateParameters parameters) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      return SIARDController.updateMetadataInformation(databaseUUID, path, parameters);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public ViewerMetadata getMetadataInformation(String databaseUUID, String siardUUID) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      return database.getMetadata();
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public Boolean validateSiard(String databaseUUID, String siardUUID, String validationReportPath,
    String allowedTypePath, boolean skipAdditionalChecks) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    String result = null;
    String path = "";
    try {
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      path = database.getPath();
      result = getValidationReportPath(validationReportPath, database.getPath());
      return SIARDController.validateSIARD(databaseUUID, database.getPath(), result, allowedTypePath,
        skipAdditionalChecks);
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path, ViewerConstants.CONTROLLER_REPORT_PATH_PARAM, result,
        ViewerConstants.CONTROLLER_SKIP_ADDITIONAL_CHECKS_PARAM, skipAdditionalChecks);
    }
  }

  @GET
  @Path("/{databaseUUID}/download/siard")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Downloads a specific SIARD file from the storage location", notes = "")
  public Response getSIARDFile(@PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    try {
      ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      File file = new File(database.getPath());
      if (!file.exists()) {
        throw new NotFoundException("SIARD file not found");
      }
      Response.ResponseBuilder responseBuilder = Response.ok(file);
      responseBuilder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
      return responseBuilder.build();
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}/validation")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Downloads a specific SIARD validation report file from the storage location", notes = "")
  public Response getValidationReportFile(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    ViewerDatabase database = null;
    try {
      database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      File file = new File(database.getValidatorReportPath());
      if (!file.exists()) {
        throw new RESTException(new NotFoundException("validation report file not found"));
      }

      Response.ResponseBuilder responseBuilder = Response.ok(file);
      responseBuilder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
      return responseBuilder.build();
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  private String getValidationReportPath(String validationReportPath, String siardPath) {
    if (validationReportPath == null) {
      String filename = Paths.get(siardPath).getFileName().toString().replaceFirst("[.][^.]+$", "") + "-"
        + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".txt";
      validationReportPath = Paths
        .get(ViewerConfiguration.getInstance().getSIARDReportValidationPath().toString(), filename).toAbsolutePath()
        .toString();
    }

    return validationReportPath;
  }
}
