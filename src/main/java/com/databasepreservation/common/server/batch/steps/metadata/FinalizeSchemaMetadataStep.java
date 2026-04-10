package com.databasepreservation.common.server.batch.steps.metadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.LobTextExtractionStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualForeignKeysStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.denormalization.DenormalizationStepUtils;
import com.databasepreservation.common.server.batch.steps.virtual.VirtualSchemaBuilderUtils;

/**
 * The central and atomic synchronization point for all schema metadata
 * alterations.
 * <p>
 * <b>State Machine Note:</b> This step acts as the Gatekeeper. It looks for
 * entities left in the PENDING_METADATA state by the Worker Steps, updates the
 * Solr Schema, and then closes the loop by marking them as PROCESSED.
 * </p>
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class FinalizeSchemaMetadataStep implements TaskletStepDefinition {
  private static final Logger LOGGER = LoggerFactory.getLogger(FinalizeSchemaMetadataStep.class);
  private final SchemaMetadataService metadataService;

  public FinalizeSchemaMetadataStep(SchemaMetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public String getDisplayName() {
    return "Finalize Schema Metadata Synchronization";
  }

  /**
   * Triggers ONLY if there are entities waiting for metadata finalization
   * (PENDING_METADATA) or marked for removal.
   */
  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return context -> {
      boolean hasVirtuals = false;
      boolean hasLobs = false;

      if (context.getCollectionStatus().getTables() != null) {
        hasVirtuals = context.getCollectionStatus().getTables().stream()
          .anyMatch(table -> (table.getVirtualTableStatus() != null
            && (table.getVirtualTableStatus().isPendingMetadata() || table.getVirtualTableStatus().shouldProcess()))
            || (table.getColumns() != null && table.getColumns().stream()
              .anyMatch(c -> c.isVirtual() && c.getVirtualColumnStatus() != null
                && (c.getVirtualColumnStatus().isPendingMetadata() || c.getVirtualColumnStatus().shouldProcess())))
            || (table.getForeignKeys() != null && table.getForeignKeys().stream()
              .anyMatch(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus() != null
                && (fk.getVirtualForeignKeysStatus().isPendingMetadata()
                  || fk.getVirtualForeignKeysStatus().shouldProcess()))));

        hasLobs = context.getCollectionStatus().getTables().stream().filter(t -> t.getColumns() != null)
          .flatMap(t -> t.getColumns().stream()).map(ColumnStatus::getLobTextExtractionStatus)
          .anyMatch(status -> status != null && (status.isPendingMetadata() || status.shouldProcess()));
      }

      boolean hasDenormalizations = false;
      Set<String> denormEntries = context.getCollectionStatus().getDenormalizations();
      if (denormEntries != null && !denormEntries.isEmpty()) {
        hasDenormalizations = denormEntries.stream().map(context::getDenormalizeConfig)
          .anyMatch(config -> config != null
            && (config.getState() == ViewerJobStatus.PENDING_METADATA || config.shouldProcess()));
      }

      return hasVirtuals || hasDenormalizations || hasLobs;
    };
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    return new ErrorPolicy(0, 0);
  }

  @Override
  public Tasklet createTasklet(JobContext context, ExecutionContext executionContext) {
    return (contribution, chunkContext) -> {

      ViewerMetadata metadata = context.getViewerDatabase().getMetadata();

      // 1. Process Virtual Entities Metadata (Tables, Columns, References)
      processVirtualEntities(context, metadata);

      // 2. Process Completed Denormalizations Metadata
      processDenormalization(context);

      // 3. Process Completed Lob Text Extractions Metadata
      processLobTextExtractions(context);

      // 4. Mark the overall collection status as fully processed
      context.getCollectionStatus().setNeedsToBeProcessed(false);

      metadataService.updateAndPersistMetadata(context);

      return RepeatStatus.FINISHED;
    };
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) {
    // Intentionally left empty.
  }

  private void processVirtualEntities(JobContext context, ViewerMetadata metadata) {
    if (context.getCollectionStatus().getTables() != null) {
      context.getCollectionStatus().getTables().forEach(table -> {

        processDeletions(table, metadata);

        if (table.getVirtualTableStatus() != null && table.getVirtualTableStatus().isPendingMetadata()) {
          synchronizeInheritedForeignKeysInStatus(context, table, metadata);
          table.getVirtualTableStatus().markAsProcessed();
        }

        if (table.getColumns() != null) {
          table.getColumns().stream().filter(
            c -> c.isVirtual() && c.getVirtualColumnStatus() != null && c.getVirtualColumnStatus().isPendingMetadata())
            .forEach(c -> c.getVirtualColumnStatus().markAsProcessed());
        }

        if (table.getForeignKeys() != null) {
          table.getForeignKeys().stream()
            .filter(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus() != null
              && fk.getVirtualForeignKeysStatus().isPendingMetadata())
            .forEach(fk -> fk.getVirtualForeignKeysStatus().markAsProcessed());
        }
      });

      context.getCollectionStatus().removeMarkedVirtualTables();
      context.getCollectionStatus().getTables().forEach(t -> {
        t.removeMarkedVirtualColumns();
        if (t.getForeignKeys() != null) {
          t.getForeignKeys().removeIf(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus() != null
            && fk.getVirtualForeignKeysStatus().isMarkedForRemoval());
        }
        t.recalculateVirtualColumnIndexes();
      });

      metadata.setSchemas(metadata.getSchemas());
    }
  }

  private void processDeletions(TableStatus tableStatus, ViewerMetadata shadowMetadata) {
    if (tableStatus.getVirtualTableStatus() != null && tableStatus.getVirtualTableStatus().isMarkedForRemoval()) {
      LOGGER.info("Removing virtual table from metadata: {}", tableStatus.getId());
      if (shadowMetadata.getSchemas() != null) {
        shadowMetadata.getSchemas().forEach(s -> {
          if (s.getTables() != null)
            s.getTables().removeIf(t -> t.getUuid().equals(tableStatus.getUuid()));
        });
      }
      return;
    }

    ViewerTable viewerTable = shadowMetadata.getTable(tableStatus.getUuid());
    if (viewerTable == null)
      return;

    if (tableStatus.getColumns() != null) {
      tableStatus.getColumns().stream()
        .filter(
          c -> c.isVirtual() && c.getVirtualColumnStatus() != null && c.getVirtualColumnStatus().isMarkedForRemoval())
        .forEach(c -> {
          LOGGER.info("Removing virtual column from metadata: {}", c.getId());
          viewerTable.getColumns().removeIf(vc -> vc.getSolrName().equals(c.getId()));
        });
    }

    if (tableStatus.getForeignKeys() != null && viewerTable.getForeignKeys() != null) {
      tableStatus.getForeignKeys().stream().filter(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus() != null
        && fk.getVirtualForeignKeysStatus().isMarkedForRemoval()).forEach(fk -> {
          LOGGER.info("Removing virtual foreign key from metadata: {}", fk.getName());
          viewerTable.getForeignKeys().removeIf(vfk -> vfk.getName().equals(fk.getName()));
        });
    }
  }

  private void processDenormalization(JobContext context) {
    Set<String> entries = context.getCollectionStatus().getDenormalizations();
    if (entries != null) {
      for (String entryID : entries) {
        DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

        if (config != null && config.getState() == ViewerJobStatus.PENDING_METADATA) {
          LOGGER.info("Injecting denormalized nested columns into the collection status for: {}", entryID);

          DenormalizationStepUtils.updateCollectionStatusInMemory(context.getCollectionStatus(), config,
            context.getViewerDatabase());

          config.setState(ViewerJobStatus.COMPLETED);
          config.setLastExecutionDate(new java.util.Date());
        }
      }
    }
  }

  private void synchronizeInheritedForeignKeysInStatus(JobContext context, TableStatus virtualTableStatus,
    ViewerMetadata metadata) {
    if (!virtualTableStatus.getVirtualTableStatus().getUseSourceTableForeignKeys())
      return;

    TableStatus sourceTableStatus = context.getCollectionStatus()
      .getTableStatus(virtualTableStatus.getVirtualTableStatus().getSourceTableUUID());

    if (sourceTableStatus == null || sourceTableStatus.getForeignKeys() == null)
      return;

    List<String> virtualColumnIds = virtualTableStatus.getColumns().stream().map(ColumnStatus::getId)
      .collect(Collectors.toList());

    if (virtualTableStatus.getForeignKeys() == null) {
      virtualTableStatus.setForeignKeys(new ArrayList<>());
    }

    for (ForeignKeysStatus sourceFk : sourceTableStatus.getForeignKeys()) {
      boolean isApplicable = sourceFk.getReferences().stream()
        .allMatch(ref -> virtualColumnIds.contains(ref.getSourceColumnId()));

      if (isApplicable) {
        boolean alreadyExists = virtualTableStatus.getForeignKeys().stream()
          .anyMatch(fk -> fk.getName().equals(sourceFk.getName()));

        if (!alreadyExists) {
          ForeignKeysStatus newFk = new ForeignKeysStatus();
          newFk
            .setId(sourceFk.getId() != null ? sourceFk.getId() + "_virtual" : java.util.UUID.randomUUID().toString());
          newFk.setName(sourceFk.getName());
          newFk.setSourceType(ViewerSourceType.VIRTUAL);
          newFk.setReferencedTableUUID(sourceFk.getReferencedTableUUID());
          newFk.setReferencedTableId(sourceFk.getReferencedTableId());

          List<ForeignKeysStatus.ReferencedColumnStatus> newRefs = new ArrayList<>();
          for (ForeignKeysStatus.ReferencedColumnStatus ref : sourceFk.getReferences()) {
            ForeignKeysStatus.ReferencedColumnStatus newRef = new ForeignKeysStatus.ReferencedColumnStatus();
            newRef.setSourceColumnId(ref.getSourceColumnId());
            newRef.setReferencedColumnId(ref.getReferencedColumnId());
            newRefs.add(newRef);
          }
          newFk.setReferences(newRefs);

          VirtualForeignKeysStatus vFkStatus = new VirtualForeignKeysStatus();
          vFkStatus.setProcessingState(ProcessingState.PROCESSED);
          vFkStatus.setLastExecutionDate(new Date());
          newFk.setVirtualForeignKeysStatus(vFkStatus);

          virtualTableStatus.getForeignKeys().add(newFk);
          LOGGER.info("Inherited foreign key '{}' synchronized to virtual TableStatus '{}'", newFk.getName(),
            virtualTableStatus.getId());

          ViewerTable sourceViewerTable = metadata.getTable(virtualTableStatus.getUuid());
          ViewerTable targetViewerTable = metadata.getTable(newFk.getReferencedTableUUID());

          if (sourceViewerTable != null && targetViewerTable != null) {
            if (sourceViewerTable.getForeignKeys() == null) {
              sourceViewerTable.setForeignKeys(new ArrayList<>());
            }

            sourceViewerTable.getForeignKeys()
              .add(VirtualSchemaBuilderUtils.buildViewerForeignKey(newFk, sourceViewerTable, targetViewerTable));
          }
        }
      }
    }
  }

  /**
   * Finalizes the state of LOB text extraction metadata. Successfully processed
   * columns are marked as PROCESSED regardless of whether text was found.
   */
  private void processLobTextExtractions(JobContext context) {
    if (context.getCollectionStatus().getTables() != null) {
      context.getCollectionStatus().getTables().forEach(table -> {
        if (table.getColumns() != null) {
          table.getColumns().forEach(column -> {
            LobTextExtractionStatus status = column.getLobTextExtractionStatus();

            if (status != null
              && (status.isPendingMetadata() || status.getProcessingState() == ProcessingState.TO_PROCESS
                || status.getProcessingState() == ProcessingState.TO_REMOVE)) {

              if (status.getProcessingState() == ProcessingState.TO_REMOVE) {
                // Clear state if the user explicitly requested removal
                status.setProcessingState(null);
                status.setExtractedAndIndexedText(false);
              } else {
                // Mark as processed to move out of PENDING/TO_PROCESS states
                status.markAsProcessed();
              }
            }
          });
        }
      });
    }
  }
}
