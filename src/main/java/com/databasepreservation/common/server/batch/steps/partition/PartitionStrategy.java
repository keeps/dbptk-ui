package com.databasepreservation.common.server.batch.steps.partition;

import java.util.Map;

import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface PartitionStrategy {
  Map<String, ExecutionContext> mapPartitions(JobContext context);

  long calculateWorkload(JobContext context, ExecutionContext stepContext);
}
