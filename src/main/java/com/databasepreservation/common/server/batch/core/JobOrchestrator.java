package com.databasepreservation.common.server.batch.core;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.server.batch.context.ContextResolver;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.context.JobContextRegistry;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.listeners.JobStatusListener;

/**
 * Orchestrates the creation of a Spring Batch Job based on a list of
 * StepDefinitions. It calculates the total workload for progress tracking and
 * builds the execution flow.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class JobOrchestrator {
  private final JobRepository jobRepository;
  private final StepFactory stepFactory;
  private final ContextResolver contextResolver;
  private final JobContextRegistry contextRegistry;

  public JobOrchestrator(JobRepository jobRepository, StepFactory stepFactory, ContextResolver contextResolver,
    JobContextRegistry contextRegistry) {
    this.jobRepository = jobRepository;
    this.stepFactory = stepFactory;
    this.contextResolver = contextResolver;
    this.contextRegistry = contextRegistry;
  }

  /**
   * Builds a Spring Batch Job based on the provided StepDefinitions. It resolves
   * the JobContext,
   */
  public Job buildJob(String databaseUUID, List<StepDefinition> stepDefinitions) throws BatchJobException {
    // Resolve and Register the Context (To be accessible via @StepScope)
    JobContext context = contextResolver.resolve(databaseUUID);
    contextRegistry.register(databaseUUID, context);

    // Calculate the Total Workload for progress tracking
    long totalWorkload = calculateTotalWorkload(stepDefinitions, context);
    context.getJobProgressAggregator().setTotalItems(totalWorkload);

    // Start building the Job
    JobBuilder jobBuilder = new JobBuilder("job-" + databaseUUID, jobRepository);
    jobBuilder.listener(new JobStatusListener(context, contextRegistry));

    SimpleJobBuilder flowBuilder = null;

    // Add Steps that should be executed according to their policy
    for (StepDefinition definition : stepDefinitions) {
      if (definition.getExecutionPolicy().shouldExecute(context)) {

        Step step = stepFactory.build(definition, context);

        if (flowBuilder == null) {
          flowBuilder = jobBuilder.start(step);
        } else {
          flowBuilder.next(step);
        }
      }
    }

    return (flowBuilder != null) ? flowBuilder.build() : jobBuilder.start(createNoOpStep()).build();
  }

  /**
   * Calculates the total workload cleanly by delegating the responsibility to
   * each individual StepDefinition.
   */
  private long calculateTotalWorkload(List<StepDefinition> definitions, JobContext context) {
    long sum = 0L;
    for (StepDefinition def : definitions) {
      if (def.getExecutionPolicy().shouldExecute(context)) {
        long l = def.calculateWorkload(context);
        sum += l;
      }
    }
    return sum;
  }

  private Step createNoOpStep() {
    // No-op step in case there are no steps to execute. This ensures the Job can be
    // created even without Steps, avoiding configuration errors.
    return new StepBuilder("noOpStep", jobRepository)
      .tasklet((contribution, chunkContext) -> org.springframework.batch.repeat.RepeatStatus.FINISHED,
        new org.springframework.batch.support.transaction.ResourcelessTransactionManager())
      .build();
  }
}
