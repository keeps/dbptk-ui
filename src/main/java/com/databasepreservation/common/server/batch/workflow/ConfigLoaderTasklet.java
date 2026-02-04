package com.databasepreservation.common.server.batch.workflow;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.database.DatabaseStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class ConfigLoaderTasklet implements Tasklet {
  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    String databaseUUID = (String) chunkContext.getStepContext().getJobParameters()
      .get(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    String collectionUUID = (String) chunkContext.getStepContext().getJobParameters()
      .get(ViewerConstants.CONTROLLER_COLLECTION_ID_PARAM);
    String tableUUID = (String) chunkContext.getStepContext().getJobParameters()
      .get(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);

    DatabaseStatus databaseStatus = ViewerFactory.getConfigurationManager().getDatabaseStatus(databaseUUID);

    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
      .put("DATABASE_STATUS_CONFIG", databaseStatus);

    CollectionStatus configurationCollection = ViewerFactory.getConfigurationManager()
      .getConfigurationCollection(databaseUUID, collectionUUID);

    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
      .put("COLLECTION_STATUS_CONFIG", configurationCollection);

    DenormalizeConfiguration denormalizeConfiguration = ViewerFactory.getConfigurationManager()
      .getDenormalizeConfiguration(databaseUUID, tableUUID);
    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
      .put("DENORMALIZE_STATUS_CONFIG", denormalizeConfiguration);

    return RepeatStatus.FINISHED;
  }
}
