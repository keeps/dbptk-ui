package com.databasepreservation.common.server.batch.steps.virtual;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualTableStatus;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.BatchConstants;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualEntityStepUtils {
  static public boolean hasVirtualColumnsToProcess(TableStatus table) {
    return table.getColumns().stream().anyMatch(VirtualEntityStepUtils::shouldProcess);
  }

  public static void enrichVirtualColumnPartitionContext(ExecutionContext partitionContext, TableStatus tableStatus) {
    partitionContext.put(BatchConstants.FILTER_KEY, FilterUtils.filterByTable(new Filter(), tableStatus.getId()));
    partitionContext.put(BatchConstants.FIELDS_KEY, new ArrayList<String>());
  }

  private static boolean shouldProcess(ColumnStatus column) {
    return column.getVirtualColumnStatus() != null && column.getVirtualColumnStatus().shouldProcess();
  }

  public static boolean isMarkedForRemoval(VirtualColumnStatus vcs) {
    return vcs != null && vcs.getProcessingState() == ProcessingState.TO_REMOVE;
  }

  static public TableStatus findTableStatus(JobContext context, String tableId) {
    return context.getCollectionStatus().getTables().stream().filter(t -> t.getId().equals(tableId)).findFirst()
      .orElse(null);
  }

  public static boolean isVirtual(ColumnStatus c) {
    return ViewerType.dbTypes.VIRTUAL.equals(c.getType());
  }

  public static void updateProcessedColumnsStateInMemory(TableStatus tableStatus, BatchStatus status) {
    Date now = new Date();
    tableStatus.getColumns().stream().filter(VirtualEntityStepUtils::isVirtual)
      .filter(VirtualEntityStepUtils::shouldProcess)
      .filter(c -> !VirtualEntityStepUtils.isMarkedForRemoval(c.getVirtualColumnStatus())).forEach(c -> {
        VirtualColumnStatus vcs = c.getVirtualColumnStatus();
        vcs.setProcessingState(ProcessingState.PROCESSED);
        vcs.setLastExecutionDate(now);
        recalculateVirtualColumnIndexes(tableStatus);
      });
  }

  public static void removeMarkedVirtualColumnsInMemory(TableStatus tableStatus) {
    List<ColumnStatus> activeColumns = tableStatus.getColumns().stream()
      .filter(c -> !(isVirtual(c) && isMarkedForRemoval(c.getVirtualColumnStatus()))).collect(Collectors.toList());
    tableStatus.setColumns(activeColumns);
  }

  private static void recalculateVirtualColumnIndexes(TableStatus tableStatus) {
    int index = tableStatus.getColumns().stream().filter(c -> !isVirtual(c)).mapToInt(ColumnStatus::getColumnIndex)
      .max().orElse(-1) + 1;
    for (ColumnStatus column : tableStatus.getColumns()) {
      if (isVirtual(column)) {
        column.setColumnIndex(index++);
      }
    }
  }

  // Virtual table
  public static boolean hasVirtualTableToProcess(TableStatus table) {
    return table.getVirtualTableStatus() != null && table.getVirtualTableStatus().shouldProcess();
  }

  public static void enrichVirtualTablePartitionContext(ExecutionContext partitionContext, TableStatus tableStatus) {
    // For virtual tables, we need to filter by the source table to ensure we
    // process the correct rows, as the virtual table may have a different ID and
    // structure.
    String sourceTableUUID = tableStatus.getVirtualTableStatus().getSourceTableUUID();
    partitionContext.put(BatchConstants.FILTER_KEY, FilterUtils.filterByTableUUID(new Filter(), sourceTableUUID));
    partitionContext.put(BatchConstants.FIELDS_KEY, new ArrayList<String>());
  }

  public static void updateProcessedTableStateInMemory(TableStatus tableStatus, BatchStatus status) {
    Date now = new Date();
    VirtualTableStatus virtualTableStatus = tableStatus.getVirtualTableStatus();
    virtualTableStatus.setLastExecutionDate(now);
    virtualTableStatus.setProcessingState(ProcessingState.PROCESSED);
  }

  public static void removeMarkedVirtualTableInMemory(CollectionStatus collectionStatus) {
    List<TableStatus> activeTables = collectionStatus.getTables().stream()
      .filter(t -> !(t.getVirtualTableStatus() != null
        && t.getVirtualTableStatus().getProcessingState() == ProcessingState.TO_REMOVE))
      .collect(Collectors.toList());
    collectionStatus.setTables(activeTables);
  }
}
