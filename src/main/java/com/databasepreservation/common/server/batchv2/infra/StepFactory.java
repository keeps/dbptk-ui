package com.databasepreservation.common.server.batchv2.infra;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.batchv2.common.ErrorPolicy;
import com.databasepreservation.common.server.batchv2.common.StepDefinition;
import com.databasepreservation.common.server.batchv2.common.TaskContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class StepFactory {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  public StepFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  public <I, O> Step build(StepDefinition<I, O> definition, TaskContext context) {

    FaultTolerantStepBuilder<I, O> workerBuilder = new StepBuilder(definition.getName() + "Worker", jobRepository)
      .<I, O> chunk(1000, transactionManager).reader(definition.getReader(context))
      .processor(definition.getProcessor(context)).writer(definition.getWriter(context)).faultTolerant();

    ErrorPolicy policy = definition.getErrorPolicy();
    workerBuilder.skipLimit(policy.getSkipLimit());
    if (policy.getSkippableExceptions().isEmpty()) {
      workerBuilder.skip(Exception.class);
    } else {
      for (Class<? extends Throwable> ex : policy.getSkippableExceptions()) {
        workerBuilder.skip(ex);
      }
    }

    workerBuilder.retryLimit(policy.getRetryLimit());
    if (policy.getRetryableExceptions().isEmpty()) {
      workerBuilder.retry(Exception.class);
    } else {
      for (Class<? extends Throwable> ex : policy.getRetryableExceptions()) {
        workerBuilder.retry(ex);
      }
    }

    workerBuilder.listener(new ExecutionStatusListener(context));

    Step workerStep = workerBuilder.build();

    return new StepBuilder(definition.getName(), jobRepository)
      .partitioner(workerStep.getName(), gridSize -> definition.getPartitionStrategy().mapPartitions(context))
      .step(workerStep).build();
  }
}
