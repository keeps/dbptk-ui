package com.databasepreservation.common.server.jobs.spring;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeProcessor implements Tasklet {

  String databaseUUID = null;

  public DenormalizeProcessor() {
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    System.out.println("Processor " + this.databaseUUID);
    return null;
  }
}
