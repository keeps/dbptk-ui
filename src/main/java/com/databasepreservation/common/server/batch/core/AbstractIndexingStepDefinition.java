package com.databasepreservation.common.server.batch.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.server.batch.components.readers.SolrItemReader;
import com.databasepreservation.common.server.batch.components.writers.SolrItemWriter;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * Base class for chunk-oriented steps that read from and write to the Solr
 * index. It provides standard implementations for SolrItemReader and
 * SolrItemWriter.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class AbstractIndexingStepDefinition<I extends IsIndexed & Serializable, O extends IsIndexed & Serializable>
  implements ChunkStepDefinition<I, O> {

  @Autowired
  protected DatabaseRowsSolrManager solrManager;

  private final Class<I> incomingClass;

  @SuppressWarnings("unchecked")
  protected AbstractIndexingStepDefinition() {
    Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(getClass(), AbstractIndexingStepDefinition.class);
    this.incomingClass = (Class<I>) (generics != null ? generics[0] : Object.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ItemReader<I> createReader(JobContext context, ExecutionContext executionContext) {
    Filter filter = (Filter) executionContext.get(BatchConstants.FILTER_KEY);
    List<String> fields = (List<String>) executionContext.get(BatchConstants.FIELDS_KEY);

    return new SolrItemReader<>(solrManager, context.getDatabaseUUID(), filter, fields, incomingClass);
  }

  @Override
  public ItemWriter<O> createWriter(JobContext context) {
    return new SolrItemWriter<>(solrManager, context.getDatabaseUUID());
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    ErrorPolicy policy = new ErrorPolicy(0, 2);
    policy.getRetryableExceptions().add(SolrServerException.class);
    policy.getRetryableExceptions().add(IOException.class);
    return policy;
  }
}
