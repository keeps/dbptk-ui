package com.databasepreservation.common.server.batch.steps.extraction;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.LobTextExtractionStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.core.BatchConstants;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LobTextExtractionStepUtils {
  public static boolean hasLobsToProcess(TableStatus table) {
    if (table.getColumns() == null)
      return false;

    return table.getColumns().stream().anyMatch(c -> shouldProcess(c) || isMarkedForCleanup(c));
  }

  public static boolean shouldProcess(ColumnStatus column) {
    if (column == null || !isLobType(column))
      return false;
    LobTextExtractionStatus status = column.getLobTextExtractionStatus();

    return status != null && status.getProcessingState() != null && status.shouldProcess();
  }

  public static boolean isMarkedForCleanup(ColumnStatus column) {
    LobTextExtractionStatus status = column.getLobTextExtractionStatus();
    return status != null && status.getProcessingState() == ProcessingState.TO_REMOVE;
  }

  public static boolean isLobType(ColumnStatus column) {
    return ViewerType.dbTypes.BINARY.equals(column.getType())
      || (ViewerType.dbTypes.CLOB.equals(column.getType()) && column.isExternalLob());
  }

  public static void updateProcessedColumnsStateInMemory(TableStatus tableStatus) {
    Date now = new Date();
    if (tableStatus.getColumns() != null) {
      tableStatus.getColumns().stream().filter(c -> shouldProcess(c) || isMarkedForCleanup(c)).forEach(c -> {
        LobTextExtractionStatus status = c.getLobTextExtractionStatus();

        if (isMarkedForCleanup(c)) {
          status.setProcessingState(ProcessingState.TO_REMOVE);
        } else {
          status.markAsPendingMetadata();
        }
        status.setLastExecutionDate(now);
      });
    }
  }

  public static boolean isDKVersion(String version) {
    return version.equals(ViewerConstants.SIARD_DK_1007) || version.equals(ViewerConstants.SIARD_DK_1007_EXT)
      || version.equals(ViewerConstants.SIARD_DK_128) || version.equals(ViewerConstants.SIARD_DK_128_EXT);
  }

  public static void enrichPartitionContext(ExecutionContext partitionContext, TableStatus tableStatus) {
    partitionContext.put(BatchConstants.FILTER_KEY, FilterUtils.filterByTable(new Filter(), tableStatus.getId()));
    partitionContext.put(BatchConstants.FIELDS_KEY, new ArrayList<String>());
  }
}
