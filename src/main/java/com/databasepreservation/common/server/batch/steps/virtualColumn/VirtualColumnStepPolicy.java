package com.databasepreservation.common.server.batch.steps.virtualColumn;

import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.server.batch.steps.common.StepExecutionPolicy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualColumnStepPolicy implements StepExecutionPolicy {
  @Override
  public boolean shouldExecute(String databaseUUID, CollectionStatus status) {

    if (status.getTables() == null)
      return false;

    return status.getTables().stream().filter(table -> table.getColumns() != null)
      .flatMap(table -> table.getColumns().stream()).anyMatch(this::isVirtualProcessable);
  }

  private boolean isVirtualProcessable(ColumnStatus column) {
    return ViewerType.dbTypes.VIRTUAL.equals(column.getType()) && column.getVirtualColumnStatus() != null
      && column.getVirtualColumnStatus().shouldProcess();
  }
}
