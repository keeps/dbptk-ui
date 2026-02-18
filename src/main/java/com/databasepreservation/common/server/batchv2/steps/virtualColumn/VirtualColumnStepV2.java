package com.databasepreservation.common.server.batchv2.steps.virtualColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.index.sublist.Sublist;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.steps.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.batch.steps.common.writers.SolrItemWriter;
import com.databasepreservation.common.server.batchv2.common.ErrorPolicy;
import com.databasepreservation.common.server.batchv2.common.ExecutionPolicy;
import com.databasepreservation.common.server.batchv2.common.PartitionStrategy;
import com.databasepreservation.common.server.batchv2.common.StepDefinition;
import com.databasepreservation.common.server.batchv2.common.StepExitPolicy;
import com.databasepreservation.common.server.batchv2.common.TaskContext;
import com.databasepreservation.common.server.batchv2.common.WorkloadEstimator;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualColumnStepV2 implements StepDefinition<ViewerRow, ViewerRow> {

  @Autowired
  private DatabaseRowsSolrManager solrManager;

  @Autowired
  private ItemReader<ViewerRow> virtualColumnReaderV2;

  @Autowired
  private ItemProcessor<ViewerRow, ViewerRow> virtualColumnProcessorV2;

  @Override
  public String getName() {
    return "virtualColumnStep";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return context -> {
      CollectionStatus status = context.getCollectionStatus();
      if (status.getTables() == null)
        return false;

      return status.getTables().stream().flatMap(table -> table.getColumns().stream())
        .anyMatch(column -> ViewerType.dbTypes.VIRTUAL.equals(column.getType())
          && column.getVirtualColumnStatus() != null && column.getVirtualColumnStatus().shouldProcess());
    };
  }

  @Override
  public WorkloadEstimator getWorkloadEstimator() {
    return (context, tableId) -> {
      try {
        Filter filter = FilterUtils.filterByTable(new Filter(), tableId);
        IndexResult<ViewerRow> result = solrManager.findRows(context.getDatabaseUUID(), filter, null, new Sublist(0, 0),
          null);
        return result.getTotalCount();
      } catch (Exception e) {
        return 0L;
      }
    };
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return context -> {
      Map<String, ExecutionContext> partitions = new HashMap<>();
      CollectionStatus status = context.getCollectionStatus();

      if (status.getTables() != null) {
        for (TableStatus table : status.getTables()) {
          boolean hasVirtual = table.getColumns().stream().anyMatch(c -> ViewerType.dbTypes.VIRTUAL.equals(c.getType())
            && c.getVirtualColumnStatus() != null && c.getVirtualColumnStatus().shouldProcess());

          if (hasVirtual) {
            ExecutionContext v = new ExecutionContext();
            v.putString("tableId", table.getId());
            partitions.put("partition-" + table.getId(), v);
          }
        }
      }
      return partitions;
    };
  }

  @Override
  public ItemReader<ViewerRow> getReader(TaskContext context) {
    return virtualColumnReaderV2;
  }

  @Bean
  @StepScope
  public SolrCursorItemReader virtualColumnReaderV2(@Value("#{stepExecutionContext['tableId']}") String tableId,
    @Value("#{jobParameters['databaseUUID']}") String databaseUUID) {

    Filter filter = FilterUtils.filterByTable(new Filter(), tableId);
    return new SolrCursorItemReader(solrManager, databaseUUID, filter, new ArrayList<>());
  }

  @Override
  public ItemProcessor<ViewerRow, ViewerRow> getProcessor(TaskContext context) {
    return virtualColumnProcessorV2(context);
  }

  @Bean
  @StepScope
  public ItemProcessor<ViewerRow, ViewerRow> virtualColumnProcessorV2(TaskContext context) {
    return new VirtualColumnStepProcessorV2(context);
  }

  @Override
  public ItemWriter<ViewerRow> getWriter(TaskContext context) {
    return new SolrItemWriter(solrManager, context.getDatabaseUUID());
  }

  @Override
  public StepExitPolicy getExitPolicy() {
    return StepExitPolicy.FAIL_JOB_ON_FAILURE;
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    return new ErrorPolicy(10, 2);
  }
}
