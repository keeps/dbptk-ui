package com.databasepreservation.common.server.jobs;

import com.databasepreservation.model.exception.ModuleException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.transformers.Denormalize;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeProcessor implements Tasklet {

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws ModuleException {
    String databaseUUID = chunkContext.getStepContext().getStepExecution().getJobParameters()
      .getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    String tableUUID = chunkContext.getStepContext().getStepExecution().getJobParameters()
      .getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
    System.out.println("Processor " + databaseUUID);
    System.out.println("tableUUID " + tableUUID);
    Denormalize denormalize = new Denormalize(databaseUUID, tableUUID);
    return RepeatStatus.FINISHED;
  }
}
