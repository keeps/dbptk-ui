package com.databasepreservation.common.server.batch.config;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class BatchJobConfig {
  @Bean(name = BatchConstants.TASK_EXECUTOR_BEAN_NAME)
  public TaskExecutor batchTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(5,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_CORE_POOL_SIZE));
    executor.setMaxPoolSize(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(Integer.MAX_VALUE,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_MAX_POOL_SIZE));
    executor.setQueueCapacity(ViewerConfiguration.getInstance().getViewerConfigurationAsInt(Integer.MAX_VALUE,
      ViewerConfiguration.PROPERTY_BATCH_JOBS_QUEUE_SIZE));

    executor.setThreadNamePrefix(BatchConstants.TASK_EXECUTOR_THREAD_NAME_PREFIX);
    executor.initialize();
    return executor;
  }

  @Bean(name = BatchConstants.JOB_LAUNCHER_BEAN_NAME)
  public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.setTaskExecutor(batchTaskExecutor());
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }

  /*
   * Solr manager bean to be injected in batch steps and components
   */
  @Bean
  public DatabaseRowsSolrManager solrManager() {
    return ViewerFactory.getSolrManager();
  }

  @Bean
  public ExecutionContextSerializer executionContextSerializer() {
    return new Jackson2ExecutionContextStringSerializer();
  }
}
