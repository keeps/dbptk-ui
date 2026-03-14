package com.databasepreservation.common.server.batch.listeners;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.context.JobContextRegistry;
import com.databasepreservation.common.server.controller.JobController;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobStatusListenerTest {
  @Mock
  private JobContext jobContextMock;
  @Mock
  private JobContextRegistry registryMock;
  @Mock
  private JobRepository jobRepositoryMock;
  @Mock
  private ConfigurationManager configurationManagerMock;
  @Mock
  private CollectionStatus collectionStatusMock;

  private JobStatusListener listener;
  private MockedStatic<JobController> mockedJobController;
  private MockedStatic<ViewerFactory> mockedViewerFactory;
  private AutoCloseable openMocks;
  private final String databaseUUID = "test-db-uuid";

  @BeforeEach
  public void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);

    // Injects the JobRepository to ensure we can test the framework state override
    listener = new JobStatusListener(jobContextMock, registryMock, jobRepositoryMock);

    Mockito.when(jobContextMock.getDatabaseUUID()).thenReturn(databaseUUID);
    Mockito.when(jobContextMock.getCollectionStatus()).thenReturn(collectionStatusMock);

    mockedJobController = Mockito.mockStatic(JobController.class);
    mockedViewerFactory = Mockito.mockStatic(ViewerFactory.class);

    mockedViewerFactory.when(ViewerFactory::getConfigurationManager).thenReturn(configurationManagerMock);
  }

  @AfterEach
  public void tearDown() throws Exception {
    if (mockedJobController != null)
      mockedJobController.close();
    if (mockedViewerFactory != null)
      mockedViewerFactory.close();
    if (openMocks != null)
      openMocks.close();
  }

  @Test
  public void testAfterJobOverridesStatusToFailedWhenPersistenceFails() throws Exception {
    JobExecution execution = new JobExecution(1L);
    execution.setStatus(BatchStatus.COMPLETED); // Job finished internal steps successfully

    // Configure the mocked CollectionStatus directly to prevent deep-stubbing
    // confusion
    String entryID = "denorm-123";
    Mockito.when(collectionStatusMock.getDenormalizations()).thenReturn(Collections.singleton(entryID));

    DenormalizeConfiguration config = new DenormalizeConfiguration();
    config.setState(ViewerJobStatus.COMPLETED);
    Mockito.when(jobContextMock.getDenormalizeConfig(entryID)).thenReturn(config);

    // Simulate an IO or Generic error precisely where it is allowed by the method
    // signature
    Mockito.doThrow(new org.roda.core.data.exceptions.GenericException("Disk is full or readonly"))
      .when(configurationManagerMock).updateDenormalizationConfigurationFile(Mockito.eq(databaseUUID), Mockito.any());

    listener.afterJob(execution);

    Assertions.assertEquals(BatchStatus.FAILED, execution.getStatus(),
      "Job execution status MUST be overridden to FAILED if post-processing persistence fails");

    // CRITICAL: Verifies that the JobRepository was explicitly updated to reflect
    // this new FAILED state in the database
    Mockito.verify(jobRepositoryMock, Mockito.times(1)).update(execution);

    // Registry lock must be released regardless of failure
    Mockito.verify(registryMock, Mockito.times(1)).unregister(databaseUUID);
  }
}
