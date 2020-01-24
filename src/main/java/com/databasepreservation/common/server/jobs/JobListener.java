package com.databasepreservation.common.server.jobs;

import java.nio.file.Files;
import java.nio.file.Path;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ViewerFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.index.utils.JsonTransformer;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class JobListener extends JobExecutionListenerSupport {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobListener.class);

  @Override
  public void beforeJob(JobExecution jobExecution) {
    super.beforeJob(jobExecution);
    try {
      JobController.addSolrBatchJob(jobExecution);
      String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
      String tableUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
      ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      updateConfigurationFile(databaseUUID, tableUUID, ViewerJobStatus.valueOf(jobExecution.getStatus().name()));
      LOGGER.info("Job STARTED for table " + tableUUID);

    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot insert job on SOLR", e);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    try {
      JobController.editSolrBatchJob(jobExecution);
      if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
        String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
        String tableUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
        updateConfigurationFile(databaseUUID, tableUUID, ViewerJobStatus.valueOf(jobExecution.getStatus().name()));
        LOGGER.info("Job FINISHED for table " + tableUUID);
      }
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Cannot update job on SOLR", e);
    }
  }

  private Boolean updateConfigurationFile(String databaseUUID, String tableUUID, ViewerJobStatus status){
    Path path = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
      .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION);
    if (Files.exists(path)) {
      try {
        DenormalizeConfiguration configuration = JsonTransformer.readObjectFromFile(path, DenormalizeConfiguration.class);
        configuration.setState(status);
        JsonTransformer.writeObjectToFile(configuration, path);
        return true;
      } catch (ViewerException e) {
        return false;
      }
    }
    return false;
  }
}
