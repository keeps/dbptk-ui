package com.databasepreservation.common.server.batch.core;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface StepDefinition<I, O> {
  String getName();

  ExecutionPolicy getExecutionPolicy();

  PartitionStrategy getPartitionStrategy();

  ItemReader<I> createReader(JobContext context, ExecutionContext partitionContext);

  ItemProcessor<I, O> createProcessor(JobContext context, ExecutionContext partitionContext);

  ItemWriter<O> createWriter(JobContext context);

  ErrorPolicy getErrorPolicy();

  void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status)
    throws BatchJobException;;

  void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException;
}
