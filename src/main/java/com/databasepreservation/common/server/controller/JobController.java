/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobController {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

  private static ViewerJob createViewerJob(JobExecution jobExecution) {
    ViewerJob viewerJob = new ViewerJob();
    viewerJob.setUuid(jobExecution.getJobParameters().getString(ViewerConstants.INDEX_ID));
    viewerJob.setDatabaseUuid(jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM));
    viewerJob
      .setCollectionUuid(jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_COLLECTION_ID_PARAM));
    viewerJob.setJobId(jobExecution.getJobId());
    viewerJob.setName(jobExecution.getJobInstance().getJobName());
    viewerJob.setCreateTime(convertToDate(jobExecution.getCreateTime()));
    viewerJob.setStartTime(convertToDate(jobExecution.getStartTime()));
    viewerJob.setEndTime(convertToDate(jobExecution.getEndTime()));
    viewerJob.setStatus(ViewerJobStatus.valueOf(jobExecution.getStatus().name()));
    viewerJob.setExitCode(jobExecution.getExitStatus().getExitCode());
    if (!jobExecution.getAllFailureExceptions().isEmpty()) {
      viewerJob.setExitDescription(jobExecution.getAllFailureExceptions().get(0).getMessage());
    }

    return viewerJob;
  }

  private static ViewerJob createCompleteViewerJob(JobExecution jobExecution) throws GenericException {
    ViewerJob viewerJob = createViewerJob(jobExecution);
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    try {
      ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, viewerJob.getDatabaseUuid());
      viewerJob.setDatabaseName(database.getMetadata().getName());
      if (viewerJob.getStatus().equals(ViewerJobStatus.COMPLETED)) {
        long rowsNumber = 0;
        viewerJob.setRowsToProcess(rowsNumber);
        viewerJob.setProcessRows(rowsNumber);
      }
    } catch (NotFoundException e) {
      viewerJob.setDatabaseName(viewerJob.getDatabaseUuid());
    }

    return viewerJob;
  }

  private static Date convertToDate(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }

    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    Instant instant = zonedDateTime.toInstant();
    return Date.from(instant);
  }

  private static ViewerJob createMinimalViewerJob(JobParameters parameters) {
    ViewerJob viewerJob = new ViewerJob();
    viewerJob.setUuid(parameters.getString(ViewerConstants.INDEX_ID));
    viewerJob.setDatabaseUuid(parameters.getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM));
    viewerJob.setCollectionUuid(parameters.getString(ViewerConstants.CONTROLLER_COLLECTION_ID_PARAM));
    viewerJob.setStatus(ViewerJobStatus.STARTING);

    return viewerJob;
  }

  public static void addMinimalSolrBatchJob(JobParameters parameters) throws NotFoundException, GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerJob viewerJob = createMinimalViewerJob(parameters);
    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, viewerJob.getDatabaseUuid());
    viewerJob.setDatabaseName(database.getMetadata().getName());
    solrManager.addBatchJob(viewerJob);

    LOGGER.info("JOB Created in Solr with ID: {} for Database UUID: {}, Collection UUID: {}", viewerJob.getUuid(),
      viewerJob.getDatabaseUuid(), viewerJob.getCollectionUuid());
  }

  public static void editSolrBatchJob(JobExecution jobExecution) throws NotFoundException, GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerJob viewerJob = createViewerJob(jobExecution);
    solrManager.editBatchJob(viewerJob);
  }

  public static void createSolrBatchJob(JobExecution jobExecution) throws NotFoundException, GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerJob viewerJob = createCompleteViewerJob(jobExecution);
    solrManager.editBatchJob(viewerJob);
  }

  public static void setMessageToSolrBatchJob(JobExecution jobExecution, String message)
    throws NotFoundException, GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerJob viewerJob = createViewerJob(jobExecution);
    viewerJob.setExitDescription(message);
    solrManager.editBatchJob(viewerJob);
  }

  public static void deleteSolrBatchJobs() throws GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    solrManager.deleteBatchJob();
  }

  public static void reindex(JobRepository jobRepository, JobExplorer jobExplorer)
    throws NotFoundException, GenericException, NoSuchJobException {
    deleteSolrBatchJobs();
    int startIndex = 0;
    int batchSize = ViewerConfiguration.getInstance().getViewerConfigurationAsInt(100,
      ViewerConstants.REINDEX_BATCH_SIZE);

    for (String jobName : jobRepository.getJobNames()) {
      while (startIndex < (int) jobExplorer.getJobInstanceCount(jobName)) {
        for (JobInstance jobInstance : jobRepository.findJobInstancesByName(jobName, startIndex, batchSize)) {
          for (JobExecution jobExecution : jobRepository.findJobExecutions(jobInstance)) {
            JobController.createSolrBatchJob(jobExecution);
          }
        }
        startIndex += batchSize;
      }
    }
  }
}
