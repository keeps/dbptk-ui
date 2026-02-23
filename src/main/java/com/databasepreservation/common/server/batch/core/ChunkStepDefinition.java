package com.databasepreservation.common.server.batch.core;

import java.io.Serializable;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * Defines a chunk-oriented step in a batch job, specifying how to read,
 * process, and write items in chunks. This interface extends StepDefinition to
 * include chunk-specific behavior, such as creating readers, processors, and
 * writers that operate on chunks of data. Implementations of this interface
 * should provide the logic for how to read input items, process them, and write
 * the output items, while also adhering to the execution and error policies
 * defined in the StepDefinition contract.
 * 
 * @param <I>
 *          Input type read from the source.
 * @param <O>
 *          Output type to be written to the destination.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ChunkStepDefinition<I extends Serializable, O extends Serializable> extends StepDefinition {
  /**
   * Creates a reader scoped to the current execution context.
   * 
   * @param context
   *          The job context providing access to job-level configuration and
   *          status.
   * @param executionContext
   *          The execution context for the current step or partition, allowing
   *          for state management and sharing across executions.
   */
  ItemReader<I> createReader(JobContext context, ExecutionContext executionContext);

  /**
   * Creates a processor to transform items from type I to O.
   * 
   * @param context
   *          The job context providing access to job-level configuration and
   *          status.
   * @param executionContext
   *          The execution context for the current step or partition, allowing
   *          for state management and sharing across executions.
   * 
   */
  ItemProcessor<I, O> createProcessor(JobContext context, ExecutionContext executionContext);

  /**
   * Creates a writer to persist processed chunks.
   * 
   * @param context
   *          The job context providing access to job-level configuration and
   */
  ItemWriter<O> createWriter(JobContext context);
}
