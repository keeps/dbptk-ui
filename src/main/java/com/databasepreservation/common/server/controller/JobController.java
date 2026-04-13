package com.databasepreservation.common.server.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerJobStepExecution;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobController {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

  /**
   * Thread-safe method to append errors to the Spring Batch ExecutionContext
   * during concurrent step executions.
   */
  @SuppressWarnings("unchecked")
  public static synchronized void appendErrorToContext(JobExecution jobExecution, String errorMessage) {
    if (jobExecution == null || errorMessage == null)
      return;

    List<String> errors = (List<String>) jobExecution.getExecutionContext().get(BatchConstants.CONTEXT_JOB_ERRORS_KEY);
    if (errors == null) {
      errors = new ArrayList<>();
    }

    if (!errors.contains(errorMessage)) {
      errors.add(errorMessage);
      jobExecution.getExecutionContext().put(BatchConstants.CONTEXT_JOB_ERRORS_KEY, errors);
    }
  }

  /**
   * Builds the final ViewerJob fully hydrated from the Spring Batch JobExecution.
   * This is used both at the end of a live job and during the reindex process.
   */
  @SuppressWarnings("unchecked")
  private static ViewerJob buildCompleteViewerJobFromExecution(JobExecution jobExecution) {
    ViewerJob viewerJob = new ViewerJob();
    JobParameters params = jobExecution.getJobParameters();

    viewerJob.setUuid(params.getString(BatchConstants.JOB_UUID_KEY));
    viewerJob.setDatabaseUuid(params.getString(BatchConstants.DATABASE_UUID_KEY));
    viewerJob.setCollectionUuid(params.getString(BatchConstants.COLLECTION_UUID_KEY));
    viewerJob.setJobId(jobExecution.getJobId());
    viewerJob.setName(params.getString(BatchConstants.JOB_DISPLAY_NAME_KEY));
    viewerJob.setStatus(ViewerJobStatus.valueOf(jobExecution.getStatus().name()));

    viewerJob.setCreateTime(convertToDate(jobExecution.getCreateTime()));
    viewerJob.setStartTime(convertToDate(jobExecution.getStartTime()));
    viewerJob.setEndTime(convertToDate(jobExecution.getEndTime()));
    viewerJob.setExitCode(jobExecution.getExitStatus().getExitCode());

    if (!jobExecution.getAllFailureExceptions().isEmpty()) {
      viewerJob.setExitDescription(jobExecution.getAllFailureExceptions().get(0).getMessage());
    }

    // Errors from ExecutionContext
    List<String> contextErrors = (List<String>) jobExecution.getExecutionContext()
      .get(BatchConstants.CONTEXT_JOB_ERRORS_KEY);
    if (contextErrors != null) {
      viewerJob.setErrorDetails(contextErrors);
    }

    long dynamicProcessed = 0;
    long totalSkips = 0;
    int completedSteps = 0;
    String currentStepName = "";
    List<ViewerJobStepExecution> stepDetails = new ArrayList<>();
    List<String> executedStepNames = new ArrayList<>();

    // Identify which steps are "Masters" to avoid double-counting items
    java.util.Set<String> masterStepNames = new java.util.HashSet<>();
    for (StepExecution step : jobExecution.getStepExecutions()) {
      if (step.getStepName().contains(BatchConstants.PARTITION_WORKER_NAME)) {
        masterStepNames.add(step.getStepName().split(BatchConstants.PARTITION_WORKER_NAME)[0]);
      }
    }

    for (StepExecution step : jobExecution.getStepExecutions()) {
      boolean isMasterStep = masterStepNames.contains(step.getStepName());
      boolean isWorkerPartition = step.getStepName().contains(BatchConstants.PARTITION_PREFIX) ||
        step.getStepName().contains(BatchConstants.PARTITION_WORKER_NAME);

      if (!isMasterStep) {
        dynamicProcessed += step.getReadCount();
        totalSkips += step.getSkipCount();
      }

      if (!isWorkerPartition) {
        long duration = 0;
        if (step.getStartTime() != null) {
          LocalDateTime end = step.getEndTime() != null ? step.getEndTime() : LocalDateTime.now();
          duration = ChronoUnit.MILLIS.between(step.getStartTime(), end);
        }

        String displayName = step.getExecutionContext().getString(BatchConstants.CONTEXT_STEP_DISPLAY_NAME_KEY, step.getStepName());
        executedStepNames.add(displayName);

        stepDetails.add(new ViewerJobStepExecution(displayName, step.getStatus().name(), step.getWriteCount(), step.getSkipCount(), duration));

        if (step.getStatus() == org.springframework.batch.core.BatchStatus.COMPLETED) {
          completedSteps++;
        } else if (step.getStatus() == org.springframework.batch.core.BatchStatus.STARTED) {
          currentStepName = displayName;
        }
      }
    }

    // Workload Total
    long totalWorkload = dynamicProcessed;
    if (jobExecution.getExecutionContext().containsKey(BatchConstants.CONTEXT_TOTAL_WORKLOAD_KEY)) {
      totalWorkload = jobExecution.getExecutionContext().getLong(BatchConstants.CONTEXT_TOTAL_WORKLOAD_KEY);
    }
    viewerJob.setRowsToProcess(totalWorkload);

    // If completed, we set it to 100%. If not, send the dynamic count that is always growing
    if (jobExecution.getStatus() == org.springframework.batch.core.BatchStatus.COMPLETED) {
      viewerJob.setProcessRows(totalWorkload);
      viewerJob.setCurrentStepName("Completed");
      viewerJob.setCurrentStepNumber(executedStepNames.size());
    } else {
      viewerJob.setProcessRows(dynamicProcessed);
      viewerJob.setCurrentStepName(currentStepName);
      viewerJob.setCurrentStepNumber(completedSteps + 1);
    }

    viewerJob.setSkipCount(totalSkips);
    viewerJob.setStepExecutions(stepDetails);

    if (jobExecution.getExecutionContext().containsKey(BatchConstants.CONTEXT_STEP_DISPLAY_NAMES_KEY)) {
      viewerJob.setStepNames((List<String>) jobExecution.getExecutionContext().get(BatchConstants.CONTEXT_STEP_DISPLAY_NAMES_KEY));
    } else {
      viewerJob.setStepNames(executedStepNames);
    }
    viewerJob.setTotalSteps(viewerJob.getStepNames().size());

    // Fetch Database Name
    try {
      DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
      ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, viewerJob.getDatabaseUuid());
      viewerJob.setDatabaseName(database.getMetadata().getName());
    } catch (NotFoundException | GenericException e) {
      viewerJob.setDatabaseName(viewerJob.getDatabaseUuid());
    }

    return viewerJob;
  }

  private static Date convertToDate(LocalDateTime localDateTime) {
    if (localDateTime == null)
      return null;
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    return Date.from(zonedDateTime.toInstant());
  }

  public static void addMinimalSolrBatchJob(JobParameters parameters, JobContext context)
    throws NotFoundException, GenericException {
    ViewerJob viewerJob = new ViewerJob();
    viewerJob.setUuid(parameters.getString(BatchConstants.JOB_UUID_KEY));
    viewerJob.setDatabaseUuid(parameters.getString(BatchConstants.DATABASE_UUID_KEY));
    viewerJob.setCollectionUuid(parameters.getString(BatchConstants.COLLECTION_UUID_KEY));
    viewerJob.setName(parameters.getString(BatchConstants.JOB_DISPLAY_NAME_KEY));
    viewerJob.setStatus(ViewerJobStatus.STARTING);
    viewerJob.setStepNames(context.getStepNames());
    viewerJob.setTotalSteps(context.getTotalSteps());

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, viewerJob.getDatabaseUuid());
    viewerJob.setDatabaseName(database.getMetadata().getName());

    solrManager.addBatchJob(viewerJob);
    LOGGER.info("JOB Created in Solr with ID: {} for Database UUID: {}", viewerJob.getUuid(),
      viewerJob.getDatabaseUuid());
  }

  /**
   * Final push to Solr. Called ONLY when the job completes or fails, guaranteeing
   * that Solr has the definitive historical snapshot based on the Spring Batch
   * DB.
   */
  public static void syncJobStateToSolr(JobExecution jobExecution) throws NotFoundException, GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerJob finalJob = buildCompleteViewerJobFromExecution(jobExecution);
    solrManager.editBatchJob(finalJob);
  }

  public static void deleteSolrBatchJobs() throws GenericException {
    ViewerFactory.getSolrManager().deleteBatchJob();
  }

  /**
   * Rebuilds the entire Solr Job History by reading the native Spring Batch
   * database.
   */
  public static void reindex(JobRepository jobRepository, JobExplorer jobExplorer)
    throws NotFoundException, GenericException, NoSuchJobException {

    deleteSolrBatchJobs();
    int batchSize = ViewerConfiguration.getInstance().getViewerConfigurationAsInt(100,
      ViewerConstants.REINDEX_BATCH_SIZE);

    for (String jobName : jobRepository.getJobNames()) {
      int startIndex = 0;
      int instanceCount = (int) jobExplorer.getJobInstanceCount(jobName);

      while (startIndex < instanceCount) {
        for (JobInstance jobInstance : jobRepository.findJobInstancesByName(jobName, startIndex, batchSize)) {
          for (JobExecution jobExecution : jobRepository.findJobExecutions(jobInstance)) {
            // Re-hydrate and push to Solr using the unified builder
            syncJobStateToSolr(jobExecution);
          }
        }
        startIndex += batchSize;
      }
    }
  }
}
