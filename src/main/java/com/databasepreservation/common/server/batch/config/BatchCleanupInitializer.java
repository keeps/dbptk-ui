package com.databasepreservation.common.server.batch.config;

import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class BatchCleanupInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchCleanupInitializer.class);

  @Autowired
  private JobExplorer jobExplorer;

  @Autowired
  private JobRepository jobRepository;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    LOGGER.info("Checking for any jobs blocked from previous executions...");

    for (String jobName : jobExplorer.getJobNames()) {
      Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions(jobName);

      for (JobExecution execution : runningJobs) {
        String databaseUUID = execution.getJobParameters().getString("databaseUUID");
        String jobUUID = execution.getJobParameters().getString(BatchConstants.JOB_UUID_KEY);

        LOGGER.warn("Job Zombie detected! ID: {} for Database: {}. Forcing shutdown.", execution.getId(), databaseUUID);

        execution.setEndTime(LocalDateTime.now());
        execution.setStatus(BatchStatus.FAILED);
        execution.setExitStatus(ExitStatus.FAILED);

        String errorMessage = "Job marked as FAILED by BatchCleanupInitializer due to being detected as a zombie job on application startup.";
        execution.addFailureException(new RuntimeException(errorMessage));

        jobRepository.update(execution);

        try {
          DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
          if (solrManager != null && jobUUID != null) {
            solrManager.appendBatchJobError(jobUUID, errorMessage);

            JobController.editSolrBatchJob(execution, null);
            LOGGER.info("Successfully synced zombie job failure to Solr for Job UUID: {}", jobUUID);
          }
        } catch (Exception e) {
          LOGGER.error("Failed to sync zombie job status to Solr for Job UUID: {}", jobUUID, e);
        }
      }
    }
  }
}
