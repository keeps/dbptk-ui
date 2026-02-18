package com.databasepreservation.common.server.v2batch.steps.denormalization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.v2batch.common.policy.ErrorPolicy;
import com.databasepreservation.common.server.v2batch.common.policy.ExecutionPolicy;
import com.databasepreservation.common.server.v2batch.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.v2batch.job.JobContext;
import com.databasepreservation.common.server.v2batch.steps.StepDefinition;
import com.databasepreservation.common.server.v2batch.steps.StepExitPolicy;
import com.databasepreservation.common.server.v2batch.steps.partition.PartitionStrategy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DenormalizationStep
  implements StepDefinition<ViewerRow, DenormalizationStepProcessor.NestedDocumentWrapper> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizationStep.class);

  @Autowired
  private DatabaseRowsSolrManager solrManager;

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
  public ItemReader<ViewerRow> getReader(JobContext context, ExecutionContext stepContext) {
    String entryID = stepContext.getString("denormalizeEntryID");
    DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

    if (config == null) {
      LOGGER.error("Configuration not found for entryID: {}. Skipping partition.", entryID);
      return null; // Returning null will cause Spring Batch to skip this partition
    }

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);

    for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
      for (ReferencesConfiguration reference : relatedTable.getReferences()) {
        fieldsToReturn.add(reference.getReferencedTable().getSolrName());
      }
    }

    Filter filter = FilterUtils.filterByTable(new Filter(), config.getTableID());
    return new SolrCursorItemReader(solrManager, context.getDatabaseUUID(), filter, fieldsToReturn);
  }

  @Override
  public ItemProcessor<ViewerRow, DenormalizationStepProcessor.NestedDocumentWrapper> getProcessor(JobContext context,
    ExecutionContext stepContext) {
    String entryID = stepContext.getString("denormalizeEntryID");
    DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

    return new DenormalizationStepProcessor(solrManager, config, context.getDatabaseUUID());
  }

  @Override
  public ItemWriter<DenormalizationStepProcessor.NestedDocumentWrapper> getWriter(JobContext context) {
    return new DenormalizeStepWriter(solrManager, context.getDatabaseUUID());
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    ErrorPolicy errorPolicy = new ErrorPolicy(0, 2);
    errorPolicy.getRetryableExceptions().add(SolrServerException.class);
    errorPolicy.getRetryableExceptions().add(IOException.class);
    return errorPolicy;
  }

  @Override
  public StepExitPolicy getExitPolicy() {
    return StepExitPolicy.FAIL_JOB_ON_FAILURE;
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
      ViewerDatabase database;
      try {
        database = solrManager.retrieve(ViewerDatabase.class, jobContext.getDatabaseUUID());
      } catch (NotFoundException | GenericException e) {
        throw new BatchJobException("Error retrieving database metadata for UUID: " + jobContext.getDatabaseUUID(), e);
      }

      Set<String> entries = jobContext.getCollectionStatus().getDenormalizations();
      for (String entryID : entries) {
        DenormalizeConfiguration config = jobContext.getDenormalizeConfig(entryID);
        if (config != null && config.getState() == ViewerJobStatus.COMPLETED) {
          DenormalizationStepUtils.updateCollectionStatusInMemory(jobContext.getCollectionStatus(), config, database);
        }
      }
    }
  }
}
