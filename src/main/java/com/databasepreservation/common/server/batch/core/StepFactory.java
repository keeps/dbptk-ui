package com.databasepreservation.common.server.batch.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.listeners.PartitionStatusListener;
import com.databasepreservation.common.server.batch.listeners.SolrItemErrorListener;
import com.databasepreservation.common.server.batch.listeners.SolrProgressFeedListener;
import com.databasepreservation.common.server.batch.listeners.StepStatusListener;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * Factory responsible for assembling Spring Batch Steps utilizing SOLID
 * principles. It reads the capabilities (interfaces) of a StepDefinition to
 * dynamically build Chunks, Tasklets, and Partitioned steps.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class StepFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(StepFactory.class);

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Autowired
  private DatabaseRowsSolrManager solrManager;

  @Autowired
  @Qualifier(BatchConstants.TASK_EXECUTOR_BEAN_NAME)
  private TaskExecutor taskExecutor;

  // proxies for @StepScope support
  @Autowired
  private ItemReader<?> proxyReader;

  @Autowired
  private ItemProcessor<?, ?> proxyProcessor;

  @Autowired
  private ItemWriter<?> proxyWriter;

  @Autowired
  private Tasklet proxyTasklet;

  public StepFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  /**
   * Orchestrates the building process using Pattern Matching to identify
   * capabilities.
   */
  public Step build(StepDefinition definition, JobContext context) {

    ExecutionPolicy executionPolicy = definition.getExecutionPolicy();
    String stepName = definition.getName();

    // 1. Check if the step requires partitioning (How it scales)
    boolean isPartitioned = definition instanceof PartitionableStep;
    String workerName = isPartitioned ? stepName + BatchConstants.PARTITION_WORKER_NAME : stepName;

    // 2. Build the core worker step (What it does: Chunk or Tasklet)
    Step workerStep;
    if (definition instanceof ChunkStepDefinition<?, ?> chunkDef) {
      LOGGER.info("Building Chunk-oriented step for: {}", workerName);
      workerStep = buildChunkStep(chunkDef, workerName, context, executionPolicy, isPartitioned);
    } else if (definition instanceof TaskletStepDefinition taskletDef) {
      LOGGER.info("Building Tasklet-oriented step for: {}", workerName);
      workerStep = buildTaskletStep(taskletDef, workerName, context, isPartitioned);
    } else {
      throw new IllegalArgumentException("Unknown Step capability for: " + definition.getClass().getName());
    }

    // 3. Wrap in a partitioner if it implements PartitionableStep
    if (isPartitioned) {
      LOGGER.info("Wrapping step {} in a Partitioner (Master/Worker)", stepName);
      return buildPartitionedStep((PartitionableStep) definition, workerStep, stepName, context, executionPolicy);
    }

    // Otherwise, return the simple step directly
    return workerStep;
  }

  /**
   * Builds a Chunk-oriented step using the stepScope proxies.
   */
  @SuppressWarnings("unchecked")
  private Step buildChunkStep(ChunkStepDefinition<?, ?> definition, String stepName, JobContext context,
    ExecutionPolicy executionPolicy, boolean isPartitionedWorker) {

    StepBuilder stepBuilder = new StepBuilder(stepName, jobRepository);
    int chunkSize = executionPolicy.getChunkSize();

    SimpleStepBuilder<?, ?> simpleBuilder = stepBuilder.chunk(chunkSize, transactionManager);

    // Bind @StepScope proxies
    simpleBuilder.reader((ItemReader) proxyReader);
    simpleBuilder.processor((ItemProcessor) proxyProcessor);
    simpleBuilder.writer((ItemWriter) proxyWriter);

    FaultTolerantStepBuilder<?, ?> faultTolerantBuilder = simpleBuilder.faultTolerant();
    applyErrorPolicy(faultTolerantBuilder, definition.getErrorPolicy());
    applyListeners(faultTolerantBuilder, definition, context, isPartitionedWorker);

    return faultTolerantBuilder.build();
  }

  /**
   * Wraps a worker Step inside a partitioned Master Step.
   */
  private Step buildPartitionedStep(PartitionableStep partitionableDef, Step workerStep, String stepName,
    JobContext context, ExecutionPolicy executionPolicy) {

    StepBuilder masterBuilder = new StepBuilder(stepName, jobRepository);
    Partitioner partitioner = gridSize -> partitionableDef.getPartitionStrategy().mapPartitions(context);

    var partitionBuilder = masterBuilder.partitioner(workerStep.getName(), partitioner);
    partitionBuilder.step(workerStep);
    partitionBuilder.gridSize(executionPolicy.getConcurrencyLimit());
    partitionBuilder.taskExecutor(taskExecutor);

    // The master step gets the StepStatusListener to update global status
    partitionBuilder.listener(new StepStatusListener((StepDefinition) partitionableDef, context));

    return partitionBuilder.build();
  }

  /**
   * Builds a Tasklet-oriented step using the framework proxy. Note: Tasklets are
   * atomic and do not support chunk-based fault tolerance (skip/retry).
   */
  private Step buildTaskletStep(TaskletStepDefinition definition, String stepName, JobContext context,
    boolean isPartitionedWorker) {

    StepBuilder stepBuilder = new StepBuilder(stepName, jobRepository);
    TaskletStepBuilder taskletBuilder = stepBuilder.tasklet(proxyTasklet, transactionManager);

    applyListeners(taskletBuilder, definition, context, isPartitionedWorker);

    return taskletBuilder.build();
  }

  /**
   * Applies Fault Tolerance (Retry and Skip) based on ErrorPolicy. This is ONLY
   * called for Chunk-oriented steps.
   */
  private void applyErrorPolicy(FaultTolerantStepBuilder<?, ?> builder, ErrorPolicy policy) {
    if (policy == null)
      return;

    builder.skipLimit(policy.getSkipLimit());
    for (Class<? extends Throwable> skippable : policy.getSkippableExceptions()) {
      builder.skip(skippable);
    }

    builder.retryLimit(policy.getRetryLimit());
    for (Class<? extends Throwable> retryable : policy.getRetryableExceptions()) {
      builder.retry(retryable);
    }
  }

  /**
   * Applies common listeners to track progress and state.
   * 
   */
  @SuppressWarnings("rawtypes")
  private void applyListeners(AbstractTaskletStepBuilder builder, StepDefinition definition, JobContext context,
    boolean isPartitionedWorker) {

    SolrProgressFeedListener progressListener = new SolrProgressFeedListener(solrManager, context);

    builder.listener((StepExecutionListener) progressListener);
    builder.listener((ChunkListener) progressListener);

    SolrItemErrorListener<Object, Object> errorListener = new SolrItemErrorListener<>(solrManager);
    builder.listener((StepExecutionListener) errorListener);
    builder.listener((ItemReadListener<Object>) errorListener);
    builder.listener((ItemProcessListener<Object, Object>) errorListener);
    builder.listener((ItemWriteListener<Object>) errorListener);

    // Partition status is always tracked at the worker level
    if (definition instanceof PartitionableStep partitionable) {
      builder.listener(new PartitionStatusListener(partitionable, context));
    }

    // If it is NOT a partitioned worker, it must update the global step status
    // itself
    if (!isPartitionedWorker) {
      builder.listener(new StepStatusListener(definition, context));
    }
  }
}
