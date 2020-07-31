package com.databasepreservation.common.server.jobs;

import java.nio.file.Files;
import java.nio.file.Path;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
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
    String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    String tableUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
    try {
      JobController.editSolrBatchJob(jobExecution);
      updateConfigurationFile(databaseUUID, tableUUID, ViewerJobStatus.valueOf(jobExecution.getStatus().name()),
        jobExecution.getJobId());
      LOGGER.info("Job STARTED for " + databaseUUID + "/" + tableUUID);
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot update job on SOLR for " + databaseUUID + "/" + tableUUID, e);
    } catch (ViewerException e) {
      LOGGER.error("Cannot update configuration file for "+ databaseUUID + "/" + tableUUID , e);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    super.afterJob(jobExecution);
    String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    String tableUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
    try {
      JobController.editSolrBatchJob(jobExecution);
      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        updateConfigurationFile(databaseUUID, tableUUID, ViewerJobStatus.valueOf(jobExecution.getStatus().name()),
          jobExecution.getJobId());
        LOGGER.info("Job FINISHED for " + databaseUUID + "/" + tableUUID);
      } else {
        updateConfigurationFile(databaseUUID, tableUUID, ViewerJobStatus.FAILED, jobExecution.getJobId());
        LOGGER.error("Job FINISHED for " + databaseUUID + "/" + tableUUID + "with error: "
          + jobExecution.getExitStatus().getExitDescription());
      }
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Cannot update job on SOLR for " + databaseUUID + "/" + tableUUID, e);
    } catch (ViewerException e) {
      LOGGER.error("Cannot update configuration file for "+ databaseUUID + "/" + tableUUID , e);
    }
  }

  private void updateConfigurationFile(String databaseUUID, String tableUUID, ViewerJobStatus status, Long jobId)
    throws ViewerException {
    Path path = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
      .resolve(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION);
    if (Files.exists(path)) {
      DenormalizeConfiguration configuration = JsonTransformer.readObjectFromFile(path, DenormalizeConfiguration.class);
      configuration.setJob(jobId);
      configuration.setState(status);
      JsonTransformer.writeObjectToFile(configuration, path);
    }
  }
}
