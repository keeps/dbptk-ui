package com.databasepreservation.common.api.v1;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
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
import com.databasepreservation.common.client.models.DenormalizeProgressData;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.TableConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.services.JobService;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.I18nUtility;
import com.databasepreservation.model.exception.ModuleException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_JOB)
public class JobResource implements JobService {
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
  public Boolean denormalizeJob(String databaseUUID) {
    try {
      CollectionConfiguration configuration = getConfiguration(Paths.get(databaseUUID + ViewerConstants.JSON_EXTENSION),
        databaseUUID, CollectionConfiguration.class);

      setupJob(configuration, databaseUUID);
      return true;
    } catch (ModuleException e) {
      throw new RESTException(e);
    }
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

  private void setupJob(CollectionConfiguration configuration, String databaseUUID) throws ModuleException {
    for (TableConfiguration table : configuration.getTables()) {
      if (table.getDenormalizeConfiguration().getState().equals(ViewerJobStatus.NEW)) {
        try {
          JobParametersBuilder jobBuilder = new JobParametersBuilder();
          jobBuilder.addDate(ViewerConstants.SOLR_SEARCHES_DATE_ADDED, new Date());
          jobBuilder.addString(ViewerConstants.INDEX_ID, SolrUtils.randomUUID());
          jobBuilder.addString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
          jobBuilder.addString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM, table.getUuid());
          JobParameters jobParameters = jobBuilder.toJobParameters();

          JobExecution jobExecution = jobLauncher.run(job, jobParameters);
          String uuid = databaseUUID + table.getUuid();
          jobExecutionMap.put(uuid, jobExecution);

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
          | JobParametersInvalidException e) {
          throw new ModuleException().withMessage("Cannot run a Job: " + e.getMessage());
        }
      }
    }

    for (String jobName : jobOperator.getJobNames()) {
      System.out.println("JobNames " + jobName);
    }
  }

  private <T> T getConfiguration(java.nio.file.Path path, String databaseUUID, Class<T> objectClass)
    throws ModuleException {
    java.nio.file.Path configurationPath = ViewerConfiguration.getInstance().getDatabasesPath()
      .resolve(databaseUUID).resolve(path);
    if (Files.exists(configurationPath)) {
      return JsonTransformer.readObjectFromFile(configurationPath, objectClass);
    } else {
      throw new ModuleException().withMessage("Configuration file not exist: " + configurationPath.toString());
    }
  }

  @Override
  public List<DenormalizeProgressData> progress(String databaseUUID) {
    List<DenormalizeProgressData> progressDataList = new ArrayList<>();

    try {
      CollectionConfiguration configuration = getConfiguration(Paths.get(databaseUUID + ViewerConstants.JSON_EXTENSION),
        databaseUUID, CollectionConfiguration.class);
      for (TableConfiguration table : configuration.getTables()) {
        progressDataList.add(DenormalizeProgressData.getInstance(databaseUUID, table.getUuid()));
      }
    } catch (ModuleException e) {
      throw new RESTException(e);
    }

    return progressDataList;
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
