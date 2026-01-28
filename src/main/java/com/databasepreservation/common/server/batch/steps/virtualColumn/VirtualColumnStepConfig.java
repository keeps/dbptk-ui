package com.databasepreservation.common.server.batch.steps.virtualColumn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.steps.common.JobProgressAggregator;
import com.databasepreservation.common.server.batch.steps.common.listners.SolrProgressFeedListener;
import com.databasepreservation.common.server.batch.steps.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.batch.steps.common.writers.SolrItemWriter;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class VirtualColumnStepConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualColumnStepConfig.class);

  @Autowired
  private CollectionStatus collectionStatus;

  @Autowired
  private JobProgressAggregator aggregator;

  @Bean
  @StepScope
  public SolrProgressFeedListener virtualColumnProgressListener(DatabaseRowsSolrManager solrManager) {
    return new SolrProgressFeedListener(solrManager, aggregator);
  }

  @Bean("virtualColumnStep")
  public Step virtualColumnStep(JobRepository jobRepository, Step virtualColumnsTablePartitionerStep,
    CollectionStatus status) {

    return new StepBuilder("virtualColumnStep", jobRepository)
      .partitioner("tablePartitionerStep", new VirtualColumnStepTablePartitioner(status))
      .step(virtualColumnsTablePartitionerStep).taskExecutor(new SyncTaskExecutor()).gridSize(1).build();
  }

  // Step
  @Bean("virtualColumnsTablePartitionerStep")
  public Step virtualColumnsTablePartitionerStep(JobRepository jobRepository,
    PlatformTransactionManager transactionManager, @Qualifier("virtualColumnReader") SolrCursorItemReader reader,
    VirtualColumnStepProcessor processor, SolrItemWriter writer,
    SolrProgressFeedListener virtualColumnProgressListener) {

    return new StepBuilder("virtualColumnsTablePartitionerStep", jobRepository)
      .<ViewerRow, ViewerRow> chunk(1000, transactionManager).reader(reader).processor(processor).writer(writer)

      // Listeners
      .listener((StepExecutionListener) virtualColumnProgressListener)
      .listener((ChunkListener) virtualColumnProgressListener).build();
  }

  // ==================================================================================
  // 3. READER DINÂMICO (Lê apenas campos necessários da Tabela atual)
  // ==================================================================================
  @Bean
  @StepScope
  public SolrCursorItemReader virtualColumnReader(@Value("#{stepExecutionContext['tableId']}") String tableId,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    DatabaseRowsSolrManager solrManager) {

    LOGGER.info("Configuring Reader for Virtual Columns on table: {}", tableId);

    TableStatus tableStatus = collectionStatus.getTables().stream().filter(t -> t.getId().equals(tableId)).findFirst()
      .orElseThrow(() -> new RuntimeException("Table config not found for ID: " + tableId));

    Set<String> fieldsToReturn = new HashSet<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    if (tableStatus.getColumns() != null) {
      tableStatus.getColumns().stream().filter(c -> ViewerType.dbTypes.VIRTUAL.equals(c.getType()))
        .filter(c -> c.getVirtualColumnStatus() != null && c.getVirtualColumnStatus().shouldProcess()).forEach(c -> {
          List<String> sources = c.getVirtualColumnStatus().getSourceColumnsIds();
          if (sources != null) {
            fieldsToReturn.addAll(sources);
          }
        });
    }

    LOGGER.debug("Fetching fields for table {}: {}", tableId, fieldsToReturn);

    // Agora usamos o objeto real tableStatus, garantindo que getId() não devolve
    // null
    Filter filter = FilterUtils.filterByTable(new Filter(), tableStatus.getId());

    return new SolrCursorItemReader(solrManager, databaseUUID, filter, new ArrayList<>(fieldsToReturn));
  }

  // ==================================================================================
  // 4. PROCESSOR (Aplica a lógica da Coluna Virtual)
  // ==================================================================================
  @Bean
  @StepScope
  public VirtualColumnStepProcessor virtualColumnProcessor() {
    return new VirtualColumnStepProcessor(collectionStatus);
  }

  // ==================================================================================
  // 5. WRITER (Persiste no Solr)
  // ==================================================================================
  @Bean
  @StepScope
  public SolrItemWriter virtualColumnWriter(DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {

    return new SolrItemWriter(solrManager, databaseUUID);
  }
}
