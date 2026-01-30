/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.tools.FilterUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.batch.item.processor.DenormalizeProcessor;
import com.databasepreservation.common.server.batch.item.reader.SolrCursorItemReader;
import com.databasepreservation.common.server.batch.item.tasklet.DenormalizeCleanupTasklet;
import com.databasepreservation.common.server.batch.item.writer.DenormalizeWriter;
import com.databasepreservation.common.server.batch.listener.JobListener;
import com.databasepreservation.common.server.batch.listener.ProgressChunkListener;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.model.exception.ModuleException;

/**
 * Configuration for the Denormalize Batch Job.
 */
@Configuration
public class DenormalizeJobConfiguration {

  private final DatabaseRowsSolrManager solrManager;

  public DenormalizeJobConfiguration(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Autowired
  private JobListener jobListener;

  @Bean
  @StepScope
  public ProgressChunkListener progressListener() {
    return new ProgressChunkListener(solrManager);
  }

  @Bean
  @StepScope
  public DenormalizeCleanupTasklet denormalizeCleanupTasklet(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID)
    throws ModuleException, NotFoundException, GenericException {

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    DenormalizeConfiguration config = loadConfiguration(databaseUUID, tableUUID);

    return new DenormalizeCleanupTasklet(solrManager, config, database, databaseUUID, tableUUID);
  }

  @Bean
  public Step denormalizeCleanupStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    DenormalizeCleanupTasklet cleanupTasklet) {

    return new StepBuilder("denormalizeCleanupStep", jobRepository).tasklet(cleanupTasklet, transactionManager).build();
  }

  @Bean
  @StepScope
  public SolrCursorItemReader denormalizeReader(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID)
    throws ModuleException, NotFoundException, GenericException {

    DenormalizeConfiguration config = loadConfiguration(databaseUUID, tableUUID);

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
      for (ReferencesConfiguration reference : relatedTable.getReferences()) {
        fieldsToReturn.add(reference.getReferencedTable().getSolrName());
      }
    }

    Filter filter = FilterUtils.filterByTable(new Filter(), config.getTableID());
    return new SolrCursorItemReader(solrManager, databaseUUID, filter, fieldsToReturn);
  }

  @Bean
  @StepScope
  public DenormalizeProcessor denormalizeProcessor(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID)
    throws ModuleException, NotFoundException, GenericException {

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    DenormalizeConfiguration config = loadConfiguration(databaseUUID, tableUUID);

    return new DenormalizeProcessor(solrManager, config, database, databaseUUID);
  }

  @Bean
  @StepScope
  public DenormalizeWriter denormalizeWriter(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {
    return new DenormalizeWriter(solrManager, databaseUUID);
  }

  @Bean
  public Step denormalizeProcessingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    SolrCursorItemReader reader, DenormalizeProcessor processor, DenormalizeWriter writer,
    ProgressChunkListener progressListener, @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {

    return new StepBuilder("denormalizeProcessingStep",
      jobRepository).<ViewerRow, DenormalizeProcessor.NestedDocumentWrapper> chunk(200, transactionManager)
      .reader(reader).processor(processor).writer(writer).listener((StepExecutionListener) progressListener)
      .listener((ChunkListener) progressListener).taskExecutor(taskExecutor).build();
  }

  @Bean
  public Job denormalizeJob(JobRepository jobRepository, Step denormalizeCleanupStep, Step denormalizeProcessingStep) {
    return new JobBuilder("denormalizeJob", jobRepository).listener(jobListener).start(denormalizeCleanupStep)
      .next(denormalizeProcessingStep).build();
  }

  private DenormalizeConfiguration loadConfiguration(String databaseUUID, String tableUUID) throws ModuleException {
    Path path = Paths.get(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION);
    Path configurationPath = ViewerConfiguration.getInstance().getDatabasesPath().resolve(databaseUUID)
      .resolve(path);

    if (configurationPath.toFile().exists()) {
      return JsonTransformer.readObjectFromFile(configurationPath, DenormalizeConfiguration.class);
    } else {
      throw new ModuleException().withMessage("Configuration file not exist: " + configurationPath.toString());
    }
  }
}
