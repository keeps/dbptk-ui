package com.databasepreservation.common.api.v1;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.progress.DataTransformationProgressData;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.JobService;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.I18nUtility;
import com.databasepreservation.common.utils.UserUtility;
import com.databasepreservation.model.exception.ModuleException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_JOB)
public class JobResource implements JobService {
  @Context
  private HttpServletRequest request;

  @Autowired
  ApplicationContext context;

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
  JobRegistry JobRegistry;

  @Autowired
  JobExplorer jobExplorer;

  Map<String, JobExecution> jobExecutionMap = new HashMap<>();

  @Override
  public ViewerJob retrieve(String jobUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().retrieve(ViewerJob.class, jobUUID);
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public IndexResult<ViewerJob> findJobs(FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);
    long count = 0;

    try {
      final IndexResult<ViewerJob> result = ViewerFactory.getSolrManager().find(ViewerJob.class, findRequest.filter,
        findRequest.sorter, findRequest.sublist, findRequest.facets);
      count = result.getTotalCount();
      return result;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_FACET_PARAM,
        JsonUtils.getJsonFromObject(findRequest.facets), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
        JsonUtils.getJsonFromObject(findRequest.sublist), ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  @Override
  public List<String> denormalizeCollectionJob(String databaseUUID) {
    List<String> jobList = new ArrayList<>();
    try {
      CollectionStatus collectionStatus = getConfiguration(
        Paths
          .get(ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID + ViewerConstants.JSON_EXTENSION),
        databaseUUID, CollectionStatus.class);

      for (String denormalization : collectionStatus.getDenormalizations()) {
        DenormalizeConfiguration denormalizeConfiguration = getConfiguration(
          Paths.get(denormalization + ViewerConstants.JSON_EXTENSION), databaseUUID, DenormalizeConfiguration.class);
        if (denormalizeConfiguration.getState().equals(ViewerJobStatus.NEW)) {
          jobList.add(setupJob(databaseUUID, denormalizeConfiguration.getTableUUID()));
        }
      }

    } catch (ModuleException e) {
      e.printStackTrace();
    }
    return jobList;
  }

  @Override
  public String denormalizeTableJob(String databaseUUID, String tableuuid) {
    try {
      return setupJob(databaseUUID, tableuuid);
    } catch (ModuleException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Boolean stopDenormalizeJob(String databaseuuid, String tableuuid) {
    String uuid = databaseuuid + tableuuid;
    try {
      jobOperator.stop(jobExecutionMap.get(uuid).getId());
      return true;
    } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Boolean startDenormalizeJob(String databaseuuid, String tableuuid) {
    String uuid = databaseuuid + tableuuid;
    try {
      jobOperator.restart(jobExecutionMap.get(uuid).getId());
      return true;
    } catch (NoSuchJobExecutionException | JobInstanceAlreadyCompleteException | NoSuchJobException
      | JobRestartException | JobParametersInvalidException e) {
      throw new RESTException(e);
    }
  }

  private String setupJob(String databaseUUID, String tableUUID) throws ModuleException {

    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addDate(ViewerConstants.SOLR_SEARCHES_DATE_ADDED, new Date());
    jobBuilder.addString(ViewerConstants.INDEX_ID, SolrUtils.randomUUID());
    jobBuilder.addString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    jobBuilder.addString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID);
    JobParameters jobParameters = jobBuilder.toJobParameters();

    JobExecution jobExecution = null;
    try {
      jobExecution = jobLauncher.run(job, jobParameters);
    } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
      | JobParametersInvalidException e) {
      throw new ModuleException().withMessage("Cannot run a Job: " + e.getMessage());
    }

    String uuid = databaseUUID + tableUUID;
    jobExecutionMap.put(uuid, jobExecution);
    return uuid;
  }

  private <T> T getConfiguration(java.nio.file.Path path, String databaseUUID, Class<T> objectClass)
    throws ModuleException {
    java.nio.file.Path configurationPath = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
      .resolve(path);
    if (Files.exists(configurationPath)) {
      return JsonTransformer.readObjectFromFile(configurationPath, objectClass);
    } else {
      throw new ModuleException().withMessage("Configuration file not exist: " + configurationPath.toString());
    }
  }

  @Override
  public Map<String, DataTransformationProgressData> getProgress() {
    return DataTransformationProgressData.getInstances();
  }

  @Override
  public IndexResult<ViewerJob> find(FindRequest findRequest, String locale) {
    try {
      final IndexResult<ViewerJob> result = ViewerFactory.getSolrManager().find(ViewerJob.class, findRequest.filter,
        findRequest.sorter, findRequest.sublist, findRequest.facets);
      return I18nUtility.translate(result, ViewerJob.class, locale);
    } catch (GenericException | RequestNotValidException e) {
      throw new RESTException(e);
    }
  }
}
