package com.databasepreservation.common.api.v1;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.DownloadUtils;
import com.databasepreservation.common.api.utils.HandlebarsUtils;
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
import com.databasepreservation.common.client.models.status.collection.LargeObjectConsolidateProperty;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.database.DatabaseStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ConfigurationManager;
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

  @Autowired
  @Qualifier("denormalizeJob")
  Job job;

  @Autowired
  @Qualifier("customJobLauncher")
  JobLauncher jobLauncher;

  @Autowired
  @Qualifier("customJobOperator")
  JobOperator jobOperator;

  @Autowired
  org.springframework.batch.core.configuration.JobRegistry JobRegistry;

  @Autowired
  JobExplorer jobExplorer;

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/report")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Downloads the migration report for a specific database")
  public Response getReport(@PathParam("databaseUUID") String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      final String collectionName = SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
      if (SolrClientFactory.get().deleteCollection(collectionName)) {
        Filter savedSearchFilter = new Filter(new SimpleFilterParameter(SOLR_SEARCHES_DATABASE_UUID, databaseUUID));
        SolrUtils.delete(ViewerFactory.getSolrClient(), SolrDefaultCollectionRegistry.get(SavedSearch.class),
          savedSearchFilter);

        final ConfigurationManager configurationManager = ViewerFactory.getConfigurationManager();
        final DatabaseStatus databaseStatus = configurationManager.getDatabaseStatus(databaseUUID);
        final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);

        for (String collectionUUID : databaseStatus.getCollections()) {

          final CollectionStatus configurationCollection = configurationManager.getConfigurationCollection(databaseUUID,
            collectionUUID, true);
          for (String denormalizationUUID : configurationCollection.getDenormalizations()) {
            configurationManager.deleteDenormalizationFromCollection(databaseUUID, denormalizationUUID);
          }

          configurationManager.deleteCollection(databaseUUID, collectionUUID);
          configurationManager.addCollection(database.getUuid(),
            SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + database.getUuid());

          configurationManager.addTable(database);
        }

        databaseStatus.setCollections(
          Collections.singletonList(ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + database.getUuid()));
        configurationManager.updateDatabaseStatus(databaseStatus);

        ViewerFactory.getSolrManager().markDatabaseCollection(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
        return true;
      }
    } catch (GenericException | RequestNotValidException | ViewerException | NotFoundException e) {
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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
        .getConfigurationCollection(databaseUUID, collectionUUID);
      return Collections.singletonList(configurationCollection);
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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
    }
  }

  @Override
  public synchronized Boolean createDenormalizeConfigurationFile(String databaseUUID, String collectionUUID,
    String tableUUID, DenormalizeConfiguration configuration) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    // check if there is no job running on table
    for (JobExecution runningJobExecution : jobExplorer.findRunningJobExecutions("denormalizeJob")) {
      System.out.println(runningJobExecution);
      if (runningJobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM)
        .equals(tableUUID)) {
        throw new RESTException("A job is already running on this table",
          com.google.gwt.http.client.Response.SC_CONFLICT);
      }
    }

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
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
    }
    return true;
  }

  @Override
  public Boolean deleteDenormalizeConfigurationFile(String databaseUUID, String collectionUUID, String tableUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
    }
    return true;
  }

  @Override
  public synchronized void run(String databaseUUID, String collectionUUID, String tableUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    // check if there is no job running on table
    for (JobExecution runningJobExecution : jobExplorer.findRunningJobExecutions("denormalizeJob")) {
      System.out.println(runningJobExecution);
      if (runningJobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM)
        .equals(tableUUID)) {
        throw new RESTException("A job is already running on this table",
          com.google.gwt.http.client.Response.SC_CONFLICT);
      }
    }

    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addDate(ViewerConstants.SOLR_SEARCHES_DATE_ADDED, new Date());
    jobBuilder.addString(ViewerConstants.INDEX_ID, SolrUtils.randomUUID());
    jobBuilder.addString(ViewerConstants.CONTROLLER_COLLECTION_ID_PARAM, collectionUUID);
    jobBuilder.addString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    jobBuilder.addString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
    JobParameters jobParameters = jobBuilder.toJobParameters();

    try {
      jobLauncher.run(job, jobParameters);
    } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
      | JobParametersInvalidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException();
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
    }
  }

  /*******************************************************************************
   * Collection Resource - Data Sub-resource
   ******************************************************************************/
  @Override
  public IndexResult<ViewerRow> findRows(String databaseUUID, String collectionUUID, String schema, String table,
    FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    long count = 0;

    try {
      final IndexResult<ViewerRow> viewerRowIndexResult = ViewerFactory.getSolrManager().findRows(databaseUUID,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, findRequest.fieldsToReturn,
        findRequest.extraParameters);
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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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
  @ApiOperation(value = "Downloads a LOB for a specific row within a database", notes = "download the specified LOB.", response = Response.class)
  public Response exportLOB(@PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @PathParam(ViewerConstants.API_PATH_PARAM_COLLECTION_UUID) String collectionUUID,
    @PathParam("schema") String schema, @PathParam("table") String table, @PathParam("rowIndex") String rowIndex,
    @PathParam("columnIndex") Integer columnIndex,
    @QueryParam(ViewerConstants.API_PATH_PARAM_LOB_FILENAME) String filename) {

    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    try {
      ViewerRow row = solrManager.retrieveRows(databaseUUID, rowIndex);
      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
        .getConfigurationCollection(databaseUUID, databaseUUID);
      final TableStatus configTable = configurationCollection.getTableStatusByTableId(row.getTableId());

      if (configurationCollection.getConsolidateProperty().equals(LargeObjectConsolidateProperty.CONSOLIDATED)) {
        return handleConsolidatedLobDownload(databaseUUID, configTable, columnIndex, row, rowIndex);
      } else {
        if (configTable.getColumnByIndex(columnIndex).isExternalLob()) {
          return handleExternalLobDownload(configTable, row, columnIndex);
        } else {
          return handleInternalLobDownload(database.getPath(), configTable, row, columnIndex);
        }
      }
    } catch (NotFoundException | GenericException | IOException e) {
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

  private Response handleConsolidatedLobDownload(String databaseUUID, TableStatus tableConfiguration, int columnIndex,
    ViewerRow row, String rowIndex) throws IOException {
    final java.nio.file.Path consolidatedPath = LobPathManager.getConsolidatedPath(
      ViewerFactory.getViewerConfiguration(), databaseUUID, tableConfiguration.getUuid(), columnIndex, rowIndex);
    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, tableConfiguration, columnIndex);
    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = consolidatedPath.getFileName().toString();
    }

    return ApiUtils.okResponse(
      new StreamResponse(handlebarsFilename, tableConfiguration.getColumnByIndex(columnIndex).getApplicationType(),
        DownloadUtils
          .stream(Files.newInputStream(LobPathManager.getConsolidatedPath(ViewerFactory.getViewerConfiguration(),
            databaseUUID, row.getTableId(), columnIndex, rowIndex)))));
  }

  private Response handleExternalLobDownload(TableStatus tableConfiguration, ViewerRow row, int columnIndex)
    throws FileNotFoundException {
    final String lobLocation = row.getCells().get(tableConfiguration.getColumnByIndex(columnIndex).getId()).getValue();
    final java.nio.file.Path lobPath = Paths.get(lobLocation);
    final java.nio.file.Path completeLobPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath()
      .resolve(lobPath);

    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, tableConfiguration, columnIndex);

    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = completeLobPath.getFileName().toString();
    }

    return ApiUtils.okResponse(
      new StreamResponse(handlebarsFilename, tableConfiguration.getColumnByIndex(columnIndex).getApplicationType(),
        DownloadUtils.stream(new FileInputStream(completeLobPath.toFile()))));
  }

  private Response handleInternalLobDownload(String databasePath, TableStatus tableConfiguration, ViewerRow row,
    int columnIndex) throws IOException, GenericException {
    ZipFile zipFile = new ZipFile(databasePath);
    final ZipEntry entry = zipFile.getEntry(LobPathManager.getZipFilePath(tableConfiguration, columnIndex, row));
    if (entry == null) {
      throw new GenericException("Zip archive entry is missing");
    }

    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, tableConfiguration, columnIndex);

    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = row.getCells().get(tableConfiguration.getColumnByIndex(columnIndex).getId()).getValue();
    }

    return ApiUtils.okResponse(
      new StreamResponse(handlebarsFilename, tableConfiguration.getColumnByIndex(columnIndex).getApplicationType(),
        DownloadUtils.stream(new BufferedInputStream(zipFile.getInputStream(entry)))));
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
    @ApiParam(value = "Export LOBs", allowableValues = "true, false") @QueryParam("lobs") boolean exportLobs,
    @ApiParam(value = "Fields to export", required = true) @QueryParam("fl") String fieldsToHeader) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    FindRequest findRequest = null;

    try {
      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      findRequest = JsonUtils.getObjectFromJson(findRequestJson, FindRequest.class);
      final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
        .getConfigurationCollection(databaseUUID, databaseUUID);
      final TableStatus configTable = configurationCollection.getTableStatusByTableId(schema + "." + table);

      if (Boolean.FALSE.equals(exportLobs) && StringUtils.isBlank(zipFilename)) {
        return handleCSVExport(solrManager, databaseUUID, configTable, findRequest, filename, exportDescription,
          fieldsToHeader);
      } else {
        return handleCSVExportWithLobs(solrManager, configurationCollection, database, databaseUUID, configTable,
          findRequest, zipFilename, filename, exportDescription, fieldsToHeader);
      }
    } catch (GenericException | RequestNotValidException | NotFoundException e) {
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
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      final ViewerRow viewerRow = solrManager.retrieveRows(databaseUUID, rowIndex);
      final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
        .getConfigurationCollection(databaseUUID, databaseUUID);
      final TableStatus configTable = configurationCollection.getTableStatusByTableId(schema + "." + table);

      if (viewerRow != null && viewerRow.getTableId().equals(schema + "." + table)) {
        if (Boolean.FALSE.equals(exportLobs) && StringUtils.isBlank(zipFilename)) {
          return handleSingleCSVExportWithoutLOBs(databaseUUID, configTable, viewerRow, filename, exportDescription);
        } else {
          return handleSingleCSVExportWithLOBs(configurationCollection, database, configTable, viewerRow, filename,
            zipFilename, exportDescription);
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

  private Response handleSingleCSVExportWithoutLOBs(String databaseUUID, TableStatus configTable, ViewerRow row,
    String filename, boolean exportDescriptions) throws GenericException {
    final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
      .getConfigurationCollection(databaseUUID, databaseUUID);

    final List<String> fieldsToReturn = configurationCollection.getFieldsToReturn(configTable.getId());
    return ApiUtils.okResponse(new ViewerStreamingOutput(
      new ResultsCSVOutputStream(row, configTable, filename, exportDescriptions, ',', String.join(",", fieldsToReturn)))
        .toStreamResponse());
  }

  private Response handleSingleCSVExportWithLOBs(CollectionStatus configurationCollection, ViewerDatabase database,
    TableStatus configTable, ViewerRow row, String filename, String zipFilename, boolean exportDescriptions)
    throws GenericException {
    final List<String> fieldsToReturn = configurationCollection.getFieldsToReturn(configTable.getId());
    return ApiUtils.okResponse(new StreamResponse(new ZipOutputStreamSingleRow(configurationCollection, database,
      configTable, row, zipFilename, filename, fieldsToReturn, exportDescriptions)));
  }

  private Response handleCSVExport(DatabaseRowsSolrManager solrManager, final String databaseUUID,
    final TableStatus configTable, final FindRequest findRequest, final String filename,
    final boolean exportDescriptions, String fieldsToHeader) throws GenericException, RequestNotValidException {
    if (findRequest.sublist == null) {
      final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
        findRequest.fieldsToReturn, findRequest.extraParameters);
      return ApiUtils.okResponse(new ViewerStreamingOutput(new IterableIndexResultsCSVOutputStream(allRows, configTable,
        filename, exportDescriptions, ',', fieldsToHeader)).toStreamResponse());
    } else {
      final IndexResult<ViewerRow> rows = solrManager.findRows(databaseUUID, findRequest.filter, findRequest.sorter,
        findRequest.sublist, null, findRequest.fieldsToReturn, findRequest.extraParameters);

      return ApiUtils.okResponse(new ViewerStreamingOutput(
        new ResultsCSVOutputStream(rows, configTable, filename, exportDescriptions, ',', fieldsToHeader))
          .toStreamResponse());
    }
  }

  private Response handleCSVExportWithLobs(DatabaseRowsSolrManager solrManager,
    CollectionStatus configurationCollection, ViewerDatabase database, final String databaseUUID,
    final TableStatus configTable, final FindRequest findRequest, final String zipFilename, final String filename,
    final boolean exportDescription, String fieldsToHeader) {
    List<String> fields = findRequest.fieldsToReturn;
    fields.add(ViewerConstants.INDEX_ID);
    final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields, findRequest.extraParameters);
    final IterableIndexResult clone = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields, findRequest.extraParameters);
    return ApiUtils.okResponse(new StreamResponse(
      new ZipOutputStream(configurationCollection, databaseUUID, database, configTable, allRows, clone, zipFilename,
        filename, findRequest.fieldsToReturn, findRequest.sublist, exportDescription, fieldsToHeader)));
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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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
