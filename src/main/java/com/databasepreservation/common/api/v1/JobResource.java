package com.databasepreservation.common.api.v1;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.DenormalizeProgressData;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.TableConfiguration;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.model.exception.ModuleException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import com.databasepreservation.common.client.services.JobService;
import org.springframework.stereotype.Service;

import javax.ws.rs.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
  JobExplorer JobExplorer;

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

  private void setupJob(CollectionConfiguration configuration, String databaseUUID) throws ModuleException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    for (TableConfiguration table : configuration.getTables()) {
      try {
        JobParametersBuilder jobBuilder = new JobParametersBuilder();
        jobBuilder.addDate("startDate", new Date());
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

    for (String jobName : jobOperator.getJobNames()) {
      System.out.println("JobNames " + jobName);
    }
  }

  private <T> T getConfiguration(java.nio.file.Path path, String databaseUUID, Class<T> objectClass)
    throws ModuleException {
    java.nio.file.Path configurationPath = ViewerConfiguration.getInstance().getDatabaseConfigPath()
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

    CollectionConfiguration configuration = null;
    try {
      configuration = getConfiguration(Paths.get(databaseUUID + ViewerConstants.JSON_EXTENSION), databaseUUID,
        CollectionConfiguration.class);
      for (TableConfiguration table : configuration.getTables()) {
        progressDataList.add(DenormalizeProgressData.getInstance(databaseUUID, table.getUuid()));
      }
    } catch (ModuleException e) {
      throw new RESTException(e);
    }

    return progressDataList;
  }
}
