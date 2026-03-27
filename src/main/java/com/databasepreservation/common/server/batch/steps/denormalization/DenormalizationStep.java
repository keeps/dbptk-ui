package com.databasepreservation.common.server.batch.steps.denormalization;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.AbstractIndexingStepDefinition;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.core.PartitionableStep;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;

/**
 * Orchestrates the Data Denormalization step within the data transformation
 * batch job.
 * <p>
 * This step is chunk-oriented and highly scalable due to its implementation of
 * {@link PartitionableStep}. It relies on a specialized Bulk Fetching strategy
 * (Prefetch Reader) to resolve relational data hierarchies efficiently,
 * preventing the N+1 select problem against the Solr index.
 * </p>
 * <p>
 * Note: Heavy I/O cleanup operations and metadata updates are purposefully
 * excluded from this step's lifecycle hooks and are instead delegated to a
 * dedicated cleanup tasklet (e.g., DenormalizationCleanupStep) to adhere to
 * Spring Batch transaction boundaries and single-responsibility principles.
 * </p>
 * * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DenormalizationStep extends AbstractIndexingStepDefinition<ViewerRow, ViewerRow>
  implements PartitionableStep {

  @Override
  public String getDisplayName() {
    return "Data Denormalization";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new DenormalizationStepExecutionPolicy();
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new DenormalizationStepPartitionStrategy(solrManager);
  }

  @Override
  public long calculateWorkload(JobContext context) {
    return calculatePartitionedWorkload(context);
  }

  /**
   * Creates the specialized reader for this partition.
   * <p>
   * Overrides the default base reader by wrapping it inside a
   * {@link DenormalizationPrefetchReader}. This allows the step to fetch
   * relational child documents in bulk (chunk-level) rather than one-by-one,
   * significantly reducing Solr query overhead.
   * </p>
   *
   * @param context
   *          The global job context.
   * @param stepContext
   *          The execution context specific to this partition.
   * @return A reader capable of prefetching and assembling nested document
   *         hierarchies.
   */
  @Override
  @SuppressWarnings("unchecked")
  public ItemReader<ViewerRow> createReader(JobContext context, ExecutionContext stepContext) {
    // 1. Instantiate the default Solr reader provided by the abstract base class
    ItemReader<ViewerRow> baseReader = super.createReader(context, stepContext);

    // 2. Extract the specific denormalization configuration for this partition
    String entryID = stepContext.getString(BatchConstants.DENORMALIZATION_ENTRY_ID_KEY);
    DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

    // 3. Eagerly load the database metadata once per partition initialization
    ViewerDatabase database = context.getViewerDatabase();

    // 4. Wrap the base reader in the PrefetchReader to enable chunk-aware
    // relational fetching
    int chunkSize = getExecutionPolicy().getChunkSize();
    return new DenormalizationPrefetchReader((ItemStreamReader<ViewerRow>) baseReader, chunkSize, solrManager, config,
      context.getDatabaseUUID(), database);
  }

  /**
   * Creates the processor for this partition.
   * <p>
   * Since the heavy lifting of resolving relations and building the hierarchical
   * tree is now handled by the {@link DenormalizationPrefetchReader}, this
   * processor acts merely as a pure filter, discarding rows that yielded no
   * relational matches.
   * </p>
   */
  @Override
  public ItemProcessor<ViewerRow, ViewerRow> createProcessor(JobContext context, ExecutionContext stepContext) {
    return new DenormalizationStepProcessor();
  }

  /**
   * Callback executed when an individual worker partition completes.
   * <p>
   * Updates the in-memory execution state of the denormalization entry.
   * </p>
   */
  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext stepContext, BatchStatus status) {
    if (status == BatchStatus.COMPLETED) {
      String entryID = stepContext.getString(BatchConstants.DENORMALIZATION_ENTRY_ID_KEY);
      DenormalizeConfiguration config = jobContext.getDenormalizeConfig(entryID);

      if (config != null) {
        config.setState(ViewerJobStatus.PENDING_METADATA);
      }
    }
  }

  /**
   * Callback executed when the entire step (all partitions) finishes.
   * <p>
   * Left intentionally empty. Global metadata updates and Solr cleanup operations
   * must be orchestrated via a subsequent Tasklet to ensure proper transaction
   * isolation.
   * </p>
   */
  @Override
  public void onStepCompleted(JobContext jobContext, BatchStatus status) throws BatchJobException {
    // No operation required here. Delegated to DenormalizationCleanupStep.
  }
}
