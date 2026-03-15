package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * A dedicated Tasklet Step strictly responsible for destructive I/O operations.
 * <p>
 * This step isolates the deletion of orphaned nested documents from the Solr
 * index and the removal of physical configuration files from the filesystem. By
 * extracting these risky I/O operations into a dedicated Tasklet, we ensure
 * that the main data processing steps remain pure and that failures during
 * cleanup can be safely retried or bypassed by the Spring Batch framework
 * without data corruption.
 * </p>
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DenormalizationCleanupStep implements TaskletStepDefinition {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizationCleanupStep.class);
  private final DatabaseRowsSolrManager solrManager;

  public DenormalizationCleanupStep(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Override
  public String getDisplayName() {
    return "Denormalization Cleanup (I/O)";
  }

  /**
   * Evaluates whether this destructive cleanup step needs to be executed.
   * <p>
   * Returns true ONLY if there are denormalization configurations explicitly
   * marked for removal by the user ({@link ProcessingState#TO_REMOVE}).
   * </p>
   */
  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return context -> {
      Set<String> entries = context.getCollectionStatus().getDenormalizations();
      if (entries == null || entries.isEmpty()) {
        return false;
      }

      return entries.stream().map(context::getDenormalizeConfig)
        .anyMatch(config -> config != null && config.isMarkedForRemoval());
    };
  }

  /**
   * Enforces a fail-fast policy for I/O deletions.
   */
  @Override
  public ErrorPolicy getErrorPolicy() {
    return new ErrorPolicy(0, 0);
  }

  @Override
  public Tasklet createTasklet(JobContext jobContext, ExecutionContext executionContext) {
    return (contribution, chunkContext) -> {
      // Create an immutable copy of the entries to prevent
      // ConcurrentModificationException
      // when removing items from the original set during iteration.
      Set<String> entries = Set.copyOf(jobContext.getCollectionStatus().getDenormalizations());

      for (String entryID : entries) {
        DenormalizeConfiguration config = jobContext.getDenormalizeConfig(entryID);

        if (config != null && config.getProcessingState() == ProcessingState.TO_REMOVE) {
          LOGGER.info("Removing denormalization configuration and orphaned nested rows for: {}", entryID);
          removeDenormalizeConfiguration(jobContext, entryID, config);
        }
      }

      return org.springframework.batch.repeat.RepeatStatus.FINISHED;
    };
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) {
    // Intentionally empty. All logic is encapsulated within the Tasklet execution.
  }

  /**
   * Completely purges a denormalization configuration from the system.
   */
  private void removeDenormalizeConfiguration(JobContext jobContext, String entryID, DenormalizeConfiguration config)
    throws BatchJobException {

    // 1. Delete all associated nested documents from the Solr index
    deleteNestedRowsForConfig(jobContext.getDatabaseUUID(), config);

    // 2. Delete the physical configuration JSON file from the filesystem
    try {
      ViewerFactory.getConfigurationManager().deleteDenormalizationFromCollection(jobContext.getDatabaseUUID(),
        entryID);
    } catch (GenericException e) {
      throw new BatchJobException("Failed to delete physical configuration file for: " + entryID, e);
    }

    // 3. Remove the entry from the global in-memory context
    jobContext.getCollectionStatus().getDenormalizations().remove(entryID);

    // 4. Scrub the dynamically added nested columns from the parent table's schema
    // in memory
    TableStatus targetTable = jobContext.getCollectionStatus().getTableStatus(config.getTableUUID());
    if (targetTable != null) {
      DenormalizationStepUtils.removeDenormalizationColumns(targetTable);
    }
  }

  /**
   * Issues bulk delete queries to Solr to purge corresponding nested documents.
   */
  private void deleteNestedRowsForConfig(String databaseUUID, DenormalizeConfiguration config)
    throws BatchJobException {
    try {
      List<RelatedTablesConfiguration> allRelated = new ArrayList<>();
      collectAllRelatedTables(config.getRelatedTables(), allRelated);

      for (RelatedTablesConfiguration related : allRelated) {
        Filter filter = new Filter(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_NESTED_UUID, related.getUuid()));
        solrManager.deleteRowsByQuery(databaseUUID, filter);
      }
    } catch (ViewerException e) {
      throw new BatchJobException(
        "Failed to issue bulk delete queries to Solr for denormalization config: " + config.getId(), e);
    }
  }

  private void collectAllRelatedTables(List<RelatedTablesConfiguration> relatedTables,
    List<RelatedTablesConfiguration> collector) {
    if (relatedTables == null)
      return;
    for (RelatedTablesConfiguration rt : relatedTables) {
      collector.add(rt);
      collectAllRelatedTables(rt.getRelatedTables(), collector);
    }
  }
}
