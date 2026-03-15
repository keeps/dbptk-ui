package com.databasepreservation.common.server.batch.steps.metadata;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.denormalization.DenormalizationStepUtils;

/**
 * The central and atomic synchronization point for all schema metadata
 * alterations.
 * <p>
 * <b>Architectural Note:</b> To prevent "Split-Brain" inconsistencies between
 * the Solr index and the physical JSON configuration files, this step acts as
 * the <b>Single Source of Truth</b> for finalizing the database schema. It
 * computes all pending schema mutations (such as new Virtual Tables, Virtual
 * Columns, and Nested Denormalization columns) in memory, and persists them
 * atomically in a single transaction.
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
   * Evaluates whether this metadata synchronization step needs to run.
   * <p>
   * It will trigger if the job processed ANY virtual entities OR if any
   * denormalization configurations successfully reached the
   * {@link ViewerJobStatus#COMPLETED} state.
   * </p>
   */
  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return context -> {
      boolean hasVirtuals = false;
      if (context.getCollectionStatus().getTables() != null) {
        hasVirtuals = context.getCollectionStatus().getTables().stream()
          .anyMatch(table -> (table.getVirtualTableStatus() != null && table.getVirtualTableStatus().shouldProcess())
            || (table.getColumns() != null && table.getColumns().stream().anyMatch(
              c -> c.isVirtual() && c.getVirtualColumnStatus() != null && c.getVirtualColumnStatus().shouldProcess()))
            || (table.getForeignKeys() != null && table.getForeignKeys().stream().anyMatch(fk -> fk.isVirtual()
              && fk.getVirtualForeignKeysStatus() != null && fk.getVirtualForeignKeysStatus().shouldProcess())));
      }

      boolean hasDenormalizations = false;
      Set<String> denormEntries = context.getCollectionStatus().getDenormalizations();
      if (denormEntries != null && !denormEntries.isEmpty()) {
        hasDenormalizations = denormEntries.stream().map(context::getDenormalizeConfig)
          .anyMatch(config -> config != null && config.shouldProcess());
      }

      return hasVirtuals || hasDenormalizations;
    };
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    return new ErrorPolicy(0, 0);
  }

  @Override
  public Tasklet createTasklet(JobContext context, ExecutionContext executionContext) {
    return (contribution, chunkContext) -> {

      // The metadataService provides an atomic wrapper for Solr and Disk updates
      metadataService.updateAndPersistMetadata(context, metadata -> {

        // 1. Process Virtual Entities Metadata (Tables, Columns, References)
        processVirtualEntities(context, metadata);

        // 2. Process Completed Denormalizations Metadata
        processDenormalization(context, metadata);

        // 3. Mark the overall collection status as fully processed
        context.getCollectionStatus().setNeedsToBeProcessed(false);
      });

      return org.springframework.batch.repeat.RepeatStatus.FINISHED;
    };
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) {
    // Intentionally left empty.
  }

  private void processVirtualEntities(JobContext context, ViewerMetadata metadata) {
    if (context.getCollectionStatus().getTables() != null) {
      context.getCollectionStatus().getTables().forEach(table -> {
        processVirtualTablesMetadata(table, metadata);
        processVirtualColumnsMetadata(table, metadata);
        processVirtualReferencesMetadata(table, metadata);
      });
    }

    // Clean up memory state for virtual tables after processing
    context.getCollectionStatus().removeMarkedVirtualTables();
  }

  private static void processDenormalization(JobContext context, ViewerMetadata metadata) {
    Set<String> entries = context.getCollectionStatus().getDenormalizations();
    if (entries != null) {
      // DenormalizationStepUtils requires a ViewerDatabase object to read the schema.
      // Since the service exposes only the ViewerMetadata, we wrap it in a temporary
      // object.
      ViewerDatabase tempDatabaseWrapper = new ViewerDatabase();
      tempDatabaseWrapper.setMetadata(metadata);

      for (String entryID : entries) {
        DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

        if (config != null && config.getState() == ViewerJobStatus.COMPLETED) {
          LOGGER.info("Injecting denormalized nested columns into the schema for: {}", entryID);
          // Inject the nested columns dynamically into the CollectionStatus memory tree
          DenormalizationStepUtils.updateCollectionStatusInMemory(context.getCollectionStatus(), config,
            tempDatabaseWrapper);
        }
      }
    }
  }

  private void processVirtualTablesMetadata(TableStatus tableStatus, ViewerMetadata metadata) {
    if (tableStatus.getVirtualTableStatus() == null)
      return;

    if (tableStatus.getVirtualTableStatus().isMarkedForRemoval()) {
      LOGGER.info("Removing virtual table from metadata: {}", tableStatus.getId());
      ViewerTable viewerTable = metadata.getTable(tableStatus.getUuid());
      if (viewerTable != null) {
        metadata.getSchema(viewerTable.getSchemaUUID()).getTables()
          .removeIf(t -> t.getUuid().equals(tableStatus.getUuid()));
      }
    } else {
      LOGGER.info("Adding/Updating virtual table in metadata: {}", tableStatus.getId());
      ViewerTable originalTable = metadata.getTable(tableStatus.getVirtualTableStatus().getSourceTableUUID());
      if (originalTable != null) {
        ViewerTable virtualViewerTable = buildVirtualViewerTable(tableStatus, originalTable);
        metadata.getSchema(virtualViewerTable.getSchemaUUID()).getTables()
          .removeIf(t -> t.getUuid().equals(virtualViewerTable.getUuid()));
        metadata.getSchema(virtualViewerTable.getSchemaUUID()).getTables().add(virtualViewerTable);
      }
    }
  }

  private void processVirtualColumnsMetadata(TableStatus tableStatus, ViewerMetadata metadata) {
    if (tableStatus.getColumns() == null)
      return;

    tableStatus.removeMarkedVirtualColumns(); // Clean memory first

    ViewerTable viewerTable = metadata.getTable(tableStatus.getUuid());
    if (viewerTable == null)
      return;

    tableStatus.getColumns().stream().filter(ColumnStatus::isVirtual).forEach(column -> {
      viewerTable.getColumns().removeIf(c -> c.getSolrName().equals(column.getId()));

      if (!column.getVirtualColumnStatus().isMarkedForRemoval()) {
        LOGGER.info("Adding/Updating virtual column '{}' in table '{}'", column.getId(), tableStatus.getId());
        viewerTable.getColumns().add(buildViewerColumn(column, viewerTable));
      } else {
        LOGGER.info(
          "virtual column '{}' in table '{}' is marked for removal and will be deleted from metadata in the latest step",
          column.getId(), tableStatus.getId());
      }
    });
  }

  private void processVirtualReferencesMetadata(TableStatus tableStatus, ViewerMetadata metadata) {
    if (tableStatus.getForeignKeys() == null)
      return;

    ViewerTable sourceViewerTable = metadata.getTable(tableStatus.getUuid());
    if (sourceViewerTable == null)
      return;

    tableStatus.getForeignKeys().stream().filter(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus() != null)
      .forEach(fkStatus -> {
        if (sourceViewerTable.getForeignKeys() != null) {
          sourceViewerTable.getForeignKeys().removeIf(fk -> fk.getName().equals(fkStatus.getName()));
        }

        if (!fkStatus.getVirtualForeignKeysStatus().isMarkedForRemoval()) {
          LOGGER.info("Adding/Updating virtual foreign key '{}' in table '{}'", fkStatus.getName(),
            tableStatus.getId());
          ViewerTable targetTable = metadata.getTable(fkStatus.getReferencedTableUUID());
          if (targetTable != null) {
            sourceViewerTable.getForeignKeys().add(buildViewerForeignKey(fkStatus, sourceViewerTable, targetTable));
          }
          fkStatus.getVirtualForeignKeysStatus().markAsProcessed(); // Updates memory state
        } else {
          LOGGER.info(
            "Virtual foreign key '{}' in table '{}' is marked for removal and will be deleted from metadata in the latest step",
            fkStatus.getName(), tableStatus.getId());
        }
      });

    // Clean memory state
    tableStatus.getForeignKeys()
      .removeIf(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus().isMarkedForRemoval());
  }

  private ViewerTable buildVirtualViewerTable(TableStatus tableStatus, ViewerTable originalTable) {
    ViewerTable viewerTable = new ViewerTable();
    viewerTable.setUuid(tableStatus.getUuid());
    viewerTable.setId(tableStatus.getId());
    viewerTable.setSourceType(ViewerSourceType.VIRTUAL);
    viewerTable.setName(tableStatus.getName());
    viewerTable.setDescription(tableStatus.getDescription());
    viewerTable.setFolder(tableStatus.getTableFolder());

    java.util.List<ViewerColumn> columnsToInclude = tableStatus.getColumns().stream()
      .map(column -> originalTable.getColumnBySolrName(column.getId())).filter(c -> c != null).map(originalCol -> {
        ViewerColumn newCol = new ViewerColumn();
        newCol.setSolrName(originalCol.getSolrName());
        newCol.setDisplayName(originalCol.getDisplayName());
        newCol.setType(originalCol.getType());
        newCol.setNillable(originalCol.getNillable());
        newCol.setDescription(originalCol.getDescription());
        newCol.setColumnIndexInEnclosingTable(originalCol.getColumnIndexInEnclosingTable());
        newCol.setDefaultValue(originalCol.getDefaultValue());
        newCol.setAutoIncrement(originalCol.getAutoIncrement());
        newCol.setSourceType(ViewerSourceType.VIRTUAL);
        return newCol;
      }).toList();

    viewerTable.setColumns(columnsToInclude);

    viewerTable.setSchemaUUID(originalTable.getSchemaUUID());
    viewerTable.setSchemaName(originalTable.getSchemaName());
    viewerTable.setCountRows(originalTable.getCountRows());
    viewerTable.setTriggers(originalTable.getTriggers());
    viewerTable.setCheckConstraints(originalTable.getCheckConstraints());
    viewerTable.setCandidateKeys(originalTable.getCandidateKeys());

    if (originalTable.getPrimaryKey() != null) {
      if (columnsToInclude.stream().anyMatch(c -> originalTable.getPrimaryKey().getColumnIndexesInViewerTable()
        .contains(c.getColumnIndexInEnclosingTable()))) {
        viewerTable.setPrimaryKey(originalTable.getPrimaryKey());
      }
    }

    viewerTable.setForeignKeys(new java.util.ArrayList<>());
    if (tableStatus.getVirtualTableStatus().getUseSourceTableForeignKeys()) {
      if (originalTable.getForeignKeys() != null) {
        for (ViewerColumn viewerColumn : columnsToInclude) {
          java.util.List<ViewerForeignKey> viewerForeignKeys = originalTable.getForeignKeys().stream()
            .filter(fk -> fk.getReferences().stream()
              .anyMatch(r -> r.getSourceColumnIndex().equals(viewerColumn.getColumnIndexInEnclosingTable())))
            .toList();
          if (!viewerForeignKeys.isEmpty()) {
            viewerForeignKeys.forEach(viewerForeignKey -> viewerForeignKey.setSourceType(ViewerSourceType.VIRTUAL));
            viewerTable.getForeignKeys().addAll(viewerForeignKeys);
          }
        }
      }
    }

    return viewerTable;
  }

  private ViewerColumn buildViewerColumn(ColumnStatus column, ViewerTable viewerTable) {
    ViewerColumn viewerColumn = new ViewerColumn();
    viewerColumn.setSourceType(ViewerSourceType.VIRTUAL);
    viewerColumn.setSolrName(column.getId());
    viewerColumn.setDisplayName(column.getCustomName());

    ViewerType viewerType = new ViewerType();
    viewerType.setDbType(column.getType());
    viewerType.setTypeName(column.getType().name());
    viewerColumn.setType(viewerType);

    viewerColumn.setDescription(column.getDescription());
    viewerColumn.setColumnIndexInEnclosingTable(column.getColumnIndex());
    viewerColumn.setNillable(true);
    return viewerColumn;
  }

  private ViewerForeignKey buildViewerForeignKey(ForeignKeysStatus fkStatus, ViewerTable sourceTable,
    ViewerTable targetTable) {
    ViewerForeignKey vfk = new ViewerForeignKey();
    vfk.setName(fkStatus.getName());
    vfk.setSourceType(ViewerSourceType.VIRTUAL);
    vfk.setReferencedTableUUID(fkStatus.getReferencedTableUUID());
    vfk.setReferencedTableId(fkStatus.getReferencedTableId());
    vfk.setDescription("Virtual Reference created via UI");

    java.util.List<ViewerReference> references = new java.util.ArrayList<>();

    if (fkStatus.getReferences() != null) {
      for (ForeignKeysStatus.ReferencedColumnStatus refStatus : fkStatus.getReferences()) {
        ViewerColumn sourceCol = sourceTable.getColumnBySolrName(refStatus.getSourceColumnId());
        ViewerColumn targetCol = targetTable.getColumnBySolrName(refStatus.getReferencedColumnId());

        if (sourceCol != null && targetCol != null) {
          ViewerReference ref = new ViewerReference();
          ref.setSourceColumnIndex(sourceCol.getColumnIndexInEnclosingTable());
          ref.setReferencedColumnIndex(targetCol.getColumnIndexInEnclosingTable());
          references.add(ref);
        }
      }
    }

    vfk.setReferences(references);
    return vfk;
  }
}
