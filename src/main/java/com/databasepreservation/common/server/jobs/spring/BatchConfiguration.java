package com.databasepreservation.common.server.jobs.spring;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

//  @Bean
//  public JsonItemReader<CollectionConfiguration> jsonItemReader() {
//    ObjectMapper objectMapper = new ObjectMapper();
//
//    JacksonJsonObjectReader<CollectionConfiguration> jsonObjectReader = new JacksonJsonObjectReader<>(
//      CollectionConfiguration.class);
//    jsonObjectReader.setMapper(objectMapper);
//
//    return new JsonItemReaderBuilder<CollectionConfiguration>().jsonObjectReader(jsonObjectReader)
//      .resource(new ClassPathResource("")).name("tradeJsonItemReader").build();
//  }

//  @Bean(name = "denormalizeJob")
//  public Job denormalizeJob(Step step1) {
//    return jobBuilderFactory.get("denormalizeJob").incrementer(new RunIdIncrementer()).flow(step1).end().build();
//  }
//
//  @Bean
//  public Step step1() {
//    return stepBuilderFactory.get("step1").tasklet(new DenormalizeProcessor()).build();
//  }
}
