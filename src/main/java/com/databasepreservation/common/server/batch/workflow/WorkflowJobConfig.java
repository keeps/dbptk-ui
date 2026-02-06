package com.databasepreservation.common.server.batch.workflow;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.batch.steps.common.listners.JobListener;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class WorkflowJobConfig {

  public static final String JOB_NAME = "processWorkflowJob";

  @Autowired
  private ConfigLoaderTasklet configLoaderTasklet;

  @Autowired
  private JobListener jobListener;

  @Autowired
  @Qualifier("denormalizeStep")
  private Step denormalizeStep;

  @Autowired
  @Qualifier("virtualColumnStep")
  private Step virtualColumnStep;

  @Bean
  public Job processWorkflowJob(JobRepository jobRepository, Step configLoaderStep) {
    return new JobBuilder(JOB_NAME, jobRepository).start(configLoaderStep).listener(jobListener).next(virtualColumnStep)
      .next(denormalizeStep).build();
  }

  @Bean
  public Step configLoaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("configLoaderStep", jobRepository).tasklet(configLoaderTasklet, transactionManager).build();
  }
}
