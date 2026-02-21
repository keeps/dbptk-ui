package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.AbstractStepDefinition;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DenormalizationStep
  extends AbstractStepDefinition<ViewerRow, DenormalizationStepProcessor.NestedDocumentWrapper> {

  @Override
  public String getName() {
    return "denormalizationStep";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new DenormalizationStepExecutionPolicy();
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new DenormalizationStepPartitionStrategy(solrManager);
  }

  @Override
  public ItemProcessor<ViewerRow, DenormalizationStepProcessor.NestedDocumentWrapper> createProcessor(
    JobContext context, ExecutionContext stepContext) {
    String entryID = stepContext.getString("denormalizeEntryID");
    DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);
    return new DenormalizationStepProcessor(solrManager, config, context.getDatabaseUUID());
  }

  @Override
  public ItemWriter<DenormalizationStepProcessor.NestedDocumentWrapper> createWriter(JobContext context) {
    return new DenormalizeStepWriter(solrManager, context.getDatabaseUUID());
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status) {
    if (status == BatchStatus.COMPLETED) {
      String entryID = stepContext.getString("denormalizeEntryID");
      DenormalizeConfiguration config = jobContext.getDenormalizeConfig(entryID);

      if (config != null) {
        config.setState(ViewerJobStatus.COMPLETED);
        config.setLastExecutionDate(new java.util.Date());
      }
    }
  }

  @Override
  public void onStepCompleted(JobContext jobContext, BatchStatus status) throws BatchJobException {
    if (status == BatchStatus.COMPLETED) {
      try {
        ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, jobContext.getDatabaseUUID());
        Set<String> entries = jobContext.getCollectionStatus().getDenormalizations();

        for (String entryID : entries) {
          DenormalizeConfiguration config = jobContext.getDenormalizeConfig(entryID);
          if (config != null && config.getState() == ViewerJobStatus.COMPLETED) {
            DenormalizationStepUtils.updateCollectionStatusInMemory(jobContext.getCollectionStatus(), config, database);
          }
        }
      } catch (NotFoundException | GenericException e) {
        throw new BatchJobException("Error updating metadata for " + jobContext.getDatabaseUUID(), e);
      }
    }
  }
}
