package com.databasepreservation.common.server.batchv2.service;

import com.databasepreservation.common.server.batchv2.common.StepDefinition;
import com.databasepreservation.common.server.batchv2.registry.WorkflowOrchestrator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
public class JobLauncherService {
  private final JobLauncher jobLauncher;
  private final WorkflowOrchestrator orchestrator;

  public JobLauncherService(JobLauncher jobLauncher, WorkflowOrchestrator orchestrator) {
    this.jobLauncher = jobLauncher;
    this.orchestrator = orchestrator;
  }

  public void runWorkflow(String databaseUUID, List<StepDefinition<?, ?>> steps) {
    try {
      Job job = orchestrator.buildJob(databaseUUID, steps);

      JobParameters params = new JobParametersBuilder()
        .addString("databaseUUID", databaseUUID)
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

      jobLauncher.run(job, params);

    } catch (Exception e) {
      throw new RuntimeException("Failed to launch job for database UUID: " + databaseUUID, e);
    }
  }
}
