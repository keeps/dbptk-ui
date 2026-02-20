package com.databasepreservation.common.server.v2batch.steps.partition;

import com.databasepreservation.common.server.v2batch.job.JobContext;
import org.springframework.batch.item.ExecutionContext;

import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@FunctionalInterface
public interface PartitionStrategy {
  Map<String, ExecutionContext> mapPartitions(JobContext context);

  default long calculateWorkload(JobContext context, ExecutionContext stepContext) {
    return 0;
  }
}
