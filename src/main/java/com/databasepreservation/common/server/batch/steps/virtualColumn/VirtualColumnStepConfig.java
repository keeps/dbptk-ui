package com.databasepreservation.common.server.batch.steps.virtualColumn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.batch.steps.common.listners.ProgressChunkListener;
import com.databasepreservation.common.server.batch.steps.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class VirtualColumnStepConfig {

  @Bean("virtualColumnStep")
  public Step virtualColumnStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    @Qualifier("virtualColumnReader") SolrCursorItemReader reader,
    @Qualifier("virtualColumnProcessor") ItemProcessor<Object, Object> processor,
    @Qualifier("virtualColumnWriter") ItemWriter<Object> writer, ProgressChunkListener progressListener) {

    return new StepBuilder("virtualColumnStep", jobRepository).<Object, Object> chunk(1000, transactionManager)
      .reader(reader).processor(processor)
      .writer(writer)
      .listener((ChunkListener) progressListener).build();
  }

  @Bean
  @StepScope
  public SolrCursorItemReader virtualColumnReader(CollectionStatus collectionStatus,
    DatabaseRowsSolrManager solrManager,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);

    Filter filter = new Filter();

    return new SolrCursorItemReader(solrManager, databaseUUID, filter, fieldsToReturn);
  }

  @Bean("virtualColumnProcessor")
  @StepScope
  public ItemProcessor<Object, Object> virtualColumnProcessor() {
    return item -> item;
  }

  @Bean("virtualColumnWriter")
  @StepScope
  public ItemWriter<Object> virtualColumnWriter() {
    return chunk -> {
    };
  }
}
