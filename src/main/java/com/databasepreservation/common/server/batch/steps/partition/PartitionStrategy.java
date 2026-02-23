package com.databasepreservation.common.server.batch.steps.partition;

import java.util.Map;

import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * Strategy interface for partitioning a batch step's workload. Implementations
 * define how to split the work into partitions and calculate the workload for
 * each partition.
 * 
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface PartitionStrategy {

  /**
   * Generates a map of execution contexts, where each entry represents a
   * partition of the workload. The key is a unique identifier for the partition,
   * and the value is an ExecutionContext containing any necessary information for
   * processing that partition. The implementation should determine how to divide
   * the workload based on the provided JobContext, which may include job-level
   * configuration and status needed to make partitioning decisions.
   * 
   * @param context
   *          The job context providing access to job-level configuration and
   *          status, which may be needed to determine how to partition the
   *          workload.
   * @return A map where each key is a unique partition identifier and each value
   *         is an ExecutionContext for that partition.
   */
  Map<String, ExecutionContext> mapPartitions(JobContext context);

  /**
   * Calculates the workload for a given partition by summing the relevant items
   * or units of work defined in the provided ExecutionContext. This method allows
   * the partitioning strategy to determine the size of each partition's workload,
   * which can be used for load balancing and progress tracking.
   * 
   * @param context The job context providing access to job-level configuration and status, which may be needed to determine the workload for the partition.
   * @param partitionContext The execution context specific to the partition, containing information about the partition's workload and any relevant state needed for processing.
   * @return
   */
  long calculateWorkload(JobContext context, ExecutionContext partitionContext);
}
