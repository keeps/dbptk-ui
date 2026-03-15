//package com.databasepreservation.common.server.batch.components;
//
//import java.util.Collections;
//import java.util.List;
//
//import org.apache.solr.common.params.CursorMarkParams;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.batch.item.ExecutionContext;
//
//import com.databasepreservation.common.client.index.IndexResult;
//import com.databasepreservation.common.client.index.filter.Filter;
//import com.databasepreservation.common.client.models.structure.ViewerRow;
//import com.databasepreservation.common.server.batch.components.readers.SolrItemReader;
//import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
//import com.databasepreservation.common.server.index.utils.Pair;
//
///**
// * @author Gabriel Barros <gbarros@keep.pt>
// */
//public class SolrItemReaderTest {
//  private DatabaseRowsSolrManager solrManagerMock;
//  private SolrItemReader<ViewerRow> reader;
//  private final String databaseUUID = "test-db-uuid";
//  private final Filter filter = new Filter();
//  private final List<String> fields = Collections.singletonList("id");
//
//  @BeforeEach
//  public void setUp() {
//    solrManagerMock = Mockito.mock(DatabaseRowsSolrManager.class);
//    reader = new SolrItemReader<>(solrManagerMock, databaseUUID, filter, fields, ViewerRow.class);
//  }
//
//  @Test
//  public void testPaginationExhaustionReturnsNullToSignalChunkCompletion() throws Exception {
//    // Simulate the final page where Solr returns an empty list, keeping the same
//    // cursor mark
//    IndexResult<ViewerRow> emptyResult = new IndexResult<>(0L, 0L, 0L, Collections.emptyList(), Collections.emptyList(),
//      Collections.emptyMap());
//    Pair<IndexResult<ViewerRow>, String> emptyPagePair = Pair.of(emptyResult, CursorMarkParams.CURSOR_MARK_START);
//
//    Mockito.when(solrManagerMock.findRows(Mockito.eq(databaseUUID), Mockito.eq(filter), Mockito.any(), Mockito.anyInt(),
//      Mockito.eq(CursorMarkParams.CURSOR_MARK_START), Mockito.eq(fields), Mockito.any())).thenReturn(emptyPagePair);
//
//    reader.open(new ExecutionContext());
//    ViewerRow fetched = reader.read();
//
//    Assertions.assertNull(fetched,
//      "Reader must return null when pagination is exhausted to signal the end of the Step to Spring Batch");
//  }
//
//  @Test
//  public void testRestartabilityStateIsCorrectlyRestoredAndSaved() throws Exception {
//    String savedCursor = "saved-cursor-999";
//    String nextCursor = "next-cursor-1000";
//
//    ExecutionContext executionContext = new ExecutionContext();
//    executionContext.putString("current.cursor.mark", savedCursor);
//
//    // Provide the saved execution context (Simulating a job restart)
//    reader.open(executionContext);
//
//    IndexResult<ViewerRow> result = new IndexResult<>(0L, 1L, 1L, List.of(new ViewerRow()), Collections.emptyList(),
//      Collections.emptyMap());
//    Pair<IndexResult<ViewerRow>, String> pagePair = Pair.of(result, nextCursor);
//
//    // Verify that the query uses the restored cursor mark, NOT the initial one
//    Mockito.when(solrManagerMock.findRows(Mockito.eq(databaseUUID), Mockito.eq(filter), Mockito.any(), Mockito.anyInt(),
//      Mockito.eq(savedCursor), Mockito.eq(fields), Mockito.any())).thenReturn(pagePair);
//
//    // Perform a read to advance the internal cursor state
//    reader.read();
//
//    // Simulate Spring Batch taking a checkpoint
//    ExecutionContext contextToSave = new ExecutionContext();
//    reader.update(contextToSave);
//
//    Assertions.assertEquals(nextCursor, contextToSave.getString("current.cursor.mark"),
//      "The newly fetched cursor mark must be saved in the execution context to allow accurate job restarts");
//  }
//
//  @Test
//  public void testReadFetchesDataAndPaginatesCorrectly() throws Exception {
//    ViewerRow row1 = new ViewerRow();
//    ViewerRow row2 = new ViewerRow();
//    IndexResult<ViewerRow> resultPage1 = new IndexResult<>(0L, 2L, 2L, List.of(row1, row2), Collections.emptyList(),
//      Collections.emptyMap());
//    Pair<IndexResult<ViewerRow>, String> page1Pair = Pair.of(resultPage1, "nextCursorMark123");
//
//    // Mocks the first page request
//    Mockito.when(solrManagerMock.findRows(Mockito.eq(databaseUUID), Mockito.eq(filter), Mockito.any(), Mockito.eq(100),
//      Mockito.eq(CursorMarkParams.CURSOR_MARK_START), Mockito.eq(fields), Mockito.any())).thenReturn(page1Pair);
//
//    // Initializes the reader state
//    reader.open(new ExecutionContext());
//
//    // First item
//    ViewerRow fetched1 = reader.read();
//    Assertions.assertEquals(row1, fetched1, "Should return the first item from the batch");
//
//    // Second item
//    ViewerRow fetched2 = reader.read();
//    Assertions.assertEquals(row2, fetched2, "Should return the second item from the batch");
//
//    // Third read should trigger fetching the next page (which we will mock as empty
//    // to finish)
//    IndexResult<ViewerRow> emptyResult = new IndexResult<>(0L, 0L, 0L, Collections.emptyList(), Collections.emptyList(),
//      Collections.emptyMap());
//    Pair<IndexResult<ViewerRow>, String> emptyPagePair = Pair.of(emptyResult, "nextCursorMark123");
//
//    Mockito.when(solrManagerMock.findRows(Mockito.eq(databaseUUID), Mockito.eq(filter), Mockito.any(), Mockito.eq(100),
//      Mockito.eq("nextCursorMark123"), Mockito.eq(fields), Mockito.any())).thenReturn(emptyPagePair);
//
//    ViewerRow fetched3 = reader.read();
//    Assertions.assertNull(fetched3, "Should return null when all results are exhausted");
//  }
//}
