package com.databasepreservation.common.server.v2batch.common.policy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ErrorPolicy {
  private final int skipLimit;
  private final int retryLimit;
  private final List<Class<? extends Throwable>> skippableExceptions = new ArrayList<>();
  private final List<Class<? extends Throwable>> retryableExceptions = new ArrayList<>();

  public ErrorPolicy(int skipLimit, int retryLimit) {
    this.skipLimit = skipLimit;
    this.retryLimit = retryLimit;
    this.skippableExceptions.add(Exception.class);
  }

  public List<Class<? extends Throwable>> getSkippableExceptions() {
    return skippableExceptions;
  }

  public List<Class<? extends Throwable>> getRetryableExceptions() {
    return retryableExceptions;
  }

  public int getSkipLimit() {
    return skipLimit;
  }

  public int getRetryLimit() {
    return retryLimit;
  }
}
