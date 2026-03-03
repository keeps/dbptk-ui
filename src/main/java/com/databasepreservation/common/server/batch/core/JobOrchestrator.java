package com.databasepreservation.common.server.batch.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.server.batch.context.ContextResolver;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.context.JobContextRegistry;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.listeners.JobStatusListener;
import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.index.utils.SolrUtils;

/**
 * Orchestrates the creation of a Spring Batch Job based on a list of
 * StepDefinitions. It calculates the total workload for progress tracking and
 * builds the execution flow.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class JobOrchestrator {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobOrchestrator.class);

  private final JobRepository jobRepository;
  private final JobLauncher jobLauncher;
  private final StepFactory stepFactory;
  private final ContextResolver contextResolver;
  private final JobContextRegistry contextRegistry;

  public JobOrchestrator(JobRepository jobRepository, StepFactory stepFactory, ContextResolver contextResolver,
    JobContextRegistry contextRegistry, @Qualifier(BatchConstants.JOB_LAUNCHER_BEAN_NAME) JobLauncher jobLauncher) {
    this.jobRepository = jobRepository;
    this.stepFactory = stepFactory;
    this.contextResolver = contextResolver;
    this.contextRegistry = contextRegistry;
    this.jobLauncher = jobLauncher;
  }

  public JobExecution launchJob(String databaseUUID, String collectionUUID, JobDefinition jobDefinition)
    throws BatchJobException {
    try {
      // TODO: check if there is no job running for database
      // for (JobExecution runningJobExecution :
      // jobExplorer.findRunningJobExecutions("processWorkflowJob")) {
      // if
      // (runningJobExecution.getJobParameters().getString(BatchConstants.DATABASE_UUID_KEY)
      // .equals(databaseUUID)) {
      // throw new BatchJobException(new AlreadyExistsException("A job is already
      // running
      // on this database"));
      // }
      // }
      //

      String jobUUID = SolrUtils.randomUUID();
      JobParametersBuilder jobBuilder = new JobParametersBuilder();
      jobBuilder.addString(BatchConstants.JOB_UUID_KEY, jobUUID);
      jobBuilder.addString(BatchConstants.COLLECTION_UUID_KEY, collectionUUID);
      jobBuilder.addString(BatchConstants.DATABASE_UUID_KEY, databaseUUID);
      jobBuilder.addString(BatchConstants.JOB_DISPLAY_NAME_KEY, jobDefinition.getDisplayName());
      JobParameters jobParameters = jobBuilder.toJobParameters();

      JobController.addMinimalSolrBatchJob(jobParameters);

      Job job = buildJob(databaseUUID, jobDefinition);

      JobExecution jobExecution = jobLauncher.run(job, jobParameters);

      JobController.editSolrBatchJob(jobExecution, null);
      if (jobExecution.getStatus() == BatchStatus.FAILED) {
        JobController.setMessageToSolrBatchJob(jobExecution, "Queue is full, please try later");
      }

      return jobExecution;

    } catch (Exception e) {
      LOGGER.error("Failed to launch job {} for database {}: {}", jobDefinition.getName(), databaseUUID, e.getMessage(), e);
      throw new BatchJobException("Failed to launch job " + jobDefinition.getName(), e);
    }
  }

  /**
   * Builds a Spring Batch Job based on the provided StepDefinitions. It resolves
   * the JobContext,
   */
  private Job buildJob(String databaseUUID, JobDefinition jobDefinition) throws BatchJobException {
    // Resolve and Register the Context (To be accessible via @StepScope)
    JobContext context = contextResolver.resolve(databaseUUID);
    contextRegistry.register(databaseUUID, context);

    // Calculate the Total Workload for progress tracking
    long totalWorkload = calculateTotalWorkload(jobDefinition.getSteps(), context);
    context.getJobProgressAggregator().setTotalItems(totalWorkload);

    // Start building the Job
    JobBuilder jobBuilder = new JobBuilder(jobDefinition.getName() + "-" + databaseUUID, jobRepository);
    jobBuilder.listener(new JobStatusListener(context, contextRegistry));

    SimpleJobBuilder flowBuilder = null;
    int executableStepsCount = 0;

    // Add Steps that should be executed according to their policy
    for (StepDefinition definition : jobDefinition.getSteps()) {
      if (definition.getExecutionPolicy().shouldExecute(context)) {

        Step step = stepFactory.build(definition, context);
        executableStepsCount++;

        if (flowBuilder == null) {
          flowBuilder = jobBuilder.start(step);
        } else {
          flowBuilder.next(step);
        }
      }
    }

    // Set the dynamically calculated values back into the context
    context.setTotalSteps(executableStepsCount == 0 ? 1 : executableStepsCount);
    context.getJobProgressAggregator().setTotalItems(totalWorkload);

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
