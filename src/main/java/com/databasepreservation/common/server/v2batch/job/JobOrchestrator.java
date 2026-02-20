package com.databasepreservation.common.server.v2batch.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.v2batch.steps.StepDefinition;
import com.databasepreservation.common.server.v2batch.steps.StepFactory;
import com.databasepreservation.common.server.v2batch.steps.partition.PartitionStrategy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class JobOrchestrator {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobOrchestrator.class);
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final StepFactory stepFactory;
  private final ContextResolver contextResolver;

  public JobOrchestrator(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    StepFactory stepFactory, ContextResolver contextResolver) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
    this.stepFactory = stepFactory;
    this.contextResolver = contextResolver;
  }

  public Job buildJob(String databaseUUID, List<StepDefinition<?, ?>> stepDefinitions) throws BatchJobException {
    JobContext context = contextResolver.resolve(databaseUUID);
    JobProgressAggregator aggregator = context.getJobProgressAggregator();

    long totalWorkload = 0;
    for (StepDefinition<?, ?> definition : stepDefinitions) {
      if (definition.getExecutionPolicy().shouldExecute(context)) {
        // Soma o workload de todas as partições deste Step
        totalWorkload += calculateStepWorkload(definition, context);
      }
    }

    if (totalWorkload == 0) {
      LOGGER.info(
        "No workload detected for job {}. This may indicate that all steps are already completed or that there is no data to process.",
        databaseUUID);
    }
    aggregator.setTotalItems(totalWorkload);

    JobBuilder jobBuilder = new JobBuilder("job-" + databaseUUID, jobRepository);
    jobBuilder.listener(new JobStatusListener(context));
    List<Step> stepsToExecute = new ArrayList<>();

    for (StepDefinition<?, ?> definition : stepDefinitions) {
      if (definition.getExecutionPolicy().shouldExecute(context)) {
        stepsToExecute.add(stepFactory.build(definition, context));
      }
    }

    return buildFlow(jobBuilder, stepsToExecute);
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

  private long calculateStepWorkload(StepDefinition<?, ?> definition, JobContext context) {
    PartitionStrategy strategy = definition.getPartitionStrategy();
    Map<String, ExecutionContext> partitions = strategy.mapPartitions(context);

    return partitions.values().stream().mapToLong(execContext -> strategy.calculateWorkload(context, execContext))
      .sum();
  }
}
