package com.databasepreservation.common.server.controller;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
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
    viewerJob.setTableUuid(jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM));
    viewerJob.setJobId(jobExecution.getJobId());
    viewerJob.setName(jobExecution.getJobInstance().getJobName());
    viewerJob.setCreateTime(jobExecution.getCreateTime());
    viewerJob.setStartTime(jobExecution.getStartTime());
    viewerJob.setEndTime(jobExecution.getEndTime());
    viewerJob.setStatus(ViewerJobStatus.valueOf(jobExecution.getStatus().name()));
    viewerJob.setExitCode(jobExecution.getExitStatus().getExitCode());
    if (!jobExecution.getAllFailureExceptions().isEmpty()) {
      viewerJob.setExitDescription(jobExecution.getAllFailureExceptions().get(0).getMessage());
    }

    return viewerJob;
  }

  public static void addSolrBatchJob(JobExecution jobExecution) throws NotFoundException, GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerJob viewerJob = createViewerJob(jobExecution);
    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, viewerJob.getDatabaseUuid());
    viewerJob.setDatabaseName(database.getMetadata().getName());
    viewerJob.setTableName(database.getMetadata().getTable(viewerJob.getTableUuid()).getName());
    viewerJob.setSchemaName(database.getMetadata().getTable(viewerJob.getTableUuid()).getSchemaName());
    solrManager.addBatchJob(viewerJob);
  }

  public static void editSolrBatchJob(JobExecution jobExecution) throws NotFoundException, GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    ViewerJob viewerJob = createViewerJob(jobExecution);
    solrManager.editBatchJob(viewerJob);
  }
}
