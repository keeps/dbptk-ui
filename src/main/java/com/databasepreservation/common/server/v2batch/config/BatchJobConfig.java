package com.databasepreservation.common.server.v2batch.config;

import com.databasepreservation.common.server.v2batch.job.JobContext;
import com.databasepreservation.common.server.v2batch.steps.StepDefinition;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

import java.util.Map;

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

  @Bean
  @StepScope
  public <I> ItemStreamReader<I> itemReader(
          @Value("#{stepExecutionContext}") Map<String, Object> stepContextMap,
          StepDefinition<I, ?> definition,
          JobContext jobContext) {

    // O Spring injeta o contexto específico da partição automaticamente aqui
    ExecutionContext executionContext = new ExecutionContext(stepContextMap);

    // Retornamos um novo wrapper (ou o próprio leitor) que será privado desta thread
    return new ItemStreamReader<I>() {
      private ItemReader<I> delegate;

      @Override
      public void open(ExecutionContext context) {
        this.delegate = definition.getReader(jobContext, executionContext);
        if (this.delegate instanceof ItemStream) {
          ((ItemStream) this.delegate).open(executionContext);
        }
      }

      @Override
      public I read() throws Exception { return delegate.read(); }

      @Override
      public void update(ExecutionContext ctx) {}
      @Override
      public void close() {}
    };
  }
}
