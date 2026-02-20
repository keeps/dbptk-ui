package com.databasepreservation.common.server.v2batch.steps;

import java.util.Map;

import com.databasepreservation.common.server.v2batch.common.readers.SolrCursorItemReader;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.PartitionStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.v2batch.common.policy.ErrorPolicy;
import com.databasepreservation.common.server.v2batch.job.JobContext;
import com.databasepreservation.common.server.v2batch.job.SolrProgressFeedListener;
import com.databasepreservation.common.server.v2batch.steps.partition.PartitionStatusListener;
import com.databasepreservation.common.server.v2batch.steps.partition.PartitionStrategy;

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

  public StepFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  public <I, O> Step build(StepDefinition<I, O> definition, JobContext context) {
    Step workerStep = buildWorkerStep(definition, context);

    String stepName = definition.getName();
    StepBuilder stepBuilder = new StepBuilder(stepName, jobRepository);

    Partitioner partitioner = createPartitioner(definition, context);
    PartitionStepBuilder partitionBuilder = stepBuilder.partitioner(workerStep.getName(), partitioner);

    partitionBuilder.step(workerStep);
    partitionBuilder.taskExecutor(taskExecutor);

    StepStatusListener masterListener = new StepStatusListener(definition, context);
    partitionBuilder.listener(masterListener);

    return partitionBuilder.build();
  }

  private <I, O> Step buildWorkerStep(StepDefinition<I, O> definition, JobContext context) {
    String workerName = definition.getName() + "Worker";
    StepBuilder workerStepBuilder = new StepBuilder(workerName, jobRepository);

    SimpleStepBuilder<I, O> simpleBuilder = workerStepBuilder.<I, O> chunk(SolrCursorItemReader.getChunkSize(), transactionManager);

    ReaderWrapper<I> reader = new ReaderWrapper<>(definition, context);
    simpleBuilder.reader(reader);

    ProcessorWrapper<I, O> processor = new ProcessorWrapper<>(definition, context);
    simpleBuilder.processor(processor);

    simpleBuilder.writer(definition.getWriter(context));

    FaultTolerantStepBuilder<I, O> faultTolerantBuilder = simpleBuilder.faultTolerant();
    applyErrorPolicy(faultTolerantBuilder, definition.getErrorPolicy());

    // Listeners do Worker
    PartitionStatusListener partitionListener = new PartitionStatusListener(definition, context);
    faultTolerantBuilder.listener(partitionListener);

    SolrProgressFeedListener progress = new SolrProgressFeedListener(solrManager, context.getJobProgressAggregator());
    faultTolerantBuilder.listener((StepExecutionListener) progress);
    faultTolerantBuilder.listener((ChunkListener) progress);

    return faultTolerantBuilder.build();
  }

  private Partitioner createPartitioner(final StepDefinition<?, ?> definition, final JobContext context) {
    return new Partitioner() {
      @Override
      public Map<String, ExecutionContext> partition(int gridSize) {
        PartitionStrategy strategy = definition.getPartitionStrategy();
        Map<String, ExecutionContext> partitions = strategy.mapPartitions(context);

        for (Map.Entry<String, ExecutionContext> entry : partitions.entrySet()) {
          ExecutionContext execContext = entry.getValue();
          long count = strategy.calculateWorkload(context, execContext);
          execContext.putLong("totalRows", count);
        }
        return partitions;
      }
    };
  }

  private <I, O> void applyErrorPolicy(FaultTolerantStepBuilder<I, O> builder, ErrorPolicy policy) {
    builder.skipLimit(policy.getSkipLimit());
    if (policy.getSkipLimit() > 0) {
      for (Class<? extends Throwable> ex : policy.getSkippableExceptions()) {
        builder.skip(ex);
      }
    }

    builder.retryLimit(policy.getRetryLimit());
    for (Class<? extends Throwable> ex : policy.getRetryableExceptions()) {
      builder.retry(ex);
    }

  }
  // --- WRAPPERS ---

  private static class ReaderWrapper<I> implements ItemStreamReader<I> {
    private final StepDefinition<I, ?> definition;
    private final JobContext jobContext;
    private ItemReader<I> delegate;

    public ReaderWrapper(StepDefinition<I, ?> definition, JobContext jobContext) {
      this.definition = definition;
      this.jobContext = jobContext;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
      this.delegate = definition.getReader(jobContext, executionContext);
      if (this.delegate instanceof ItemStream) {
        ((ItemStream) this.delegate).open(executionContext);
      }
    }

    @Override
    public I read() throws Exception {
      return delegate.read();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
      if (this.delegate instanceof ItemStream) {
        ((ItemStream) this.delegate).update(executionContext);
      }
    }

    @Override
    public void close() throws ItemStreamException {
      if (this.delegate instanceof ItemStream) {
        ((ItemStream) this.delegate).close();
      }
    }
  }

  private static class ProcessorWrapper<I, O> implements ItemProcessor<I, O>, StepExecutionListener {
    private final StepDefinition<I, O> definition;
    private final JobContext jobContext;
    private ItemProcessor<I, O> delegate;

    public ProcessorWrapper(StepDefinition<I, O> definition, JobContext jobContext) {
      this.definition = definition;
      this.jobContext = jobContext;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
      this.delegate = definition.getProcessor(jobContext, stepExecution.getExecutionContext());
    }

    @Override
    public O process(I item) throws Exception {
      return delegate.process(item);
    }
  }
}
