package com.databasepreservation.common.server.batch.components;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.Chunk;

import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.components.writers.SolrItemWriter;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrItemWriterTest {
  private DatabaseRowsSolrManager solrManagerMock;
  private SolrItemWriter<ViewerRow> writer;
  private final String databaseUUID = "test-db-uuid";

  @BeforeEach
  public void setUp() {
    solrManagerMock = Mockito.mock(DatabaseRowsSolrManager.class);
    writer = new SolrItemWriter<>(solrManagerMock, databaseUUID, DatabaseRowsSolrManager.WriteMode.UPDATE);
  }

  @Test
  public void testWriteGracefullyHandlesNullOrEmptyChunk() throws Exception {
    writer.write(null);
    writer.write(new Chunk<>(Collections.emptyList()));

    // Must return silently without throwing NullPointerException to prevent job
    // crashes on empty reads
    Mockito.verify(solrManagerMock, Mockito.never()).insertBatchDocuments(Mockito.anyString(), Mockito.anyList(),
      Mockito.any());
  }

  @Test
  public void testWritePropagatesExceptionsForSpringBatchFaultTolerance() throws Exception {
    ViewerRow row = new ViewerRow();
    Chunk<ViewerRow> chunk = new Chunk<>(List.of(row));

    // Using a RuntimeException because insertBatchDocuments does not explicitly
    // throw SolrServerException.
    // The core value of this test is ensuring the wrapper does not silently catch
    // and suppress failures.
    Mockito.doThrow(new RuntimeException("Database connection failed!")).when(solrManagerMock)
      .insertBatchDocuments(Mockito.eq(databaseUUID), Mockito.anyList(), Mockito.any());

    // The exception MUST bubble up. If the writer catches and suppresses it,
    // Spring Batch's ErrorPolicy (Retry/Skip) will fail to intercept it and falsely
    // mark the chunk as complete.
    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
      writer.write(chunk);
    });

    Assertions.assertEquals("Database connection failed!", exception.getMessage(),
      "Must propagate the exact underlying exception to trigger framework-level retry policies");
  }
}
