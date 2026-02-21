package com.databasepreservation.common.server.batch.config;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.context.JobContextRegistry;
import com.databasepreservation.common.server.batch.core.StepDefinition;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class StepComponentsConfig {
  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private JobContextRegistry contextRegistry;

  @Bean
  @StepScope
  public ItemReader<ViewerRow> frameworkReader(@Value("#{stepExecution.stepName}") String stepName,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{stepExecutionContext}") Map<String, Object> partitionCtx) {

    JobContext ctx = contextRegistry.get(databaseUUID);
    return getDefinition(stepName).createReader(ctx, new ExecutionContext(partitionCtx));
  }

  @Bean
  @StepScope
  public ItemProcessor<ViewerRow, ?> frameworkProcessor(@Value("#{stepExecution.stepName}") String stepName,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{stepExecutionContext}") Map<String, Object> partitionCtx) {

    JobContext ctx = contextRegistry.get(databaseUUID);
    return getDefinition(stepName).createProcessor(ctx, new ExecutionContext(partitionCtx));
  }

  @Bean
  @StepScope
  public ItemWriter<?> frameworkWriter(@Value("#{stepExecution.stepName}") String stepName,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {

    JobContext ctx = contextRegistry.get(databaseUUID);
    return getDefinition(stepName).createWriter(ctx);
  }

  @SuppressWarnings("unchecked")
  private StepDefinition<ViewerRow, ?> getDefinition(String stepName) {
    String beanName = stepName.split(":")[0].replace("Worker", "");
    return applicationContext.getBean(beanName, StepDefinition.class);
  }
}
