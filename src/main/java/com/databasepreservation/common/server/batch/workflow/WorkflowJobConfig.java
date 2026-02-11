package com.databasepreservation.common.server.batch.workflow;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.batch.steps.common.listners.JobListener;
import com.databasepreservation.common.server.batch.steps.denormalize.DenormalizationStepPolicy;
import com.databasepreservation.common.server.batch.steps.virtualColumn.VirtualColumnStepPolicy;
import com.databasepreservation.common.server.batch.workflow.tasklets.ConfigLoaderTasklet;
import com.databasepreservation.common.server.batch.workflow.tasklets.GlobalEstimationTasklet;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class WorkflowJobConfig {

  public static final String JOB_NAME = "processWorkflowJob";

  @Autowired
  private ConfigLoaderTasklet configLoaderTasklet;

  @Autowired
  private GlobalEstimationTasklet globalEstimationTasklet;

  @Autowired
  private JobListener jobListener;

  @Autowired
  private DenormalizationStepPolicy denormalizationStepPolicy;

  @Autowired
  private VirtualColumnStepPolicy virtualColumnStepPolicy;

  @Autowired
  @Qualifier("denormalizeStep")
  private Step denormalizeStep;

  @Autowired
  @Qualifier("virtualColumnStep")
  private Step virtualColumnStep;

  // ===================================================================================================================
  // Workflow
  // ===================================================================================================================

  @Bean
  public Job processWorkflowJob(JobRepository jobRepository, Step configLoaderStep, Step estimationStep,
    Step denormalizeFlowStep, Step virtualColumnFlowStep) {
    return new JobBuilder(JOB_NAME, jobRepository).start(configLoaderStep).next(estimationStep)
      .next(denormalizeFlowStep).next(virtualColumnFlowStep).listener(jobListener).build();
  }

  @Bean
  public Step configLoaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("configLoaderStep", jobRepository).tasklet(configLoaderTasklet, transactionManager).build();
  }

  @Bean
  public Step estimationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("estimationStep", jobRepository).tasklet(globalEstimationTasklet, transactionManager)
      .build();
  }

  // ===================================================================================================================
  // Denormalization flow
  // ===================================================================================================================
  @Bean
  public JobExecutionDecider denormalizeDecider() {
    return new PolicyBasedDecider(denormalizationStepPolicy);
  }

  @Bean
  public Step denormalizeFlowStep(JobRepository jobRepository, Flow denormalizeFlow) {
    return new StepBuilder("denormalizeFlowStep", jobRepository).flow(denormalizeFlow).build();
  }

  @Bean
  public Flow denormalizeFlow(Step denormalizeStep, JobExecutionDecider denormalizeDecider) {
    return new FlowBuilder<Flow>("denormalizeFlow").start(denormalizeDecider).on("CONTINUE").to(denormalizeStep)
      .from(denormalizeDecider).on("SKIP").end().build();
  }

  // ===================================================================================================================
  // Virtual Columne flow
  // ===================================================================================================================
  @Bean
  public JobExecutionDecider virtualColumnDecider() {
    return new PolicyBasedDecider(virtualColumnStepPolicy);
  }

  @Bean
  public Step virtualColumnFlowStep(JobRepository jobRepository, Flow virtualColumnFlow) {
    return new StepBuilder("virtualColumnFlowStep", jobRepository).flow(virtualColumnFlow).build();
  }

  @Bean
  public Flow virtualColumnFlow(Step virtualColumnStep, JobExecutionDecider virtualColumnDecider) {
    return new FlowBuilder<Flow>("virtualColumnFlow").start(virtualColumnDecider).on("CONTINUE").to(virtualColumnStep)
      .from(virtualColumnDecider).on("SKIP").end().build();
  }

}
