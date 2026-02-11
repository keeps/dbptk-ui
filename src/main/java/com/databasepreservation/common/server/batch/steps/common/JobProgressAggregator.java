package com.databasepreservation.common.server.batch.steps.common;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
@JobScope
public class JobProgressAggregator {
  private final AtomicLong totalRows = new AtomicLong(0);
  private final AtomicLong processedRows = new AtomicLong(0);

  public void reset(long total) {
    this.totalRows.set(total);
    this.processedRows.set(0);
  }

  public long addProgress(long delta) {
    return this.processedRows.addAndGet(delta);
  }

  public long getTotal() {
    return totalRows.get();
  }

  public long getProcessed() {
    return processedRows.get();
  }
}
