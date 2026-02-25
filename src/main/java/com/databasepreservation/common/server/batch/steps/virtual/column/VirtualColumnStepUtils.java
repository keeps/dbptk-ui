package com.databasepreservation.common.server.batch.steps.virtual.column;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.BatchStatus;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStepUtils {
  static boolean hasVirtualColumnsToProcess(TableStatus table) {
    return table.getColumns().stream().anyMatch(VirtualColumnStepUtils::shouldProcess);
  }

  private static boolean shouldProcess(ColumnStatus column) {
    return column.getVirtualColumnStatus() != null && column.getVirtualColumnStatus().shouldProcess();
  }

  static boolean isMarkedForRemoval(VirtualColumnStatus vcs) {
    return vcs != null && vcs.getProcessingState() == ProcessingState.TO_REMOVE;
  }

  static TableStatus findTableStatus(JobContext context, String tableId) {
    return context.getCollectionStatus().getTables().stream().filter(t -> t.getId().equals(tableId)).findFirst()
      .orElse(null);
  }

  static boolean isVirtual(ColumnStatus c) {
    return ViewerType.dbTypes.VIRTUAL.equals(c.getType());
  }

  static void updateProcessedColumnsStateInMemory(TableStatus tableStatus, BatchStatus status) {
    Date now = new Date();
    tableStatus.getColumns().stream().filter(VirtualColumnStepUtils::isVirtual)
      .filter(VirtualColumnStepUtils::shouldProcess)
      .filter(c -> !VirtualColumnStepUtils.isMarkedForRemoval(c.getVirtualColumnStatus())).forEach(c -> {
        VirtualColumnStatus vcs = c.getVirtualColumnStatus();
        vcs.setProcessingState(ProcessingState.PROCESSED);
        vcs.setLastExecutionDate(now);
        recalculateVirtualColumnIndexes(tableStatus);
      });
  }

  static void removeMarkedVirtualColumnsInMemory(TableStatus tableStatus) {
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
}
