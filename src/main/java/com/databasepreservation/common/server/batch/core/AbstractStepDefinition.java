package com.databasepreservation.common.server.batch.core;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.server.batch.components.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class AbstractStepDefinition<I, O> implements StepDefinition<I, O> {
  @Autowired
  protected DatabaseRowsSolrManager solrManager;

  @Override
  @SuppressWarnings("unchecked")
  public ItemReader<I> createReader(JobContext context, ExecutionContext partitionContext) {
    Filter filter = (Filter) partitionContext.get("filter");
    List<String> fields = (List<String>) partitionContext.get("fields");

    return (ItemReader<I>) new SolrCursorItemReader(solrManager, context.getDatabaseUUID(), filter, fields);
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    ErrorPolicy policy = new ErrorPolicy(0, 2);
    policy.getRetryableExceptions().add(SolrServerException.class);
    policy.getRetryableExceptions().add(IOException.class);
    return policy;
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status)
    throws BatchJobException {
    // No-op
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException {
    // No-op
  }
}
