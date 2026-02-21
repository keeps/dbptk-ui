package com.databasepreservation.common.server.batch.listeners;

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
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.context.JobContextRegistry;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.controller.JobController;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobStatusListener implements JobExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobStatusListener.class);

  private final JobContext jobContext;
  private final JobContextRegistry registry;

  public JobStatusListener(JobContext jobContext, JobContextRegistry registry) {
    this.jobContext = jobContext;
    this.registry = registry;
  }

  @Override
  public void beforeJob(JobExecution jobExecution) {
    try {
      JobController.editSolrBatchJob(jobExecution);
      LOGGER.info("Job STARTED for database: {}", jobContext.getDatabaseUUID());
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot update job on SOLR for {}", jobContext.getDatabaseUUID(), e);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String databaseUUID = jobContext.getDatabaseUUID();

    try {
      JobController.editSolrBatchJob(jobExecution);

      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        persistJobChanges(databaseUUID);
      } else {
        LOGGER.error("Job FINISHED with status {} for database {}", jobExecution.getStatus(), databaseUUID);
      }
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot update job on SOLR for {}", databaseUUID, e);
    } catch (BatchJobException e) {
      LOGGER.error("Failed to persist job changes for {}", databaseUUID, e);
      jobExecution.setStatus(BatchStatus.FAILED);
    } finally {
      registry.unregister(databaseUUID);
      LOGGER.debug("JobContext unregistered for database: {}", databaseUUID);
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
