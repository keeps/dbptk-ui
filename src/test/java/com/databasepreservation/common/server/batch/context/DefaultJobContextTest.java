package com.databasepreservation.common.server.batch.context;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultJobContextTest {
  @Mock
  private CollectionStatus mockCollectionStatus;
  @Mock
  private ConfigurationManager mockConfigurationManager;

  private MockedStatic<ViewerFactory> mockedViewerFactory;
  private AutoCloseable openMocks;

  @BeforeEach
  public void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
    mockedViewerFactory = Mockito.mockStatic(ViewerFactory.class);
    mockedViewerFactory.when(ViewerFactory::getConfigurationManager).thenReturn(mockConfigurationManager);
  }

  @AfterEach
  public void tearDown() throws Exception {
    mockedViewerFactory.close();
    openMocks.close();
  }

  @Test
  public void testConstructorThrowsBatchJobExceptionWhenDenormalizationConfigFailsToLoad() throws Exception {
    String databaseUUID = "db-uuid-123";
    String brokenEntryId = "corrupt-entry-id";

    Mockito.when(mockCollectionStatus.getDenormalizations()).thenReturn(Collections.singleton(brokenEntryId));

    // Simulate a systemic failure (e.g., file not found, permission denied) when
    // trying to read the configuration
    Mockito
      .when(mockConfigurationManager.getDenormalizeConfigurationFromCollectionStatusEntry(databaseUUID, brokenEntryId))
      .thenThrow(new org.roda.core.data.exceptions.GenericException("File permission denied"));

    // The constructor must intercept the GenericException and convert it to a
    // BatchJobException to safely abort job creation
    BatchJobException exception = Assertions.assertThrows(BatchJobException.class, () -> {
      new DefaultJobContext(databaseUUID, mockCollectionStatus);
    });

    Assertions.assertTrue(exception.getMessage().contains("Failed to load denormalization config"),
      "Must abort context initialization and map the exception if configuration files cannot be loaded");
  }
}
