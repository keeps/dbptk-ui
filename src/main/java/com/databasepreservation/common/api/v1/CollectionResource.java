/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.databasepreservation.common.api.v1.utils.JobResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.springframework.batch.core.BatchStatus;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.DownloadUtils;
import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.api.utils.StreamResponse;
import com.databasepreservation.common.api.utils.ViewerStreamingOutput;
import com.databasepreservation.common.api.v1.utils.IterableIndexResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ResultsCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.api.v1.utils.ZipOutputStreamMultiRow;
import com.databasepreservation.common.api.v1.utils.ZipOutputStreamSingleRow;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.LargeObjectConsolidateProperty;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerLobStoreType;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.ViewerCelllUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.exceptions.AuthorizationException;
import com.databasepreservation.common.exceptions.SavedSearchException;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.controller.ReporterType;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.server.storage.BinaryConsumesOutputStream;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.LobManagerUtils;
import com.databasepreservation.common.utils.UserUtility;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_DATABASE)
public class CollectionResource implements CollectionService {
  @Autowired
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

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/report", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads the migration report for a specific database")
  public ResponseEntity<Resource> getReport(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      Path reportPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID, ReporterType.BROWSE);
      String filename = reportPath.getFileName().toString();
      if (!Files.exists(reportPath)) {
        throw new NotFoundException("Missing report file: " + filename);
      }

      InputStreamResource resource = new InputStreamResource(new FileInputStream(reportPath.toFile()));
      return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=\"" + reportPath.toFile().getName() + "\"")
        .contentLength(reportPath.toFile().length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    } catch (NotFoundException | IOException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
  }

  @Override
  public StringResponse createCollection(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();
    // Checks if property ui.plugin.loadOnAccess is enable. If so, let the
    // authenticated user
    // creates a collection for that SIARD. If the user is a guest it will throw an
    // AuthorizationException
    try {
      final boolean loadOnAccess = ViewerFactory.getViewerConfiguration().getViewerConfigurationAsBoolean(false,
        ViewerConstants.PROPERTY_PLUGIN_LOAD_ON_ACCESS);
      if (loadOnAccess) {
        user = UserUtility.getUser(request);
        if (user.isGuest()) {
          controllerAssistant.registerAction(UserUtility.getGuest(request), LogEntryState.UNAUTHORIZED);
          throw new AuthorizationDeniedException(
            "The user '" + user.getId() + "' does not have all needed permissions");
        }
      } else {
        user = controllerAssistant.checkRoles(request);
      }

      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);

      return new StringResponse(SIARDController.loadFromLocal(database.getPath(), databaseUUID, database.getVersion()));

    } catch (GenericException | AuthorizationDeniedException | NotFoundException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public ProgressData getProgressData(String databaseUUID, String collectionUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      return ProgressData.getInstance(databaseUUID);
    } catch (AuthorizationException e) {
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public Boolean deleteCollection(String databaseUUID, String collectionUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      final String collectionName = SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
      if (SolrClientFactory.get().deleteCollection(collectionName)) {
        Filter savedSearchFilter = new Filter(new SimpleFilterParameter(SOLR_SEARCHES_DATABASE_UUID, databaseUUID));
        SolrUtils.delete(ViewerFactory.getSolrClient(), SolrDefaultCollectionRegistry.get(SavedSearch.class),
          savedSearchFilter);

        ViewerFactory.getSolrManager().markDatabaseCollection(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
        ViewerFactory.getConfigurationManager().updateDatabaseStatus(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
        return true;
      }
    } catch (ViewerException | GenericException | RequestNotValidException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
        .getConfigurationCollection(databaseUUID, collectionUUID);
      return Collections.singletonList(configurationCollection);
    } catch (GenericException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      ViewerFactory.getConfigurationManager().updateCollectionStatus(databaseUUID, status);
    } catch (ViewerException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      Path path = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
        .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION);
      if (Files.exists(path)) {
        return JsonTransformer.readObjectFromFile(path, DenormalizeConfiguration.class);
      } else {
        ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
        ViewerTable table = database.getMetadata().getTable(tableUUID);
        return new DenormalizeConfiguration(databaseUUID, table);
      }
    } catch (ViewerException | NotFoundException | GenericException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      // check if there is no job running on table
      for (JobExecution runningJobExecution : jobExplorer.findRunningJobExecutions("denormalizeJob")) {
        if (runningJobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM)
          .equals(tableUUID)) {
          throw new RESTException(new AlreadyExistsException("A job is already running on this table"));
        }
      }
      JsonTransformer.writeObjectToFile(configuration,
        ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
          .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION));
      ViewerFactory.getConfigurationManager().addDenormalization(databaseUUID,
        ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID);
    } catch (GenericException | ViewerException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      ViewerFactory.getConfigurationManager().removeDenormalization(databaseUUID,
        ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID);
      Path path = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
        .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION);
      if (Files.exists(path)) {
        Files.delete(path);
      }
    } catch (GenericException | IOException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
    }
    return true;
  }

  @Override
  public synchronized JobResponse run(String databaseUUID, String collectionUUID, String tableUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);

      // check if there is no job running on table
      for (JobExecution runningJobExecution : jobExplorer.findRunningJobExecutions("denormalizeJob")) {
        if (runningJobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM)
          .equals(tableUUID)) {
          throw new RESTException(new AlreadyExistsException("A job is already running on this table"));
        }
      }

      JobParametersBuilder jobBuilder = new JobParametersBuilder();
      jobBuilder.addDate(ViewerConstants.SOLR_SEARCHES_DATE_ADDED, new Date());
      String jobId = SolrUtils.randomUUID();
      jobBuilder.addString(ViewerConstants.INDEX_ID, jobId);
      jobBuilder.addString(ViewerConstants.CONTROLLER_COLLECTION_ID_PARAM, collectionUUID);
      jobBuilder.addString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
      jobBuilder.addString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
      JobParameters jobParameters = jobBuilder.toJobParameters();

      JobController.addMinimalSolrBatchJob(jobParameters);
      JobExecution jobExecution = jobLauncher.run(job, jobParameters);
      JobController.editSolrBatchJob(jobExecution);

      if (jobExecution.getStatus().equals(BatchStatus.FAILED)) {
        JobController.setMessageToSolrBatchJob(jobExecution, "Queue is full, please try later");
      }
      return new JobResponse(jobId, jobExecution.getStatus().toString(), jobExecution.getCreateTime().toString());
    } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
      | JobParametersInvalidException | NotFoundException | GenericException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
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
    User user = new User();

    long count = 0;

    try {
      user = controllerAssistant.checkRoles(request);
      final IndexResult<ViewerRow> viewerRowIndexResult = ViewerFactory.getSolrManager().findRows(databaseUUID,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, findRequest.fieldsToReturn,
        findRequest.extraParameters);
      count = viewerRowIndexResult.getTotalCount();
      return viewerRowIndexResult;
    } catch (GenericException | RequestNotValidException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      final ViewerRow viewerRow = ViewerFactory.getSolrManager().retrieveRows(databaseUUID, rowIndex);
      if (viewerRow.getTableId().equals(schema + "." + table)) {
        return viewerRow;
      } else {
        throw new NotFoundException("Row not found");
      }
    } catch (NotFoundException | GenericException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_ROW_ID_PARAM, rowIndex);
    }
  }

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{rowIndex}/{columnIndex}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads a LOB for a specific row within a database")
  public ResponseEntity<StreamingResponseBody> exportLOB(
    @PathVariable(name = ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID,
    @PathVariable(name = ViewerConstants.API_PATH_PARAM_COLLECTION_UUID) String collectionUUID,
    @PathVariable(name = "schema") String schema, @PathVariable(name = "table") String table,
    @PathVariable(name = "rowIndex") String rowIndex, @PathVariable(name = "columnIndex") Integer columnIndex,
    @RequestHeader HttpHeaders headers) {

    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    try {
      user = controllerAssistant.checkRoles(request);
      ViewerRow row = solrManager.retrieveRows(databaseUUID, rowIndex);
      final ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
        .getConfigurationCollection(databaseUUID, databaseUUID);
      final TableStatus configTable = configurationCollection.getTableStatusByTableId(row.getTableId());

      if (ViewerType.dbTypes.CLOB.equals(configTable.getColumnByIndex(columnIndex).getType())) {
        return handleClobDownload(configTable, row, columnIndex);
      }

      if (configurationCollection.getConsolidateProperty().equals(LargeObjectConsolidateProperty.CONSOLIDATED)) {
        return handleConsolidatedLobDownload(databaseUUID, configTable, columnIndex, row, rowIndex);
      } else {
        if (ViewerLobStoreType.EXTERNALLY
          .equals(row.getCells().get(configTable.getColumnByIndex(columnIndex).getId()).getStoreType())) {
          return handleExternalLobDownload(configTable, row, columnIndex);
        } else {
          String version = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID).getVersion();
          return handleInternalLobDownload(database.getPath(), configTable, row, columnIndex, version, headers);
        }
      }
    } catch (NotFoundException | GenericException | IOException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, schema + "." + table, ViewerConstants.CONTROLLER_ROW_ID_PARAM,
        rowIndex, ViewerConstants.CONTROLLER_COLUMN_ID_PARAM, columnIndex);
    }
  }

  private ResponseEntity<StreamingResponseBody> handleConsolidatedLobDownload(String databaseUUID,
    TableStatus tableConfiguration, int columnIndex, ViewerRow row, String rowIndex) throws IOException {
    final java.nio.file.Path consolidatedPath = LobManagerUtils.getConsolidatedPath(
      ViewerFactory.getViewerConfiguration(), databaseUUID, tableConfiguration.getUuid(), columnIndex, rowIndex);
    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, tableConfiguration, columnIndex);
    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = consolidatedPath.getFileName().toString();
    }

    return ApiUtils.okResponse(
      new StreamResponse(handlebarsFilename, tableConfiguration.getColumnByIndex(columnIndex).getApplicationType(),
        DownloadUtils
          .stream(Files.newInputStream(LobManagerUtils.getConsolidatedPath(ViewerFactory.getViewerConfiguration(),
            databaseUUID, row.getTableId(), columnIndex, rowIndex)))));
  }

  private ResponseEntity<StreamingResponseBody> handleClobDownload(TableStatus tableConfiguration, ViewerRow row,
    int columnIndex) {
    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, tableConfiguration, columnIndex);

    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = "file_" + columnIndex;
    }

    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      row.getCells().get(tableConfiguration.getColumnByIndex(columnIndex).getId()).getValue().getBytes());

    return ApiUtils.okResponse(new StreamResponse(handlebarsFilename,
      tableConfiguration.getColumnByIndex(columnIndex).getApplicationType(), DownloadUtils.stream(inputStream)));
  }

  private ResponseEntity<StreamingResponseBody> handleExternalLobDownload(TableStatus tableConfiguration, ViewerRow row,
    int columnIndex) throws IOException {
    final String lobLocation = row.getCells().get(tableConfiguration.getColumnByIndex(columnIndex).getId()).getValue();
    final Path lobPath = Paths.get(lobLocation);
    final Path completeLobPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath().resolve(lobPath);

    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, tableConfiguration, columnIndex);

    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = completeLobPath.getFileName().toString();
    }

    String mimeType = handleMimeType(tableConfiguration, row, columnIndex);

    return ApiUtils.okResponse(new StreamResponse(handlebarsFilename, mimeType,
      DownloadUtils.stream(Files.newInputStream(completeLobPath.toFile().toPath()))));
  }

  private ResponseEntity<StreamingResponseBody> handleInternalLobDownload(String databasePath,
    TableStatus tableConfiguration, ViewerRow row, int columnIndex, String version, HttpHeaders headers)
    throws IOException, GenericException {
    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, tableConfiguration, columnIndex);

    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = ViewerConstants.SIARD_RECORD_PREFIX + row.getUuid()
        + ViewerConstants.SIARD_LOB_FILE_EXTENSION;
    }

    String mimeType = handleMimeType(tableConfiguration, row, columnIndex);

    if (version.equals(ViewerConstants.SIARD_DK_1007) || version.equals(ViewerConstants.SIARD_DK_128)) {
      String filePath = row.getCells().get(row.getCells().keySet().toArray()[row.getCells().size() - 1]).getValue();
      Path path = Paths.get(filePath);
      // if the lob is a directory zip it
      if (path.toFile().isDirectory()) {
        Path zipFile = LobManagerUtils.zipDirectory(path, databasePath, handlebarsFilename);

        return ApiUtils.okResponse(new StreamResponse(handlebarsFilename, mimeType,
          DownloadUtils.stream(new BufferedInputStream(new FileInputStream(zipFile.toFile())))));
      } else {

        if (!headers.getRange().isEmpty()) {
          return ApiUtils.rangeResponse(headers, new BinaryConsumesOutputStream(Path.of(filePath),
            Path.of(filePath).toFile().length(), handlebarsFilename, mimeType));
        }

        return ApiUtils.okResponse(new StreamResponse(handlebarsFilename, mimeType,
          DownloadUtils.stream(new BufferedInputStream(new FileInputStream(filePath)))));
      }
    } else {
      if (LobManagerUtils.isLobEmbedded(tableConfiguration, row, columnIndex)) {
        // handle lob as embedded
        String lobCellValue = LobManagerUtils.getLobCellValue(tableConfiguration, row, columnIndex);
        lobCellValue = lobCellValue.replace(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX, "");
        String decodedString = new String(Base64.decodeBase64(lobCellValue.getBytes()));

        return ApiUtils.okResponse(new StreamResponse(handlebarsFilename, mimeType,
          DownloadUtils.stream(new BufferedInputStream(new ByteArrayInputStream(decodedString.getBytes())))));
      } else {
        // handle lob as internal on separated folder
        ZipFile zipFile = new ZipFile(databasePath);
        final ZipEntry entry = zipFile.getEntry(LobManagerUtils.getZipFilePath(tableConfiguration, columnIndex, row));
        if (entry == null) {
          throw new GenericException("Zip archive entry is missing");
        }

        return ApiUtils.okResponse(new StreamResponse(handlebarsFilename, mimeType,
          DownloadUtils.stream(new BufferedInputStream(zipFile.getInputStream(entry)))));
      }
    }
  }

  @NotNull
  private static String handleMimeType(TableStatus tableConfiguration, ViewerRow row, int columnIndex) {
    String configurationApplicationType = tableConfiguration.getColumnByIndex(columnIndex).getApplicationType();
    if (configurationApplicationType.equals(ViewerCelllUtils.getAutoDetectMimeTypeTemplate())) {
      String handlebarsMimeType = HandlebarsUtils.applyMimeTypeTemplate(row, tableConfiguration, columnIndex);
      return ViewerStringUtils.isNotBlank(handlebarsMimeType) ? handlebarsMimeType
        : MediaType.APPLICATION_OCTET_STREAM.getType();
    } else {
      return configurationApplicationType;
    }
  }

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/find/export", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Export the rows as CSV")
  public ResponseEntity<StreamingResponseBody> exportFindToCSV(
    @Parameter(name = "The database unique identifier", required = true) @PathVariable(name = "databaseUUID") String databaseUUID,
    @Parameter(name = "The collection unique identifier", required = true) @PathVariable(name = "collectionUUID") String collectionUUID,
    @Parameter(name = "The schema name", required = true) @PathVariable(name = "schema") String schema,
    @Parameter(name = "The table name", required = true) @PathVariable(name = "table") String table,
    @Parameter(name = "Find request to filter/limit the search") @RequestParam(name = "f") String findRequestJson,
    @Parameter(name = "The CSV filename") @RequestParam(name = "filename") String filename,
    @Parameter(name = "The Zip filename") @RequestParam(name = "zipFilename", required = false) String zipFilename,
    @Parameter(name = "Export description", schema = @Schema(allowableValues = "true, false")) @RequestParam(name = "descriptions") boolean exportDescription,
    @Parameter(name = "Export LOBs", schema = @Schema(allowableValues = "true, false")) @RequestParam(name = "lobs") boolean exportLobs,
    @Parameter(name = "Fields to export", required = true) @RequestParam(name = "fl") String fieldsToHeader) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    FindRequest findRequest = null;

    try {
      user = controllerAssistant.checkRoles(request);
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
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationException e) {
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

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{rowIndex}/export", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Export the a single row as CSV")
  public ResponseEntity<StreamingResponseBody> exportSingleRowToCSV(
    @Parameter(name = "The database unique identifier", required = true) @PathVariable(name = "databaseUUID") String databaseUUID,
    @Parameter(name = "The collection unique identifier", required = true) @PathVariable(name = "collectionUUID") String collectionUUID,
    @Parameter(name = "The schema name", required = true) @PathVariable(name = "schema") String schema,
    @Parameter(name = "The table name", required = true) @PathVariable(name = "table") String table,
    @Parameter(name = "The index of the row", required = true) @PathVariable(name = "rowIndex") String rowIndex,
    @Parameter(name = "The CSV filename", required = true) @RequestParam(name = "filename") String filename,
    @Parameter(name = "The Zip filename") @RequestParam(name = "zipFilename", required = false) String zipFilename,
    @Parameter(name = "Export description", schema = @Schema(allowableValues = "true, false"), required = true) @RequestParam(name = "descriptions") boolean exportDescription,
    @Parameter(name = "Export LOBs", schema = @Schema(allowableValues = "true, false"), required = true) @RequestParam(name = "lobs") boolean exportLobs) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
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
    } catch (GenericException | NotFoundException | AuthorizationException e) {
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

  private ResponseEntity<StreamingResponseBody> handleSingleCSVExportWithoutLOBs(String databaseUUID,
    TableStatus configTable, ViewerRow row, String filename, boolean exportDescriptions) throws GenericException {
    final CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
      .getConfigurationCollection(databaseUUID, databaseUUID);

    final List<String> fieldsToReturn = configurationCollection.getFieldsToReturn(configTable.getId());
    return ApiUtils.okResponse(new ViewerStreamingOutput(
      new ResultsCSVOutputStream(row, configTable, filename, exportDescriptions, ',', String.join(",", fieldsToReturn)))
      .toStreamResponse());
  }

  private ResponseEntity<StreamingResponseBody> handleSingleCSVExportWithLOBs(CollectionStatus configurationCollection,
    ViewerDatabase database, TableStatus configTable, ViewerRow row, String filename, String zipFilename,
    boolean exportDescriptions) throws GenericException {
    final List<String> fieldsToReturn = configurationCollection.getFieldsToReturn(configTable.getId());
    return ApiUtils.okResponse(new StreamResponse(new ZipOutputStreamSingleRow(configurationCollection, database,
      configTable, row, zipFilename, filename, fieldsToReturn, exportDescriptions)));
  }

  private ResponseEntity<StreamingResponseBody> handleCSVExport(DatabaseRowsSolrManager solrManager,
    final String databaseUUID, final TableStatus configTable, final FindRequest findRequest, final String filename,
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

  private ResponseEntity<StreamingResponseBody> handleCSVExportWithLobs(DatabaseRowsSolrManager solrManager,
    CollectionStatus configurationCollection, ViewerDatabase database, final String databaseUUID,
    final TableStatus configTable, final FindRequest findRequest, final String zipFilename, final String filename,
    final boolean exportDescription, String fieldsToHeader) {
    List<String> fields = findRequest.fieldsToReturn;
    fields.add(ViewerConstants.INDEX_ID);
    final IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields, findRequest.extraParameters);
    final IterableIndexResult clone = solrManager.findAllRows(databaseUUID, findRequest.filter, findRequest.sorter,
      fields, findRequest.extraParameters);
    return ApiUtils.okResponse(new StreamResponse(new ZipOutputStreamMultiRow(configurationCollection, database,
      configTable, allRows, clone, zipFilename, filename, findRequest.sublist, exportDescription, fieldsToHeader)));
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
  public StringResponse saveSavedSearch(String databaseUUID, String collectionUUID, String tableUUID, String name,
    String description, SearchInfo searchInfo) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();
    SavedSearch savedSearch = new SavedSearch();

    try {
      user = controllerAssistant.checkRoles(request);

      String searchInfoJson = JsonUtils.getJsonFromObject(searchInfo);

      savedSearch.setUuid(SolrUtils.randomUUID());
      savedSearch.setName(name);
      savedSearch.setDescription(description);
      savedSearch.setDatabaseUUID(databaseUUID);
      savedSearch.setTableUUID(tableUUID);
      savedSearch.setTableName(tableUUID);
      savedSearch.setSearchInfoJson(searchInfoJson);

      ViewerFactory.getSolrManager().addSavedSearch(savedSearch);
      return new StringResponse(savedSearch.getUuid());
    } catch (NotFoundException | GenericException | AuthorizationException e) {
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
    User user = new User();

    long count = 0;

    try {
      user = controllerAssistant.checkRoles(request);
      final IndexResult<SavedSearch> savedSearchIndexResult = ViewerFactory.getSolrManager().find(SavedSearch.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
      count = savedSearchIndexResult.getTotalCount();
      return savedSearchIndexResult;
    } catch (GenericException | RequestNotValidException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      return ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
    } catch (NotFoundException | GenericException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      ViewerFactory.getSolrManager().editSavedSearch(databaseUUID, savedSearchUUID, name, description);
    } catch (SavedSearchException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      ViewerFactory.getSolrManager().deleteSavedSearch(savedSearchUUID);
    } catch (SavedSearchException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM, savedSearchUUID);
    }
  }
}