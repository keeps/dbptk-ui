/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.databasepreservation.common.server.ViewerConfiguration;

/**
 * Configuration for the Denormalize Batch Job.
 */
@Configuration
public class DenormalizeBatchConfiguration {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Autowired
  public DenormalizeBatchConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  @Bean(name = "customTaskExecutor")
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    RejectedTaskHandler handler = new RejectedTaskHandler();
    taskExecutor.setRejectedExecutionHandler(handler);
    taskExecutor.setCorePoolSize(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(5,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_CORE_POOL_SIZE));
    taskExecutor.setMaxPoolSize(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(Integer.MAX_VALUE,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_MAX_POOL_SIZE));
    taskExecutor.setQueueCapacity(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(Integer.MAX_VALUE,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_QUEUE_SIZE));
    return taskExecutor;
  }

  @Bean(name = "customJobLauncher")
  public JobLauncher jobLauncher(@Qualifier("customTaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setTaskExecutor(taskExecutor);
    jobLauncher.setJobRepository(jobRepository);
    return jobLauncher;
  }

  @Bean(name = "denormalizeJob")
  public Job denormalizeJob(JobListener listener, Step step1) {
    return new JobBuilder("denormalizeJob", jobRepository).incrementer(new RunIdIncrementer()).listener(listener)
      .start(step1).build();
  }

  @Bean
  public Step step1() {
    return new StepBuilder("step1", jobRepository).tasklet(new DenormalizeProcessor(), transactionManager).build();
  }

  @Bean(name = "customJobOperator")
  public SimpleJobOperator jobOperator(JobExplorer jobExplorer, JobRegistry jobRegistry,
    @Qualifier("customJobLauncher") JobLauncher jobLauncher) {
    SimpleJobOperator jobOperator = new SimpleJobOperator();

    jobOperator.setJobExplorer(jobExplorer);
    jobOperator.setJobRepository(jobRepository);
    jobOperator.setJobRegistry(jobRegistry);
    jobOperator.setJobLauncher(jobLauncher);

    return jobOperator;
  }
}
