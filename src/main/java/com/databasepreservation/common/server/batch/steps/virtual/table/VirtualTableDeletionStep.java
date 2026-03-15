package com.databasepreservation.common.server.batch.steps.virtual.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualTableDeletionStep implements TaskletStepDefinition {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualTableDeletionStep.class);
  private final DatabaseRowsSolrManager solrManager;

  public VirtualTableDeletionStep(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Override
  public String getDisplayName() {
    return "Virtual Table Deletion";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return context -> {
      if (context.getCollectionStatus().getTables() == null)
        return false;

      return context.getCollectionStatus().getTables().stream()
        .anyMatch(t -> t.getVirtualTableStatus() != null && t.getVirtualTableStatus().isMarkedForRemoval());
    };
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    return new ErrorPolicy(0, 0); // No retries for bulk deletes
  }

  @Override
  public Tasklet createTasklet(JobContext context, ExecutionContext executionContext) {
    return (contribution, chunkContext) -> {

      for (TableStatus tableStatus : context.getCollectionStatus().getTables()) {
        if (tableStatus.getVirtualTableStatus() != null && tableStatus.getVirtualTableStatus().isMarkedForRemoval()) {

          LOGGER.info("Executing bulk deletion for virtual table: {} (UUID: {})", tableStatus.getId(),
            tableStatus.getUuid());

          Filter filter = FilterUtils.filterByTableUUID(new Filter(), tableStatus.getUuid());
          solrManager.deleteRowsByQuery(context.getDatabaseUUID(), filter);

          LOGGER.info("Bulk deletion completed for virtual table: {}", tableStatus.getId());
        }
      }

      return org.springframework.batch.repeat.RepeatStatus.FINISHED;
    };
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException {
    // Left empty on purpose. We will persist everything globally in PR 4.
  }
}
