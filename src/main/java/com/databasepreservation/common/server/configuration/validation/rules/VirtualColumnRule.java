package com.databasepreservation.common.server.configuration.validation.rules;

import java.util.Objects;
import java.util.Set;

import com.databasepreservation.common.client.models.status.ConfigurationDependencyGraph;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnRule extends AbstractIntegrityRule {
  @Override
  public void evaluate(CollectionStatus oldStatus, CollectionStatus newStatus, ConfigurationDependencyGraph graph,
    Set<String> violations) {

    if (newStatus.getTables() == null)
      return;

    for (TableStatus newTable : newStatus.getTables()) {
      if (newTable == null || newTable.getColumns() == null)
        continue;

      TableStatus oldTable = getTableByIdOrUuid(oldStatus, newTable);
      if (oldTable == null)
        continue;

      for (ColumnStatus newCol : newTable.getColumns()) {
        if (newCol == null)
          continue;

        ColumnStatus oldCol = oldTable.getColumnById(newCol.getId());

        if (oldCol != null && oldCol.getVirtualColumnStatus() != null && newCol.getVirtualColumnStatus() != null) {

          if (newCol.getVirtualColumnStatus().getProcessingState() == ProcessingState.TO_REMOVE) {
            checkDependents(graph, oldCol.getId(), "Virtual Column", newCol.getName(), violations, "deleted");
          } else if (isVirtualColumnModified(oldCol, newCol)) {
            checkDependents(graph, oldCol.getId(), "Virtual Column", newCol.getName(), violations, "modified");
          }

        }
      }
    }
  }

  private boolean isVirtualColumnModified(ColumnStatus oldCol, ColumnStatus newCol) {
    String oldTemplate = oldCol.getVirtualColumnStatus().getTemplateStatus() != null
      ? oldCol.getVirtualColumnStatus().getTemplateStatus().getTemplate()
      : "";
    String newTemplate = newCol.getVirtualColumnStatus().getTemplateStatus() != null
      ? newCol.getVirtualColumnStatus().getTemplateStatus().getTemplate()
      : "";

    return !Objects.equals(oldTemplate, newTemplate);
  }
}
