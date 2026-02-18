package com.databasepreservation.common.server.batchv2.registry;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.batchv2.common.StepDefinition;
import com.databasepreservation.common.server.batchv2.infra.StepFactory;
import com.databasepreservation.common.server.batchv2.common.TaskContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class WorkflowOrchestrator {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final StepFactory stepFactory;
  private final ContextResolver contextResolver;
  private final JobProgressAggregatorV2 progressAggregator;

  public WorkflowOrchestrator(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    StepFactory stepFactory, ContextResolver contextResolver, JobProgressAggregatorV2 progressAggregator) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
    this.stepFactory = stepFactory;
    this.contextResolver = contextResolver;
    this.progressAggregator = progressAggregator;
  }

  public Job buildJob(String databaseUUID, List<StepDefinition<?, ?>> stepDefinitions) {
    TaskContext context = contextResolver.resolve(databaseUUID);

    JobBuilder jobBuilder = new JobBuilder("job-" + databaseUUID, jobRepository);
    List<Step> stepsToExecute = new ArrayList<>();

    for (StepDefinition<?, ?> definition : stepDefinitions) {
      if (definition.getExecutionPolicy().shouldExecute(context)) {

        registerWorkload(definition, context);

        Step step = stepFactory.build(definition, context);
        stepsToExecute.add(step);
      }
    }

    return buildFlow(jobBuilder, stepsToExecute);
  }

  private void registerWorkload(StepDefinition<?, ?> definition, TaskContext context) {
    definition.getPartitionStrategy().mapPartitions(context).keySet().forEach(tableId -> {
      long count = definition.getWorkloadEstimator().estimate(context, tableId);
      progressAggregator.registerPartition(tableId, count);
    });
  }

  private Job buildFlow(JobBuilder builder, List<Step> steps) {
    if (steps.isEmpty()) {
      return builder.start(createNoOpStep()).build();
    }

    SimpleJobBuilder flowBuilder = builder.start(steps.get(0));
    for (int i = 1; i < steps.size(); i++) {
      flowBuilder.next(steps.get(i));
    }

    return flowBuilder.build();
  }

  private Step createNoOpStep() {
    return new StepBuilder("noOpStep", jobRepository).tasklet((contribution, chunkContext) -> {
      return RepeatStatus.FINISHED;
    }, transactionManager).build();
  }
}
