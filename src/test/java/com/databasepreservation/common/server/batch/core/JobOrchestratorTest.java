package com.databasepreservation.common.server.batch.core;

import java.util.Collections;

import com.databasepreservation.common.server.controller.JobController;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.task.TaskRejectedException;

import com.databasepreservation.common.server.batch.context.ContextResolver;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.context.JobContextRegistry;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobOrchestratorTest {
  @Mock
  private JobRepository jobRepository;
  @Mock
  private JobExplorer jobExplorer;
  @Mock
  private StepFactory stepFactory;
  @Mock
  private ContextResolver contextResolver;
  @Mock
  private JobLauncher jobLauncher;
  @Mock
  private JobDefinition jobDefinition;

  private JobContextRegistry contextRegistry;
  private JobOrchestrator orchestrator;

  // Required to prevent the real JobController from booting Solr/Spring contexts
  // during tests
  private MockedStatic<JobController> mockedJobController;
  private MockedStatic<SolrUtils> mockedSolrUtils;
  private AutoCloseable openMocks;

  @BeforeEach
  public void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
    contextRegistry = new JobContextRegistry();
    orchestrator = new JobOrchestrator(jobRepository, jobExplorer, stepFactory, contextResolver, contextRegistry,
      jobLauncher);

    mockedJobController = Mockito.mockStatic(JobController.class);
    mockedSolrUtils = Mockito.mockStatic(SolrUtils.class);

    Mockito.when(jobDefinition.getName()).thenReturn("testJob");
    Mockito.when(jobDefinition.getDisplayName()).thenReturn("Test Job");
  }

  @AfterEach
  public void tearDown() throws Exception {
    if (mockedJobController != null) {
      mockedJobController.close();
    }
    if (mockedSolrUtils != null) {
      mockedSolrUtils.close();
    }
    if (openMocks != null) {
      openMocks.close();
    }
  }

  @Test
  public void testFastFailWhenDatabaseIsAlreadyLockedInMemory() {
    String databaseUUID = "db-locked-in-memory";

    // Lock the database in the registry
    contextRegistry.register(databaseUUID, Mockito.mock(JobContext.class));

    BatchJobException exception = Assertions.assertThrows(BatchJobException.class, () -> {
      orchestrator.launchJob(databaseUUID, "col-123", jobDefinition);
    });

    Assertions.assertTrue(exception.getMessage().contains("actively modifying this database"),
      "Must fast-fail to prevent data corruption when another thread is already orchestrating this database");
  }

  @Test
  public void testFailsWhenSpringBatchReportsJobAlreadyRunning() {
    String databaseUUID = "db-running";
    String expectedJobName = "testJob-" + databaseUUID;

    // Simulate an active execution in the Spring Batch repository
    Mockito.when(jobExplorer.findRunningJobExecutions(expectedJobName))
      .thenReturn(Collections.singleton(new JobExecution(1L)));

    BatchJobException exception = Assertions.assertThrows(BatchJobException.class, () -> {
      orchestrator.launchJob(databaseUUID, "col-123", jobDefinition);
    });

    Assertions.assertTrue(exception.getMessage().contains("is already running on this database"),
      "Must prevent concurrent execution of the same job type for the same database");
  }

  @Test
  public void testHandlesThreadQueueExhaustionGracefully() throws Exception {
    String databaseUUID = "db-free";

    JobContext mockContext = Mockito.mock(JobContext.class);
    Mockito.when(mockContext.getJobProgressAggregator()).thenReturn(new JobProgressAggregator());
    Mockito.when(contextResolver.resolve(databaseUUID)).thenReturn(mockContext);

    Mockito.when(jobExplorer.findRunningJobExecutions(Mockito.anyString())).thenReturn(Collections.emptySet());
    mockedSolrUtils.when(SolrUtils::randomUUID).thenReturn("mocked-uuid");

    // Simulate a scenario where the TaskExecutor rejects new jobs due to pool
    // exhaustion
    Mockito.when(jobLauncher.run(Mockito.any(), Mockito.any(JobParameters.class)))
      .thenThrow(new TaskRejectedException("Executor queue is full"));

    BatchJobException exception = Assertions.assertThrows(BatchJobException.class, () -> {
      orchestrator.launchJob(databaseUUID, "col-123", jobDefinition);
    });

    Assertions.assertTrue(exception.getMessage().contains("Queue is full"),
      "Must capture TaskRejectedException and translate it into a friendly BatchJobException indicating the queue is full");
  }
}
