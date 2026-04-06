package com.databasepreservation.common.server.batch.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
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
  private final JobExplorer jobExplorer;
  private final JobLauncher jobLauncher;
  private final StepFactory stepFactory;
  private final ContextResolver contextResolver;
  private final JobContextRegistry contextRegistry;

  public JobOrchestrator(JobRepository jobRepository, JobExplorer jobExplorer, StepFactory stepFactory,
    ContextResolver contextResolver, JobContextRegistry contextRegistry,
    @Qualifier(BatchConstants.JOB_LAUNCHER_BEAN_NAME) JobLauncher jobLauncher) {
    this.jobRepository = jobRepository;
    this.jobExplorer = jobExplorer;
    this.stepFactory = stepFactory;
    this.contextResolver = contextResolver;
    this.contextRegistry = contextRegistry;
    this.jobLauncher = jobLauncher;
  }

  public JobExecution launchJob(String databaseUUID, String collectionUUID, JobDefinition jobDefinition)
    throws BatchJobException {
    try {
      String expectedJobName = jobDefinition.getName() + "-" + databaseUUID;

      // Check if there is an active job for the same database by looking into the
      // context registry
      if (contextRegistry.get(databaseUUID) != null) {
        throw new BatchJobException("A batch job is already actively modifying this database.");
      }

      Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions(expectedJobName);
      if (!runningExecutions.isEmpty()) {
        throw new BatchJobException(
          "A job of type '" + jobDefinition.getDisplayName() + "' is already running on this database.");
      }

      String jobUUID = SolrUtils.randomUUID();
      JobParametersBuilder jobBuilder = new JobParametersBuilder();
      jobBuilder.addString(BatchConstants.JOB_UUID_KEY, jobUUID);
      jobBuilder.addString(BatchConstants.COLLECTION_UUID_KEY, collectionUUID);
      jobBuilder.addString(BatchConstants.DATABASE_UUID_KEY, databaseUUID);
      jobBuilder.addString(BatchConstants.JOB_DISPLAY_NAME_KEY, jobDefinition.getDisplayName());
      JobParameters jobParameters = jobBuilder.toJobParameters();

      JobController.addMinimalSolrBatchJob(jobParameters, contextResolver.resolve(databaseUUID));
      Job job = buildJob(databaseUUID, jobDefinition);

      JobExecution jobExecution = jobLauncher.run(job, jobParameters);

      JobController.editSolrBatchJob(jobExecution, null);

      return jobExecution;
    } catch (BatchJobException e) {
      throw e; // Re-throw to be handled by the controller
    } catch (TaskRejectedException e) {
      LOGGER.error("Job queue is full. Could not launch job {} for database {}", jobDefinition.getName(), databaseUUID);
      throw new BatchJobException("Queue is full, please try later", e);
    } catch (Exception e) {
      LOGGER.error("Failed to launch job {} for database {}: {}", jobDefinition.getName(), databaseUUID, e.getMessage(),
        e);
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
    long totalWorkload = calculateTotalWorkload(jobDefinition.getSteps(), context)
      + calculateTotalWorkload(jobDefinition.getPostProcessingSteps(), context);
    context.getJobProgressAggregator().setTotalItems(totalWorkload);

    LOGGER.info("[ORCHESTRATOR] Building Job '{}' for database {}. Total Workload estimated: {}",
      jobDefinition.getDisplayName(), databaseUUID, totalWorkload);

    // Start building the Job
    JobBuilder jobBuilder = new JobBuilder(jobDefinition.getName() + "-" + databaseUUID, jobRepository);
    jobBuilder.listener(new JobStatusListener(context, contextRegistry, jobRepository));

    List<String> includedStepNames = new ArrayList<>();

    // Isolate logic in independent flows
    Flow mainFlow = buildFlow(jobDefinition.getSteps(), context, "mainFlow", includedStepNames);
    Flow postProcessingFlow = buildFlow(jobDefinition.getPostProcessingSteps(), context, "postProcessingFlow",
      includedStepNames);

    context.setTotalSteps(includedStepNames.isEmpty() ? 1 : includedStepNames.size());
    context.setStepNames(includedStepNames);

    LOGGER.info("[ORCHESTRATOR] Job flow built with {} executable steps.", includedStepNames.size());

    // Evaluate structural boundary conditions
    if (mainFlow == null && postProcessingFlow == null)
      return jobBuilder.start(createNoOpStep()).build();
    if (mainFlow == null)
      return jobBuilder.start(postProcessingFlow).end().build();
    if (postProcessingFlow == null)
      return jobBuilder.start(mainFlow).end().build();

    JobExecutionDecider failureDecider = (jobExecution, stepExecution) -> {
      boolean hasFailure = jobExecution.getStepExecutions().stream()
        .anyMatch(se -> se.getStatus() == BatchStatus.FAILED);
      return new FlowExecutionStatus(hasFailure ? "FAILED" : "COMPLETED");
    };

    // Ensures postProcessingFlow runs unconditionally.
    // If mainFlow fails, it runs postProcessingFlow and then forces the Job to
    // fail.
    Flow executionFlow = new FlowBuilder<SimpleFlow>("executionFlow").start(mainFlow).on("FAILED")
      .to(postProcessingFlow).from(mainFlow).on("*").to(postProcessingFlow).from(postProcessingFlow).on("*")
      .to(failureDecider).from(failureDecider).on("FAILED").to(createFailStep()).from(failureDecider).on("*").end()
      .build();

    return jobBuilder.start(executionFlow).end().build();
  }

  // Helper method to dynamically construct sequential Flows
  private Flow buildFlow(List<StepDefinition> definitions, JobContext context, String flowName,
    List<String> includedStepNames) {
    if (definitions == null || definitions.isEmpty())
      return null;

    FlowBuilder<SimpleFlow> flowBuilder = new FlowBuilder<>(flowName);
    boolean hasExecutableSteps = false;

    for (StepDefinition definition : definitions) {
      if (definition.getExecutionPolicy().shouldExecute(context)) {
        LOGGER.info("[ORCHESTRATOR] [+] INCLUDED step: {}", definition.getDisplayName());
        includedStepNames.add(definition.getDisplayName());

        Step step = stepFactory.build(definition, context);
        if (!hasExecutableSteps) {
          flowBuilder.start(step);
          hasExecutableSteps = true;
        } else {
          flowBuilder.next(step);
        }
      } else {
        LOGGER.info("[ORCHESTRATOR] [-] SKIPPED step: {} (ExecutionPolicy evaluated to false)",
          definition.getDisplayName());
      }
    }
    return hasExecutableSteps ? flowBuilder.build() : null;
  }

  // Creates a Step that forces the Job to fail after post-processing if the main
  // flow failed.
  private Step createFailStep() {
    return new StepBuilder("failStep", jobRepository).tasklet((contribution, chunkContext) -> {
      contribution.setExitStatus(ExitStatus.FAILED);
      throw new BatchJobException("Main flow failed.");
    }, new ResourcelessTransactionManager()).build();
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
      .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED, new ResourcelessTransactionManager()).build();
  }

  public List<String> getJobPlan(String databaseUUID, JobDefinition jobDefinition) throws BatchJobException {
    try {
      JobContext context = contextResolver.resolve(databaseUUID);

      List<String> plannedSteps = new ArrayList<>();

      List<StepDefinition> allSteps = new ArrayList<>(jobDefinition.getSteps());
      if (jobDefinition.getPostProcessingSteps() != null) {
        allSteps.addAll(jobDefinition.getPostProcessingSteps());
      }

      for (StepDefinition definition : allSteps) {
        if (definition.getExecutionPolicy().shouldExecute(context)) {
          plannedSteps.add(definition.getDisplayName());
        }
      }

      return plannedSteps;
    } catch (Exception e) {
      LOGGER.error("Failed to generate job plan for database {}: {}", databaseUUID, e.getMessage(), e);
      throw new BatchJobException("Failed to generate job plan", e);
    }
  }
}
