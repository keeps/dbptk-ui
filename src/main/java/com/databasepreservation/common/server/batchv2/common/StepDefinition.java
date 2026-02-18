package com.databasepreservation.common.server.batchv2.common;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface StepDefinition<I, O> {
  String getName();

  default List<Class<?>> getRequiredConfigurationTypes() {
    return List.of(CollectionStatus.class);
  }

  ExecutionPolicy getExecutionPolicy();

  PartitionStrategy getPartitionStrategy();

  WorkloadEstimator getWorkloadEstimator();

  ItemProcessor<I, O> getProcessor(TaskContext context);

  ItemReader<I> getReader(TaskContext context);

  ItemWriter<O> getWriter(TaskContext context);

  ErrorPolicy getErrorPolicy();

  StepExitPolicy getExitPolicy();
}
