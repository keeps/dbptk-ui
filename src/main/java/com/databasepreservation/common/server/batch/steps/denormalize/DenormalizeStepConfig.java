package com.databasepreservation.common.server.batch.steps.denormalize;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.steps.common.JobProgressAggregator;
import com.databasepreservation.common.server.batch.steps.common.listners.SolrProgressFeedListener;
import com.databasepreservation.common.server.batch.steps.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class DenormalizeStepConfig {

  @Autowired
  private JobProgressAggregator aggregator;

  @Bean
  @StepScope
  public SolrProgressFeedListener progressListener(DatabaseRowsSolrManager solrManager) {
    return new SolrProgressFeedListener(solrManager, aggregator);
  }

  // Configuration from Job Execution Context
  @Bean
  @StepScope
  public DenormalizeConfiguration denormalizeConfiguration(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{stepExecutionContext['denormalizeEntryID']}") String denormalizeEntryID) {
    try {
      if (denormalizeEntryID == null) {
        return null;
      }
      return ViewerFactory.getConfigurationManager().getDenormalizeConfigurationFromCollectionStatusEntry(databaseUUID,
        denormalizeEntryID);
    } catch (Exception e) {
      throw new RuntimeException("Error loading denormalization configuration for entry ID: " + denormalizeEntryID, e);
    }
  }

  @Bean
  @StepScope
  public TableCompletionListener tableCompletionListener(DenormalizeConfiguration config,
    DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID)
    throws GenericException, NotFoundException {

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);

    return new TableCompletionListener(config, database, databaseUUID);
  }

  @Bean("denormalizeStep")
  public Step denormalizeStep(JobRepository jobRepository, Step tablePartitionerStep, CollectionStatus status) {

    return new StepBuilder("denormalizeStep", jobRepository)
      .partitioner("tablePartitionerStep", new DenormalizeStepTablePartitioner(status)).step(tablePartitionerStep)
      .taskExecutor(new SyncTaskExecutor()).gridSize(1).build();
  }

  // Step
  @Bean("tablePartitionerStep")
  public Step tablePartitionerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    @Qualifier("denormalizeReader") SolrCursorItemReader reader, DenormalizeStepProcessor processor,
    DenormalizeStepWriter writer, SolrProgressFeedListener progressListener,
    TableCompletionListener lifecycleListener) {

    return new StepBuilder("tablePartitionerStep",
      jobRepository).<ViewerRow, DenormalizeStepProcessor.NestedDocumentWrapper> chunk(1000, transactionManager)
      .reader(reader).processor(processor).writer(writer)

      // Listeners
      .listener((StepExecutionListener) progressListener).listener((ChunkListener) progressListener)
      .listener(lifecycleListener).build();
  }

  // Reader
  @Bean
  @StepScope
  public SolrCursorItemReader denormalizeReader(DenormalizeConfiguration denormalizeConfiguration,
    DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    for (RelatedTablesConfiguration relatedTable : denormalizeConfiguration.getRelatedTables()) {
      for (ReferencesConfiguration reference : relatedTable.getReferences()) {
        fieldsToReturn.add(reference.getReferencedTable().getSolrName());
      }
    }

    Filter filter = FilterUtils.filterByTable(new Filter(), denormalizeConfiguration.getTableID());

    return new SolrCursorItemReader(solrManager, databaseUUID, filter, fieldsToReturn);
  }

  // Processor
  @Bean
  @StepScope
  public DenormalizeStepProcessor denormalizeProcessor(DenormalizeConfiguration config,
    DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID)
    throws NotFoundException, GenericException {

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    return new DenormalizeStepProcessor(solrManager, config, database, databaseUUID);
  }

  // Writer
  @Bean
  @StepScope
  public DenormalizeStepWriter denormalizeWriter(DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {
    return new DenormalizeStepWriter(solrManager, databaseUUID);
  }
}
