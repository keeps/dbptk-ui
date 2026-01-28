package com.databasepreservation.common.server.batch.steps.virtualColumn;

import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerType;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStepUtils {

  public static boolean hasProcessableVirtualColumns(TableStatus table) {
    return table.getColumns().stream().anyMatch(column -> ViewerType.dbTypes.VIRTUAL.equals(column.getType())
      && column.getVirtualColumnStatus() != null && column.getVirtualColumnStatus().shouldProcess());
  }
}
