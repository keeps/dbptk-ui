package com.databasepreservation.common.server.batch.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobProgressAggregator {
  private final AtomicLong totalItems = new AtomicLong(0);
  private final AtomicLong processedItems = new AtomicLong(0);
  private final AtomicLong skippedItems = new AtomicLong(0);

  private final Map<Long, Long> lastReportedByExecution = new ConcurrentHashMap<>();
  private final Map<Long, Long> lastReportedSkippedByExecution = new ConcurrentHashMap<>(); // NOVO

  public void setTotalItems(long total) {
    this.totalItems.set(total);
  }

  public long addProgress(Long executionId, long currentCount) {
    long lastCount = lastReportedByExecution.getOrDefault(executionId, 0L);
    long delta = currentCount - lastCount;
    if (delta > 0) {
      lastReportedByExecution.put(executionId, currentCount);
      return processedItems.addAndGet(delta);
    }
    return processedItems.get();
  }

  public long addSkips(Long executionId, long currentSkipCount) {
    long lastCount = lastReportedSkippedByExecution.getOrDefault(executionId, 0L);
    long delta = currentSkipCount - lastCount;
    if (delta > 0) {
      lastReportedSkippedByExecution.put(executionId, currentSkipCount);
      return skippedItems.addAndGet(delta);
    }
    return skippedItems.get();
  }

  public long getTotal() {
    return totalItems.get();
  }

  public long getProcessed() {
    return processedItems.get();
  }

  public long getSkipped() {
    return skippedItems.get();
  }
}
