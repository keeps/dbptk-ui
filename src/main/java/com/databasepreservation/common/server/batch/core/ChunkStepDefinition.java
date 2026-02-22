package com.databasepreservation.common.server.batch.core;

import com.databasepreservation.common.server.batch.context.JobContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ChunkStepDefinition <I extends Serializable, O extends Serializable> extends StepDefinition {
  ItemReader<I> createReader(JobContext context, ExecutionContext executionContext);
  ItemProcessor<I, O> createProcessor(JobContext context, ExecutionContext executionContext);
  ItemWriter<O> createWriter(JobContext context);
}
