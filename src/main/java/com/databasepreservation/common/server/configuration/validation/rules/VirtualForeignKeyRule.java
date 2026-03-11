package com.databasepreservation.common.server.configuration.validation.rules;

import java.util.Objects;
import java.util.Set;

import com.databasepreservation.common.client.models.status.ConfigurationDependencyGraph;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualForeignKeyRule extends AbstractIntegrityRule {
  @Override
  public void evaluate(CollectionStatus oldStatus, CollectionStatus newStatus, ConfigurationDependencyGraph graph,
    Set<String> violations) {

    if (newStatus.getTables() == null)
      return;

    for (TableStatus newTable : newStatus.getTables()) {
      if (newTable == null || newTable.getForeignKeys() == null)
        continue;

      TableStatus oldTable = getTableByIdOrUuid(oldStatus, newTable);
      if (oldTable == null || oldTable.getForeignKeys() == null)
        continue;

      for (ForeignKeysStatus newFk : newTable.getForeignKeys()) {
        if (newFk == null)
          continue;

        ForeignKeysStatus oldFk = getFkById(oldTable, newFk.getId());

        if (oldFk != null && oldFk.getVirtualForeignKeysStatus() != null
          && newFk.getVirtualForeignKeysStatus() != null) {

          if (newFk.getVirtualForeignKeysStatus().getProcessingState() == ProcessingState.TO_REMOVE) {
            checkDependents(graph, oldFk.getId(), "Virtual Relation (FK)", newFk.getName(), violations, "deleted");
          } else if (isVirtualFkModified(oldFk, newFk)) {
            checkDependents(graph, oldFk.getId(), "Virtual Relation (FK)", newFk.getName(), violations, "modified");
          }

        }
      }
    }
  }

  private ForeignKeysStatus getFkById(TableStatus table, String fkId) {
    for (ForeignKeysStatus fk : table.getForeignKeys()) {
      if (fk != null && Objects.equals(fkId, fk.getId()))
        return fk;
    }
    return null;
  }

  private boolean isVirtualFkModified(ForeignKeysStatus oldFk, ForeignKeysStatus newFk) {
    String oldTemplate = oldFk.getVirtualForeignKeysStatus().getTemplateStatus() != null
      ? oldFk.getVirtualForeignKeysStatus().getTemplateStatus().getTemplate()
      : "";
    String newTemplate = newFk.getVirtualForeignKeysStatus().getTemplateStatus() != null
      ? newFk.getVirtualForeignKeysStatus().getTemplateStatus().getTemplate()
      : "";

    return !Objects.equals(oldTemplate, newTemplate);
  }
}
