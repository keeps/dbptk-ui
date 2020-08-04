package com.databasepreservation.common.server.jobs;

import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.transformers.DenormalizeTransformer;
import com.databasepreservation.model.exception.ModuleException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.databasepreservation.common.client.ViewerConstants;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeProcessor implements Tasklet {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeProcessor.class);

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws ModuleException {
    JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
    String databaseUUID = chunkContext.getStepContext().getStepExecution().getJobParameters()
      .getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    String tableUUID = chunkContext.getStepContext().getStepExecution().getJobParameters()
      .getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
    String jobUUID = chunkContext.getStepContext().getStepExecution().getJobParameters()
      .getString(ViewerConstants.INDEX_ID);
    try {
      DenormalizeTransformer denormalizeTransformer = new DenormalizeTransformer(databaseUUID, tableUUID, jobUUID);
    } catch (ModuleException e) {
      try {
        chunkContext.getStepContext().getStepExecution().setTerminateOnly();
        chunkContext.getStepContext().getStepExecution()
          .setExitStatus(new ExitStatus(ViewerJobStatus.FAILED.name(), e.getMessage()));
        JobController.setMessageToSolrBatchJob(jobExecution, e.getMessage());
        throw new ModuleException().withCause(e).withMessage(e.getMessage());
      } catch (NotFoundException | GenericException ex) {
        LOGGER.error("Cannot update job on SOLR", e);
      }
    }
    return RepeatStatus.FINISHED;
  }
}
