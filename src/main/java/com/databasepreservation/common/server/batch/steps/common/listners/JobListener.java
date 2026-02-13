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

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
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
    try {
      JobController.editSolrBatchJob(jobExecution);

      LOGGER.info("Job STARTED for {}", databaseUUID);
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Cannot update job on SOLR for {}", databaseUUID, e);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String databaseUUID = jobExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    try {
      JobController.editSolrBatchJob(jobExecution);
      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        try {
          LOGGER.info("Job FINISHED for {}", databaseUUID);
          CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
            .getConfigurationCollection(databaseUUID, databaseUUID);
          configurationCollection.setNeedsToBeProcessed(false);
          ViewerFactory.getConfigurationManager().updateCollectionStatus(databaseUUID, configurationCollection);
        } catch (ViewerException | IllegalAccessException e) {
          LOGGER.error("Cannot update collection status for {}", databaseUUID, e);
        }
      } else {
        LOGGER.error("Job FINISHED with ERROR for {}: {}", databaseUUID,
          jobExecution.getExitStatus().getExitDescription());
      }
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Cannot update job on SOLR for {}", databaseUUID, e);
    }
  }
}
