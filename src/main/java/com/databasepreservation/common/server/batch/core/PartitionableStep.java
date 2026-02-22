package com.databasepreservation.common.server.batch.core;

import java.util.Map;

import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface PartitionableStep {
  PartitionStrategy getPartitionStrategy();

  void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status)
    throws BatchJobException;

  default long calculatePartitionedWorkload(JobContext context) {
    PartitionStrategy strategy = getPartitionStrategy();
    Map<String, ExecutionContext> partitions = strategy.mapPartitions(context);

    return partitions.values().stream().mapToLong(exec -> strategy.calculateWorkload(context, exec)).sum();
  }
}
