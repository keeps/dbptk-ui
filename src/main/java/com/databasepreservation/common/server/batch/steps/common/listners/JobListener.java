/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.steps.common.listners;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.server.controller.JobController;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class JobListener implements JobExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobListener.class);

  @Override
  public void beforeJob(JobExecution jobExecution) {
    String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    String tableUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
    try {
      JobController.editSolrBatchJob(jobExecution);

      LOGGER.info("Job STARTED for " + databaseUUID + "/" + tableUUID);
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot update job on SOLR for " + databaseUUID + "/" + tableUUID, e);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    String tableUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
    try {
      JobController.editSolrBatchJob(jobExecution);
      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        LOGGER.info("Job FINISHED for " + databaseUUID + "/" + tableUUID);
      } else {
        LOGGER.error("Job FINISHED with ERROR for " + databaseUUID + "/" + tableUUID + ": "
          + jobExecution.getExitStatus().getExitDescription());
      }
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Cannot update job on SOLR for " + databaseUUID + "/" + tableUUID, e);
    }
  }
}
