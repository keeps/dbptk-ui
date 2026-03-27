package com.databasepreservation.common.server.batch.steps.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.databasepreservation.common.client.models.structure.ViewerSchema;
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
  public long calculateWorkload(JobContext context) {
    return 0;
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
      }

      boolean hasDenormalizations = false;
      Set<String> denormEntries = context.getCollectionStatus().getDenormalizations();
      if (denormEntries != null && !denormEntries.isEmpty()) {
        hasDenormalizations = denormEntries.stream().map(context::getDenormalizeConfig)
          .anyMatch(config -> config != null
            && (config.getState() == ViewerJobStatus.PENDING_METADATA || config.shouldProcess()));
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

        // Process only if it's pending metadata finalization
        if (config != null && config.getState() == ViewerJobStatus.PENDING_METADATA) {
          LOGGER.info("Injecting denormalized nested columns into the schema for: {}", entryID);
          // Inject the nested columns dynamically into the CollectionStatus memory tree
          DenormalizationStepUtils.updateCollectionStatusInMemory(context.getCollectionStatus(), config,
            tempDatabaseWrapper);

          config.setState(ViewerJobStatus.COMPLETED);
          config.setLastExecutionDate(new java.util.Date());
        }
      }
    }
  }

  private void processVirtualTablesMetadata(TableStatus tableStatus, ViewerMetadata metadata) {
    if (tableStatus.getVirtualTableStatus() == null)
      return;

    boolean isPending = tableStatus.getVirtualTableStatus().isPendingMetadata();
    boolean isRemoval = tableStatus.getVirtualTableStatus().isMarkedForRemoval();

    // 1. Gatekeeper Check
    if (!isPending && !isRemoval)
      return;

    if (isRemoval) {
      LOGGER.info("Removing virtual table from metadata: {}", tableStatus.getId());
      if (metadata.getSchemas() != null) {
        metadata.getSchemas().forEach(schema -> {
          if (schema.getTables() != null) {
            schema.getTables().removeIf(t -> t.getUuid().equals(tableStatus.getUuid()));
          }
        });
      }
    } else {
      LOGGER.info("Adding/Updating virtual table in metadata: {}", tableStatus.getId());
      ViewerTable originalTable = metadata.getTable(tableStatus.getVirtualTableStatus().getSourceTableUUID());
      if (originalTable != null) {
        ViewerTable virtualViewerTable = buildVirtualViewerTable(tableStatus, originalTable);
        ViewerSchema schema = metadata.getSchema(virtualViewerTable.getSchemaUUID());
        if (schema != null && schema.getTables() != null) {
          schema.getTables().removeIf(t -> t.getUuid().equals(virtualViewerTable.getUuid()));
          schema.getTables().add(virtualViewerTable);
        }
      }
    }

    // 2. Force ViewerMetadata to rebuild its internal maps (cache fix)
    metadata.setSchemas(metadata.getSchemas());

    // 3. Close the loop
    if (isPending) {
      tableStatus.getVirtualTableStatus().markAsProcessed();
    }
  }

  private void processVirtualColumnsMetadata(TableStatus tableStatus, ViewerMetadata metadata) {
    if (tableStatus.getColumns() == null)
      return;

    ViewerTable viewerTable = metadata.getTable(tableStatus.getUuid());
    if (viewerTable == null)
      return;

    // Ensure columns list is mutable before modifying
    if (!(viewerTable.getColumns() instanceof ArrayList)) {
      viewerTable.setColumns(new ArrayList<>(viewerTable.getColumns()));
    }

    tableStatus.getColumns().stream().filter(ColumnStatus::isVirtual)
      .filter(c -> c.getVirtualColumnStatus() != null
        && (c.getVirtualColumnStatus().isPendingMetadata() || c.getVirtualColumnStatus().isMarkedForRemoval()))
      .forEach(column -> {

        viewerTable.getColumns().removeIf(c -> c.getSolrName().equals(column.getId()));

        if (column.getVirtualColumnStatus().isPendingMetadata()) {
          LOGGER.info("Adding/Updating virtual column '{}' in table '{}'", column.getId(), tableStatus.getId());
          viewerTable.getColumns().add(buildViewerColumn(column, viewerTable));

          // Close the loop
          column.getVirtualColumnStatus().markAsProcessed();
        } else {
          LOGGER.info("Virtual column '{}' in table '{}' is marked for removal and was deleted from metadata",
            column.getId(), tableStatus.getId());
        }
      });

    tableStatus.removeMarkedVirtualColumns();
    metadata.setSchemas(metadata.getSchemas());
  }

  private void processVirtualReferencesMetadata(TableStatus tableStatus, ViewerMetadata metadata) {
    if (tableStatus.getForeignKeys() == null)
      return;

    ViewerTable sourceViewerTable = metadata.getTable(tableStatus.getUuid());
    if (sourceViewerTable == null)
      return;

    if (sourceViewerTable.getForeignKeys() == null) {
      sourceViewerTable.setForeignKeys(new ArrayList<>());
    }

    tableStatus.getForeignKeys().stream().filter(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus() != null)
      .filter(fk -> fk.getVirtualForeignKeysStatus().isPendingMetadata()
        || fk.getVirtualForeignKeysStatus().isMarkedForRemoval())
      .forEach(fkStatus -> {

        sourceViewerTable.getForeignKeys().removeIf(fk -> fk.getName().equals(fkStatus.getName()));

        if (fkStatus.getVirtualForeignKeysStatus().isPendingMetadata()) {
          LOGGER.info("Adding/Updating virtual foreign key '{}' in table '{}'", fkStatus.getName(),
            tableStatus.getId());
          ViewerTable targetTable = metadata.getTable(fkStatus.getReferencedTableUUID());
          if (targetTable != null) {
            sourceViewerTable.getForeignKeys().add(buildViewerForeignKey(fkStatus, sourceViewerTable, targetTable));
          }
          fkStatus.getVirtualForeignKeysStatus().markAsProcessed();
        } else {
          LOGGER.info("Virtual foreign key '{}' in table '{}' is marked for removal and was deleted from metadata",
            fkStatus.getName(), tableStatus.getId());
        }
      });

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

    List<ViewerColumn> columnsToInclude = tableStatus.getColumns().stream()
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
      }).collect(Collectors.toList());

    viewerTable.setColumns(new ArrayList<>(columnsToInclude));
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

    viewerTable.setForeignKeys(new ArrayList<>());
    if (tableStatus.getVirtualTableStatus().getUseSourceTableForeignKeys()) {
      if (originalTable.getForeignKeys() != null) {
        for (ViewerColumn viewerColumn : columnsToInclude) {
          List<ViewerForeignKey> viewerForeignKeys = originalTable.getForeignKeys().stream()
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

    List<ViewerReference> references = new ArrayList<>();

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