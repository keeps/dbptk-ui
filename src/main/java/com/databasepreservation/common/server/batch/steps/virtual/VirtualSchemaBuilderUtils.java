package com.databasepreservation.common.server.batch.steps.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerReference;
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
}
