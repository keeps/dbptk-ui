package com.databasepreservation.common.server.v2batch.steps;

import java.util.List;

import com.databasepreservation.common.server.v2batch.common.policy.ErrorPolicy;
import com.databasepreservation.common.server.v2batch.common.policy.ExecutionPolicy;
import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.v2batch.steps.partition.PartitionStrategy;
import com.databasepreservation.common.server.v2batch.job.JobContext;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

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

  ItemProcessor<I, O> getProcessor(JobContext context, ExecutionContext stepContext);

  ItemReader<I> getReader(JobContext context, ExecutionContext stepContext);

  ItemWriter<O> getWriter(JobContext context);

  ErrorPolicy getErrorPolicy();

  StepExitPolicy getExitPolicy();

  void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status) throws BatchJobException;;

  void onStepCompleted(JobContext jobContext, BatchStatus status) throws BatchJobException;
}
