package com.databasepreservation.common.server.batchv2.common;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@FunctionalInterface
public interface WorkloadEstimator {
  long estimate(TaskContext context, String tableId);
}
