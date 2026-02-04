package com.databasepreservation.common.server.batch.steps.denormalize;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.steps.common.listners.ProgressChunkListener;
import com.databasepreservation.common.server.batch.steps.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class DenormalizeStepConfig {

  @Bean
  @StepScope
  public ProgressChunkListener progressListener(DatabaseRowsSolrManager solrManager) {
    return new ProgressChunkListener(solrManager);
  }

  // Configuration from Job Execution Context
  @Bean
  @StepScope
  public DenormalizeConfiguration denormalizeConfiguration(
    @Value("#{jobExecutionContext['DENORMALIZE_STATUS_CONFIG']}") DenormalizeConfiguration denormalizeConfiguration) {
    if (denormalizeConfiguration == null) {
      throw new IllegalStateException("Denormalize configuration not found in job execution context.");
    }
    return denormalizeConfiguration;
  }

  @Bean
  @StepScope
  public DenormalizeCleanupTasklet denormalizeCleanupTasklet(DatabaseRowsSolrManager solrManager,
    DenormalizeConfiguration config, DatabaseRowsSolrManager solrManagerInternal,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID)
    throws GenericException, NotFoundException {

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    return new DenormalizeCleanupTasklet(solrManager, config, database, databaseUUID, tableUUID);
  }

  // Tasklet de Finalização (Update status)
  @Bean
  @StepScope
  public DenormalizeFinalizeTasklet denormalizeFinalizeTasklet(DenormalizeConfiguration config,
    DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID)
    throws GenericException, NotFoundException {

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    return new DenormalizeFinalizeTasklet(config, database, databaseUUID, tableUUID);
  }

  // Step
  @Bean("denormalizeStep")
  public Step denormalizeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    DenormalizeCleanupTasklet cleanupTasklet, DenormalizeFinalizeTasklet finalizeTasklet, SolrCursorItemReader reader,
    DenormalizeProcessor processor, DenormalizeWriter writer, ProgressChunkListener progressListener) {

    Step cleanupStep = new StepBuilder("cleanupStep", jobRepository).tasklet(cleanupTasklet, transactionManager)
      .build();

    Step processStep = new StepBuilder("processDenormalizeChunk",
      jobRepository).<ViewerRow, DenormalizeProcessor.NestedDocumentWrapper> chunk(1000, transactionManager)
      .reader(reader).processor(processor).writer(writer).listener((StepExecutionListener) progressListener)
      .listener((ChunkListener) progressListener).build();

    Step finalizeStep = new StepBuilder("finalizeStep", jobRepository).tasklet(finalizeTasklet, transactionManager)
      .build();

    Flow denormalizeFlow = new FlowBuilder<SimpleFlow>("denormalizeFlow").start(cleanupStep).next(processStep)
      .next(finalizeStep).build();

    return new StepBuilder("denormalizeStep", jobRepository).flow(denormalizeFlow).build();
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
  public DenormalizeProcessor denormalizeProcessor(DenormalizeConfiguration config, DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID)
    throws NotFoundException, GenericException {

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);

    // O Processor é criado com a configuração já carregada
    return new DenormalizeProcessor(solrManager, config, database, databaseUUID);
  }

  // Writer
  @Bean
  @StepScope
  public DenormalizeWriter denormalizeWriter(DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {
    return new DenormalizeWriter(solrManager, databaseUUID);
  }
}
