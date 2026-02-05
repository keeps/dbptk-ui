package com.databasepreservation.common.server.batch.config;

import com.databasepreservation.common.server.batch.steps.common.listners.ProgressChunkListener;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class BatchJobConfig {

  private final JobRepository jobRepository;

  public BatchJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public DatabaseRowsSolrManager databaseRowsSolrManager() {
    return ViewerFactory.getSolrManager();
  }

  @Bean(name = "batchTaskExecutor")
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setRejectedExecutionHandler(new RejectedTaskHandler());

    taskExecutor.setCorePoolSize(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(5,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_CORE_POOL_SIZE));
    taskExecutor.setMaxPoolSize(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(Integer.MAX_VALUE,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_MAX_POOL_SIZE));
    taskExecutor.setQueueCapacity(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(Integer.MAX_VALUE,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_QUEUE_SIZE));

    taskExecutor.setThreadNamePrefix("Batch-Worker-");
    taskExecutor.initialize();
    return taskExecutor;
  }

  @Bean(name = "customJobLauncher")
  public JobLauncher jobLauncher(@Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor taskExecutor) throws Exception {
    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();

    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.setTaskExecutor(taskExecutor);

    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }

  @Bean(name = "customJobOperator")
  public SimpleJobOperator jobOperator(JobExplorer jobExplorer, JobRegistry jobRegistry,
    @Qualifier("customJobLauncher") JobLauncher jobLauncher) throws Exception {
    SimpleJobOperator jobOperator = new SimpleJobOperator();
    jobOperator.setJobExplorer(jobExplorer);
    jobOperator.setJobRepository(jobRepository);
    jobOperator.setJobRegistry(jobRegistry);
    jobOperator.setJobLauncher(jobLauncher);
    jobOperator.afterPropertiesSet();
    return jobOperator;
  }

  @Bean
  @StepScope
  public CollectionStatus collectionStatus(
    @Value("#{jobExecutionContext['COLLECTION_STATUS_CONFIG']}") CollectionStatus collectionStatus) {
    if (collectionStatus == null) {
      throw new IllegalStateException("Collection status not found in job execution context.");
    }
    return collectionStatus;
  }
}
