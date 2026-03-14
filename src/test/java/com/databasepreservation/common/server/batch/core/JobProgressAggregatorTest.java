package com.databasepreservation.common.server.batch.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobProgressAggregatorTest {
  private JobProgressAggregator aggregator;

  @BeforeEach
  public void setUp() {
    aggregator = new JobProgressAggregator();
  }

  @Test
  public void testAddProgressCalculatesDeltaCorrectly() {
    Long executionId = 101L;

    long processed = aggregator.addProgress(executionId, 10L);
    Assertions.assertEquals(10L, processed, "Initial progress should match the reported count");

    processed = aggregator.addProgress(executionId, 25L);
    Assertions.assertEquals(25L, processed,
      "Aggregator must extract the delta (15) and sum it to the global processed items");
  }

  @Test
  public void testAddProgressIgnoresStaleUpdatesFromDelayedThreads() {
    Long executionId = 101L;

    aggregator.addProgress(executionId, 100L);

    // Simulate a delayed thread or out-of-order execution reporting a smaller total
    long processed = aggregator.addProgress(executionId, 90L);

    Assertions.assertEquals(100L, processed,
      "Aggregator MUST ignore older, smaller progress reports to prevent progress bar regression");
  }
}
