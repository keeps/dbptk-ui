package com.databasepreservation.common.server.batch.core;

import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;

/**
 * Extension of StepDefinition for steps that support partitioning. It defines
 * methods to retrieve the partitioning strategy and handle partition completion
 * callbacks.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface PartitionableStep {
  /**
   * Defines the partitioning strategy for this step, which determines how the
   * workload is divided into partitions for parallel execution.
   * 
   * @return The strategy defining how to split work into partitions.
   */
  PartitionStrategy getPartitionStrategy();

  /**
   * Callback executed when an individual worker partition begins its execution.
   * This method is ideal for initializing partition-specific resources, such as
   * opening file systems or establishing localized database connections.
   * 
   * @param jobContext
   *          The context of the overall job execution, providing access to
   *          job-level information and configuration.
   * 
   * @param stepContext
   *          The execution context specific to the starting partition, containing
   *          parameters necessary for its execution (e.g., table name,
   *          boundaries).
   * @throws BatchJobException
   *           if the partition initialization process fails.
   */
  default void onPartitionStarted(JobContext jobContext, ExecutionContext stepContext) throws BatchJobException {
  }

  /**
   * Callback executed when an individual worker partition completes.
   * 
   * @param jobContext
   *          The context of the overall job execution, providing access to
   *          job-level information and configuration.
   * 
   * @param stepContext
   *          The execution context specific to the completed partition, allowing
   *          for access to partition-specific data and state.
   * @param status
   *          The final status of the partition execution, indicating success,
   *          failure, or other outcomes.
   * @throws BatchJobException
   *           if partition finalization fails.
   */
  void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status)
    throws BatchJobException;

  /**
   * Calculates the total workload by summing all partition units.
   *
   * @param context
   *          The current job context, which may be needed to determine the
   *          workload for each partition.
   * 
   * @return Total items across all mapped partitions.
   */
  default long calculatePartitionedWorkload(JobContext context) {
    PartitionStrategy strategy = getPartitionStrategy();
    Map<String, ExecutionContext> partitions = strategy.mapPartitions(context);

    return partitions.values().stream().mapToLong(exec -> strategy.calculateWorkload(context, exec)).sum();
  }
}
