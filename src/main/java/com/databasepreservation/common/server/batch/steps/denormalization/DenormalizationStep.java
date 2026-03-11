package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.AbstractIndexingStepDefinition;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.PartitionableStep;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;

/**
 * Step responsible for denormalizing data across multiple tables. It is
 * chunk-oriented and partitionable. * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DenormalizationStep extends AbstractIndexingStepDefinition<ViewerRow, ViewerRow>
  implements PartitionableStep {

  @Override
  public String getDisplayName() {
    return "Data Denormalization";
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
  public long calculateWorkload(JobContext context) {
    return calculatePartitionedWorkload(context);
  }

  @Override
  public ItemProcessor<ViewerRow, ViewerRow> createProcessor(JobContext context, ExecutionContext stepContext) {
    String entryID = stepContext.getString(BatchConstants.DENORMALIZATION_ENTRY_ID_KEY);
    DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);
    return new DenormalizationStepProcessor(solrManager, config, context.getDatabaseUUID());
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status) {
    if (status == BatchStatus.COMPLETED) {
      String entryID = stepContext.getString(BatchConstants.DENORMALIZATION_ENTRY_ID_KEY);
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

        Set<String> entries = new HashSet<>(jobContext.getCollectionStatus().getDenormalizations());

        for (String entryID : entries) {
          DenormalizeConfiguration config = jobContext.getDenormalizeConfig(entryID);
          if (config != null) {
            if (config.getProcessingState() == ProcessingState.TO_REMOVE) {
              removeDenormalizeConfiguration(jobContext, entryID, config);
            } else if (config.getState() == ViewerJobStatus.COMPLETED) {
              DenormalizationStepUtils.updateCollectionStatusInMemory(jobContext.getCollectionStatus(), config,
                database);
            }
          }
          if (config != null && config.getState() == ViewerJobStatus.COMPLETED) {
            DenormalizationStepUtils.updateCollectionStatusInMemory(jobContext.getCollectionStatus(), config, database);
          }
        }
      } catch (NotFoundException | GenericException e) {
        throw new BatchJobException("Error updating metadata for " + jobContext.getDatabaseUUID(), e);
      }
    }
  }

  private void removeDenormalizeConfiguration(JobContext jobContext, String entryID, DenormalizeConfiguration config)
    throws BatchJobException, GenericException {
    deleteNestedRowsForConfig(jobContext.getDatabaseUUID(), config);
    ViewerFactory.getConfigurationManager().deleteDenormalizationFromCollection(jobContext.getDatabaseUUID(), entryID);
    jobContext.getCollectionStatus().getDenormalizations().remove(entryID);
    TableStatus targetTable = jobContext.getCollectionStatus().getTableStatus(config.getTableUUID());
    if (targetTable != null) {
      DenormalizationStepUtils.removeDenormalizationColumns(targetTable);
    }
  }

  private void deleteNestedRowsForConfig(String databaseUUID, DenormalizeConfiguration config)
    throws BatchJobException {
    try {
      List<RelatedTablesConfiguration> allRelated = new ArrayList<>();
      collectAllRelatedTables(config.getRelatedTables(), allRelated);

      for (RelatedTablesConfiguration related : allRelated) {
        Filter filter = new Filter(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_NESTED_UUID, related.getUuid()));
        solrManager.deleteRowsByQuery(databaseUUID, filter);
      }
    } catch (ViewerException e) {
      throw new BatchJobException("Failed to delete nested rows from Solr for denormalization: " + config.getId(), e);
    }
  }

  private void collectAllRelatedTables(List<RelatedTablesConfiguration> relatedTables,
    List<RelatedTablesConfiguration> collector) {
    if (relatedTables == null)
      return;
    for (RelatedTablesConfiguration rt : relatedTables) {
      collector.add(rt);
      collectAllRelatedTables(rt.getRelatedTables(), collector);
    }
  }
}
