package com.databasepreservation.common.server.v2batch.steps.virtualColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.v2batch.common.policy.ErrorPolicy;
import com.databasepreservation.common.server.v2batch.common.policy.ExecutionPolicy;
import com.databasepreservation.common.server.v2batch.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.v2batch.common.writers.SolrItemWriter;
import com.databasepreservation.common.server.v2batch.job.JobContext;
import com.databasepreservation.common.server.v2batch.steps.StepDefinition;
import com.databasepreservation.common.server.v2batch.steps.StepExitPolicy;
import com.databasepreservation.common.server.v2batch.steps.partition.PartitionStrategy;
import com.databasepreservation.common.server.v2batch.steps.partition.TablePartitionStrategy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualColumnStep implements StepDefinition<ViewerRow, ViewerRow> {

  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualColumnStep.class);

  @Autowired
  private DatabaseRowsSolrManager solrManager;

  @Override
  public String getName() {
    return "virtualColumnStep";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new VirtualColumnStepExecutionPolicy();
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new TablePartitionStrategy(solrManager, this::hasVirtualColumnsToProcess);
  }

  @Override
  public ItemReader<ViewerRow> getReader(JobContext jobContext, ExecutionContext stepContext) {
    String tableId = stepContext.getString("tableId");
    TableStatus tableStatus = findTable(jobContext, tableId);

    if (tableStatus == null) {
      LOGGER.warn("Skip partition: Table configuration not found for ID {}", tableId);
      return null;
    }

    List<String> fieldsToReturn = collectRequiredFields(tableStatus);
    Filter filter = FilterUtils.filterByTable(new Filter(), tableId);

    return new SolrCursorItemReader(solrManager, jobContext.getDatabaseUUID(), filter, fieldsToReturn);
  }

  @Override
  public ItemProcessor<ViewerRow, ViewerRow> getProcessor(JobContext jobContext, ExecutionContext stepContext) {
    return new VirtualColumnStepProcessor(jobContext, stepContext.getString("tableId"));
  }

  @Override
  public ItemWriter<ViewerRow> getWriter(JobContext context) {
    return new SolrItemWriter(solrManager, context.getDatabaseUUID());
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    ErrorPolicy errorPolicy = new ErrorPolicy(0, 2);
    errorPolicy.getRetryableExceptions().add(SolrServerException.class);
    errorPolicy.getRetryableExceptions().add(IOException.class);
    return errorPolicy;
  }

  @Override
  public StepExitPolicy getExitPolicy() {
    return StepExitPolicy.FAIL_JOB_ON_FAILURE;
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status) {
    if (status != BatchStatus.COMPLETED)
      return;

    String tableId = stepContext.getString("tableId");
    TableStatus table = findTable(jobContext, tableId);

    if (table != null) {
      updateProcessedColumnsState(table, status);
    }
  }

  @Override
  public void onStepCompleted(JobContext jobContext, BatchStatus status) {
    if (status != BatchStatus.COMPLETED)
      return;

    for (TableStatus table : jobContext.getCollectionStatus().getTables()) {
      removeMarkedVirtualColumns(table);
    }
  }

  private boolean hasVirtualColumnsToProcess(TableStatus table) {
    return table.getColumns().stream().anyMatch(c -> isVirtual(c) && shouldProcess(c));
  }

  private List<String> collectRequiredFields(TableStatus tableStatus) {
    Set<String> fields = new HashSet<>();
    fields.add(ViewerConstants.INDEX_ID);
    fields.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    if (tableStatus.getColumns() != null) {
      tableStatus.getColumns().stream().filter(this::isVirtual).filter(this::shouldProcess)
        .map(c -> c.getVirtualColumnStatus().getSourceColumnsIds()).filter(sources -> sources != null)
        .forEach(fields::addAll);
    }
    return new ArrayList<>(fields);
  }

  private void updateProcessedColumnsState(TableStatus table, BatchStatus status) {
    Date now = new Date();
    table.getColumns().stream().filter(this::isVirtual).filter(this::shouldProcess).filter(c -> !isMarkedForRemoval(c))
      .forEach(c -> {
        VirtualColumnStatus vcs = c.getVirtualColumnStatus();
        vcs.setProcessingState(ProcessingState.PROCESSED);
        vcs.setLastExecutionDate(now);
        vcs.setExecutionStatus(status.name());
      });
  }

  private void removeMarkedVirtualColumns(TableStatus table) {
    List<ColumnStatus> activeColumns = table.getColumns().stream().filter(c -> !(isVirtual(c) && isMarkedForRemoval(c)))
      .collect(Collectors.toList());
    table.setColumns(activeColumns);
  }

  private TableStatus findTable(JobContext context, String tableId) {
    return context.getCollectionStatus().getTables().stream().filter(t -> t.getId().equals(tableId)).findFirst()
      .orElse(null);
  }

  private boolean isVirtual(ColumnStatus c) {
    return ViewerType.dbTypes.VIRTUAL.equals(c.getType());
  }

  private boolean shouldProcess(ColumnStatus c) {
    return c.getVirtualColumnStatus() != null && c.getVirtualColumnStatus().shouldProcess();
  }

  private boolean isMarkedForRemoval(ColumnStatus c) {
    return c.getVirtualColumnStatus() != null
      && ProcessingState.TO_REMOVE.equals(c.getVirtualColumnStatus().getProcessingState());
  }
}
