package com.databasepreservation.common.server.batch.steps.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualSchemaBuilderUtils {
  public static ViewerTable buildVirtualViewerTable(TableStatus tableStatus, ViewerTable originalTable) {
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

    return viewerTable;
  }

  public static ViewerColumn buildViewerColumn(ColumnStatus column) {
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

  public static ViewerForeignKey buildViewerForeignKey(ForeignKeysStatus fkStatus, ViewerTable sourceTable,
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

  /**
   * Creates a deep-ish clone of the ViewerMetadata to avoid polluting the Solr
   * cache, then applies all pending virtual configurations (Tables, Columns,
   * References, Denormalizations) to create a "Projected" state for the UI to
   * consume.
   */
  public static ViewerDatabase projectDatabase(ViewerDatabase originalDb, CollectionStatus collectionStatus) {
    if (originalDb == null || originalDb.getMetadata() == null || collectionStatus == null) {
      return originalDb;
    }

    ViewerDatabase projectedDb = new ViewerDatabase();

    projectedDb.setUuid(originalDb.getUuid());
    projectedDb.setVersion(originalDb.getVersion());
    projectedDb.setPath(originalDb.getPath());
    projectedDb.setStatus(originalDb.getStatus());

    ViewerMetadata origMeta = originalDb.getMetadata();
    ViewerMetadata projectedMetadata = new ViewerMetadata();

    projectedMetadata.setName(origMeta.getName());
    projectedMetadata.setDescription(origMeta.getDescription());
    projectedMetadata.setArchiver(origMeta.getArchiver());
    projectedMetadata.setArchiverContact(origMeta.getArchiverContact());
    projectedMetadata.setDataOwner(origMeta.getDataOwner());
    projectedMetadata.setDataOriginTimespan(origMeta.getDataOriginTimespan());
    projectedMetadata.setProducerApplication(origMeta.getProducerApplication());
    projectedMetadata.setArchivalDate(origMeta.getArchivalDate());
    projectedMetadata.setClientMachine(origMeta.getClientMachine());
    projectedMetadata.setDatabaseProduct(origMeta.getDatabaseProduct());
    projectedMetadata.setDatabaseUser(origMeta.getDatabaseUser());
    projectedMetadata.setUsers(origMeta.getUsers() != null ? new ArrayList<>(origMeta.getUsers()) : null);
    projectedMetadata.setRoles(origMeta.getRoles() != null ? new ArrayList<>(origMeta.getRoles()) : null);
    projectedMetadata
      .setPrivileges(origMeta.getPrivileges() != null ? new ArrayList<>(origMeta.getPrivileges()) : null);

    List<ViewerSchema> projectedSchemas = new ArrayList<>();
    for (ViewerSchema origSchema : origMeta.getSchemas()) {
      ViewerSchema newSchema = new ViewerSchema();
      newSchema.setUuid(origSchema.getUuid());
      newSchema.setName(origSchema.getName());
      newSchema.setDescription(origSchema.getDescription());
      newSchema.setFolder(origSchema.getFolder());

      newSchema.setViews(origSchema.getViews() != null ? new ArrayList<>(origSchema.getViews()) : new ArrayList<>());
      newSchema
        .setRoutines(origSchema.getRoutines() != null ? new ArrayList<>(origSchema.getRoutines()) : new ArrayList<>());

      List<ViewerTable> newTables = new ArrayList<>();
      for (ViewerTable origTable : origSchema.getTables()) {
        newTables.add(cloneTableForProjection(origTable));
      }
      newSchema.setTables(newTables);
      projectedSchemas.add(newSchema);
    }

    projectedMetadata.setSchemas(projectedSchemas);
    projectedDb.setMetadata(projectedMetadata);

    if (collectionStatus.getTables() != null) {

      for (TableStatus tStatus : collectionStatus.getTables()) {
        if (tStatus.getVirtualTableStatus() != null
          && (tStatus.getVirtualTableStatus().getProcessingState() == ProcessingState.TO_PROCESS
            || tStatus.getVirtualTableStatus().getProcessingState() == ProcessingState.PENDING_METADATA)) {

          ViewerTable sourceTable = projectedMetadata.getTable(tStatus.getVirtualTableStatus().getSourceTableUUID());
          if (sourceTable != null) {
            ViewerTable virtualTable = buildVirtualViewerTable(tStatus, sourceTable);
            ViewerSchema schema = projectedMetadata.getSchema(virtualTable.getSchemaUUID());
            if (schema != null) {
              schema.getTables().add(virtualTable);
            }
          }
        }
      }

      projectedMetadata.setSchemas(projectedMetadata.getSchemas());

      for (TableStatus tStatus : collectionStatus.getTables()) {
        ViewerTable targetTable = projectedMetadata.getTable(tStatus.getUuid());
        if (targetTable == null)
          continue;

        if (tStatus.getColumns() != null) {
          for (ColumnStatus cStatus : tStatus.getColumns()) {
            if (cStatus.isVirtual() && cStatus.getVirtualColumnStatus() != null
              && (cStatus.getVirtualColumnStatus().getProcessingState() == ProcessingState.TO_PROCESS
                || cStatus.getVirtualColumnStatus().getProcessingState() == ProcessingState.PENDING_METADATA)) {
              targetTable.getColumns().add(buildViewerColumn(cStatus));
            }
          }
        }

        if (tStatus.getForeignKeys() != null) {
          if (targetTable.getForeignKeys() == null)
            targetTable.setForeignKeys(new ArrayList<>());

          for (ForeignKeysStatus fkStatus : tStatus.getForeignKeys()) {
            if (fkStatus.isVirtual() && fkStatus.getVirtualForeignKeysStatus() != null
              && (fkStatus.getVirtualForeignKeysStatus().getProcessingState() == ProcessingState.TO_PROCESS
                || fkStatus.getVirtualForeignKeysStatus().getProcessingState() == ProcessingState.PENDING_METADATA)) {
              ViewerTable referencedTable = projectedMetadata.getTable(fkStatus.getReferencedTableUUID());

              if (referencedTable != null) {
                targetTable.getForeignKeys().add(buildViewerForeignKey(fkStatus, targetTable, referencedTable));
              }
            }
          }
        }
      }
    }

    return projectedDb;
  }

  private static ViewerTable cloneTableForProjection(ViewerTable original) {
    ViewerTable copy = new ViewerTable();
    copy.setUuid(original.getUuid());
    copy.setId(original.getId());
    copy.setName(original.getName());
    copy.setSchemaUUID(original.getSchemaUUID());
    copy.setSchemaName(original.getSchemaName());
    copy.setCountRows(original.getCountRows());
    copy.setSourceType(original.getSourceType());
    copy.setDescription(original.getDescription());
    copy.setFolder(original.getFolder());
    copy.setCustomView(original.isCustomView());
    copy.setMaterializedView(original.isMaterializedView());
    copy.setNameWithoutPrefix(original.getNameWithoutPrefix());
    copy.setPrimaryKey(original.getPrimaryKey());
    copy.setCandidateKeys(original.getCandidateKeys() != null ? new ArrayList<>(original.getCandidateKeys()) : null);
    copy.setCheckConstraints(
      original.getCheckConstraints() != null ? new ArrayList<>(original.getCheckConstraints()) : null);
    copy.setTriggers(original.getTriggers() != null ? new ArrayList<>(original.getTriggers()) : null);

    if (original.getColumns() != null) {
      copy.setColumns(new ArrayList<>(original.getColumns()));
    }

    if (original.getForeignKeys() != null) {
      copy.setForeignKeys(new ArrayList<>(original.getForeignKeys()));
    }
    return copy;
  }
}
