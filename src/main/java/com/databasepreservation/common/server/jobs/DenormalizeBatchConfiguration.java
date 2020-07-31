package com.databasepreservation.common.server.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.databasepreservation.common.server.ViewerConfiguration;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@EnableBatchProcessing
@Configuration
public class DenormalizeBatchConfiguration {

  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

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
  public JobLauncher jobLauncher(@Qualifier("customTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
    JobRepository jobRepository) {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setTaskExecutor(taskExecutor);
    jobLauncher.setJobRepository(jobRepository);
    return jobLauncher;
  }

  @Bean(name = "denormalizeJob")
  public Job denormalizeJob(JobListener listener, Step step1) {
    return jobBuilderFactory.get("denormalizeJob").incrementer(new RunIdIncrementer()).listener(listener).flow(step1)
      .end().build();
  }

  @Bean
  public Step step1() {
    return stepBuilderFactory.get("step1").tasklet(new DenormalizeProcessor()).build();
  }

  @Bean(name = "customJobOperator")
  public SimpleJobOperator jobOperator(JobExplorer jobExplorer, JobRepository jobRepository, JobRegistry jobRegistry,
    @Qualifier("customJobLauncher") JobLauncher jobLauncher) {
    SimpleJobOperator jobOperator = new SimpleJobOperator();

    jobOperator.setJobExplorer(jobExplorer);
    jobOperator.setJobRepository(jobRepository);
    jobOperator.setJobRegistry(jobRegistry);
    jobOperator.setJobLauncher(jobLauncher);

    return jobOperator;
  }
}
