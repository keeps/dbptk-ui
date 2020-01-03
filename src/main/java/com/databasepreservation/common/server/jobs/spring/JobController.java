package com.databasepreservation.common.server.jobs.spring;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Component
public class JobController {
  public void run() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BatchConfiguration.class);

    JobLauncher jobLauncher = context.getBean(JobLauncher.class);
//    Job denormalizeJob = (Job) context.getBean("denormalizeJob");

    JobBuilderFactory jobBuilderFactory = context.getBean(JobBuilderFactory.class);
    StepBuilderFactory stepBuilderFactory = context.getBean(StepBuilderFactory.class);

    Step step = stepBuilderFactory.get("step").tasklet(new DenormalizeProcessor()).build();
    Job denormalizeJob = jobBuilderFactory.get("denormalizeJob").flow(step).end().build();

    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("databaseUUID", "uuid");
    JobParameters jobParameters = jobBuilder.toJobParameters();

    try {
      jobLauncher.run(denormalizeJob, jobParameters);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("End");
  }
}
