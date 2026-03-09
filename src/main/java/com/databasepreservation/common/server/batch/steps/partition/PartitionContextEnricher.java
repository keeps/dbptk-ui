package com.databasepreservation.common.server.batch.steps.partition;

import org.springframework.batch.item.ExecutionContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@FunctionalInterface
public interface PartitionContextEnricher<T> {
  void enrich(ExecutionContext partitionContext, T partitionItem);
}
