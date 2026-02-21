package com.databasepreservation.common.server.batch.core;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.components.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.listeners.PartitionStatusListener;
import com.databasepreservation.common.server.batch.listeners.SolrProgressFeedListener;
import com.databasepreservation.common.server.batch.listeners.StepStatusListener;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class StepFactory {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Autowired
  private DatabaseRowsSolrManager solrManager;

  @Autowired
  @Qualifier("batchTaskExecutor")
  private TaskExecutor taskExecutor;

  @Autowired
  private ItemReader<ViewerRow> frameworkReader;

  @Autowired
  private ItemProcessor<ViewerRow, ?> frameworkProcessor;

  @Autowired
  private ItemWriter<?> frameworkWriter;

  public StepFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  public <I, O> Step build(StepDefinition<I, O> definition, JobContext context) {
    Step workerStep = buildWorkerStep(definition, context);

    return new StepBuilder(definition.getName(), jobRepository)
      .partitioner(workerStep.getName(), createPartitioner(definition, context)).step(workerStep)
      .taskExecutor(taskExecutor).listener(new StepStatusListener(definition, context)).build();
  }

  @SuppressWarnings("unchecked")
  private <I, O> Step buildWorkerStep(StepDefinition<I, O> definition, JobContext context) {
    String workerName = definition.getName() + "Worker";

    SimpleStepBuilder<I, O> simpleBuilder = new StepBuilder(workerName, jobRepository)
      .<I, O> chunk(SolrCursorItemReader.getChunkSize(), transactionManager);

    // Injeção dos componentes @StepScope
    simpleBuilder.reader((ItemReader<I>) frameworkReader);
    simpleBuilder.processor((ItemProcessor<I, O>) frameworkProcessor);
    simpleBuilder.writer((ItemWriter<O>) frameworkWriter);

    // Configuração de tolerância a falhas (Skip/Retry)
    FaultTolerantStepBuilder<I, O> faultTolerantBuilder = simpleBuilder.faultTolerant();
    applyErrorPolicy(faultTolerantBuilder, definition.getErrorPolicy());

    SolrProgressFeedListener progressListener = new SolrProgressFeedListener(solrManager,
      context.getJobProgressAggregator());

    faultTolerantBuilder.listener((StepExecutionListener) progressListener);
    faultTolerantBuilder.listener((ChunkListener) progressListener);

    // Placeholder para Listeners de progresso (SolrProgressFeedListener)
    faultTolerantBuilder.listener(new PartitionStatusListener(definition, context));

    return faultTolerantBuilder.build();
  }

  private Partitioner createPartitioner(StepDefinition<?, ?> definition, JobContext context) {
    return gridSize -> definition.getPartitionStrategy().mapPartitions(context);
  }

  private <I, O> void applyErrorPolicy(FaultTolerantStepBuilder<I, O> builder, ErrorPolicy policy) {
    builder.skipLimit(policy.getSkipLimit());
    policy.getSkippableExceptions().forEach(builder::skip);
    builder.retryLimit(policy.getRetryLimit());
    policy.getRetryableExceptions().forEach(builder::retry);
  }
}
