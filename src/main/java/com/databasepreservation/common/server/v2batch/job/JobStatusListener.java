package com.databasepreservation.common.server.v2batch.job;

import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobStatusListener implements JobExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobStatusListener.class);

  private final JobContext jobContext;

  public JobStatusListener(JobContext jobContext) {
    this.jobContext = jobContext;
  }

  @Override
  public void beforeJob(JobExecution jobExecution) {
    try {
      JobController.editSolrBatchJob(jobExecution);
      LOGGER.info("Job STARTED for {}", jobContext.getDatabaseUUID());
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot update job on SOLR for {}", jobContext.getDatabaseUUID(), e);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String databaseUUID = jobContext.getDatabaseUUID();

    try {
      JobController.editSolrBatchJob(jobExecution);
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot update job on SOLR for {}", jobContext.getDatabaseUUID(), e);
    }

    if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
      try {
        persistJobChanges(databaseUUID);
      } catch (BatchJobException e) {
        LOGGER.error("Failed to persist job changes for {}", databaseUUID, e);
        jobExecution.setStatus(BatchStatus.FAILED);
      }
    } else {
      LOGGER.error("Job FINISHED with {} for {}", jobExecution.getStatus(), databaseUUID);
    }
  }

  private void persistJobChanges(String databaseUUID) throws BatchJobException {
    try {
      Set<String> entries = jobContext.getCollectionStatus().getDenormalizations();

      if (entries != null) {
        for (String entryID : entries) {
          DenormalizeConfiguration config = jobContext.getDenormalizeConfig(entryID);

          if (config != null && config.getState() == ViewerJobStatus.COMPLETED) {
            ViewerFactory.getConfigurationManager().updateDenormalizationConfigurationFile(databaseUUID, config);
          }
        }
      }

      ViewerFactory.getConfigurationManager().updateCollectionStatus(databaseUUID, jobContext.getCollectionStatus());
      LOGGER.info("Job FINISHED and persisted successfully for {}", databaseUUID);
    } catch (GenericException | ViewerException | IllegalAccessException e) {
      throw new BatchJobException("Failed to persist job changes for " + databaseUUID, e);
    }
  }
}
