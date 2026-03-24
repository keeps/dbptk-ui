package com.databasepreservation.common.server.batch.listeners;

import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.context.JobContextRegistry;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.BatchErrorExtractor;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobStatusListener implements JobExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobStatusListener.class);

  private final JobContext jobContext;
  private final JobContextRegistry registry;
  private final JobRepository jobRepository;

  public JobStatusListener(JobContext jobContext, JobContextRegistry registry, JobRepository jobRepository) {
    this.jobContext = jobContext;
    this.registry = registry;
    this.jobRepository = jobRepository;
  }

  @Override
  public void beforeJob(JobExecution jobExecution) {
    try {
      JobController.editSolrBatchJob(jobExecution, jobContext);
      LOGGER.info("[JOB] STARTED for database: {}", jobContext.getDatabaseUUID());
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("[JOB] ERROR: Cannot update job on SOLR for {}", jobContext.getDatabaseUUID(), e);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String databaseUUID = jobContext.getDatabaseUUID();
    String jobUUID = jobExecution.getJobParameters().getString(BatchConstants.JOB_UUID_KEY);

    try {
      if (jobExecution.getStatus() == BatchStatus.FAILED) {
        DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
        Set<String> uniqueErrors = new HashSet<>();

        for (Throwable t : jobExecution.getAllFailureExceptions()) {
          String cleanErrorMsg = BatchErrorExtractor.extractMeaningfulError(t);

          if (uniqueErrors.add(cleanErrorMsg)) {
            if (jobExecution.getExecutionContext().containsKey("ERR_" + cleanErrorMsg.hashCode())) {
              LOGGER.error("[JOB] FAILED for database {}. Cause already saved by item listener: {}", databaseUUID,
                cleanErrorMsg);
              continue;
            }

            LOGGER.error("[JOB] FAILED for database {}. Cause: {}", databaseUUID, cleanErrorMsg);
            solrManager.appendBatchJobError(jobUUID, cleanErrorMsg);
          }
        }
      }

      JobController.editSolrBatchJob(jobExecution, jobContext);

      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        persistJobChanges(databaseUUID);
      } else {
        LOGGER.error("[JOB] FAILED with status {} for database {}", jobExecution.getStatus(), databaseUUID);
      }
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("[JOB] ERROR: Cannot update job on SOLR for {}", databaseUUID, e);
    } catch (BatchJobException e) {
      LOGGER.error("[JOB] CRITICAL: Failed to persist job changes for {}", databaseUUID, e);
      jobExecution.setStatus(BatchStatus.FAILED);
      jobExecution.addFailureException(e);
      jobRepository.update(jobExecution);
    } finally {
      registry.unregister(databaseUUID);
      LOGGER.debug("[JOB] Context unregistered for database: {}", databaseUUID);
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
      LOGGER.info("[JOB] FINISHED and persisted successfully for {}", databaseUUID);
    } catch (GenericException | ViewerException | IllegalAccessException e) {
      throw new BatchJobException("Failed to persist job changes for " + databaseUUID, e);
    }
  }
}
