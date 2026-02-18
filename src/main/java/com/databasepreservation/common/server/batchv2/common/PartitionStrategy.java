package com.databasepreservation.common.server.batchv2.common;

import org.springframework.batch.item.ExecutionContext;

import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@FunctionalInterface
public interface PartitionStrategy {
  Map<String, ExecutionContext> mapPartitions(TaskContext context);
}
