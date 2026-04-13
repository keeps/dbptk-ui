package com.databasepreservation.common.server.batch.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.OrFiltersParameters;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;

/**
 * Initializes batch cleanup routines on application startup. Ensures that any
 * jobs interrupted by a server crash are properly terminated in both the Spring
 * Batch repository and the Solr index.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class BatchCleanupInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchCleanupInitializer.class);
  private static final String ZOMBIE_ERROR_MSG = "Job marked as FAILED by application startup due to an unexpected server shutdown.";

  @Autowired
  private JobExplorer jobExplorer;

  @Autowired
  private JobRepository jobRepository;

  @Autowired
  private DatabaseRowsSolrManager solrManager;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    LOGGER.info("Starting Batch Cleanup routines...");

    cleanSpringBatchZombies();
    cleanSolrOrphans();

    LOGGER.info("Batch Cleanup routines finished.");
  }

  /**
   * PHASE 1: Cleans up jobs that were actively running inside the Spring Batch
   * context when the application crashed. Terminates both StepExecutions and
   * JobExecutions.
   */
  private void cleanSpringBatchZombies() {
    LOGGER.info("Phase 1: Checking for interrupted Spring Batch executions...");

    for (String jobName : jobExplorer.getJobNames()) {
      Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions(jobName);

      for (JobExecution execution : runningJobs) {
        String databaseUUID = execution.getJobParameters().getString(BatchConstants.DATABASE_UUID_KEY);
        String jobUUID = execution.getJobParameters().getString(BatchConstants.JOB_UUID_KEY);

        LOGGER.warn("Spring Batch Zombie detected! ID: {} for Database: {}. Forcing shutdown.", execution.getId(),
          databaseUUID);

        // 1. Terminate all hanging StepExecutions to release worker locks
        for (StepExecution stepExecution : execution.getStepExecutions()) {
          if (stepExecution.getStatus().isRunning()) {
            stepExecution.setStatus(BatchStatus.FAILED);
            stepExecution.setExitStatus(ExitStatus.FAILED);
            stepExecution.setEndTime(LocalDateTime.now());
            jobRepository.update(stepExecution);
          }
        }

        // 2. Terminate the main JobExecution
        execution.setEndTime(LocalDateTime.now());
        execution.setStatus(BatchStatus.FAILED);
        execution.setExitStatus(ExitStatus.FAILED);
        execution.addFailureException(new RuntimeException(ZOMBIE_ERROR_MSG));

        JobController.appendErrorToContext(execution, ZOMBIE_ERROR_MSG);
        jobRepository.update(execution);

        // 3. Sync the failed status back to Solr
        try {
          if (jobUUID != null) {
            JobController.syncJobStateToSolr(execution);
            LOGGER.info("Successfully synced zombie job failure to Solr for Job UUID: {}", jobUUID);
          }
        } catch (Exception e) {
          LOGGER.error("Failed to sync zombie job status to Solr for Job UUID: {}", jobUUID, e);
        }
      }
    }
  }

  /**
   * PHASE 2: Cleans up jobs that were registered in Solr (e.g., in a
   * QUEUE/STARTING state) but never reached Spring Batch before the crash. Since
   * Phase 1 guarantees no Spring Batch jobs are running, any STARTING/STARTED job
   * found in Solr at this point is definitively an orphan.
   */
  private void cleanSolrOrphans() {
    LOGGER.info("Phase 2: Checking for orphan jobs stuck in Solr...");

    try {
      // 1. Build filter to find any jobs still marked as active in Solr
      List<FilterParameter> orParameters = new ArrayList<>();
      orParameters
        .add(new SimpleFilterParameter(ViewerConstants.SOLR_BATCH_JOB_STATUS, ViewerJobStatus.STARTING.name()));
      orParameters
        .add(new SimpleFilterParameter(ViewerConstants.SOLR_BATCH_JOB_STATUS, ViewerJobStatus.STARTED.name()));
      Filter filter = new Filter(new OrFiltersParameters(orParameters));

      // 2. Query Solr for ALL active jobs
      try (IterableDatabaseResult<ViewerJob> orphanJobs = solrManager.findAll(ViewerJob.class, filter, new Sorter(),
        new ArrayList<>())) {

        Map<String, List<ViewerJob>> jobsByDatabase = new HashMap<>();
        int orphansCount = 0;

        // 3. Iterate over all results safely
        for (ViewerJob orphanJob : orphanJobs) {
          LOGGER.warn("Solr Orphan Job detected! Job UUID: {}. Forcing status to FAILED.", orphanJob.getUuid());

          orphanJob.setStatus(ViewerJobStatus.FAILED);
          orphanJob.setEndTime(new Date());
          orphanJob.getErrorDetails().add(ZOMBIE_ERROR_MSG);

          jobsByDatabase.computeIfAbsent(orphanJob.getDatabaseUuid(), k -> new ArrayList<>()).add(orphanJob);
          orphansCount++;
        }

        // 4. Persist the fixed statuses back to Solr
        if (orphansCount > 0) {
          for (Map.Entry<String, List<ViewerJob>> entry : jobsByDatabase.entrySet()) {
            solrManager.insertBatchDocuments(entry.getKey(), entry.getValue(),
              DatabaseRowsSolrManager.WriteMode.UPDATE);
          }
          LOGGER.info("Successfully cleaned up {} orphan job(s) from Solr.", orphansCount);
        } else {
          LOGGER.info("No orphan jobs found in Solr.");
        }
      }
    } catch (Exception e) {
      LOGGER.error("Failed to clean orphan jobs from Solr during Phase 2 cleanup.", e);
    }
  }
}
