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
public class VirtualTableRule extends AbstractIntegrityRule {
  @Override
  public void evaluate(CollectionStatus oldStatus, CollectionStatus newStatus, ConfigurationDependencyGraph graph,
    Set<String> violations) {

    if (newStatus.getTables() == null)
      return;

    for (TableStatus newTable : newStatus.getTables()) {
      if (newTable == null)
        continue;

      TableStatus oldTable = getTableByIdOrUuid(oldStatus, newTable);
      if (oldTable == null)
        continue;

      if (newTable.getVirtualTableStatus() != null && oldTable.getVirtualTableStatus() != null) {

        // 1. Check Table Deletion
        if (newTable.getVirtualTableStatus().getProcessingState() == ProcessingState.TO_REMOVE) {
          checkDependents(graph, oldTable.getUuid(), "Virtual Table", newTable.getName(), violations, "deleted");
        }
        // 2. Check Table Modification
        else if (isVirtualTableModified(oldTable, newTable)) {
          checkDependents(graph, oldTable.getUuid(), "Virtual Table", newTable.getName(), violations, "modified");
        }

        // 3. Check for specific columns removed from the Virtual Table
        if (oldTable.getColumns() != null) {
          for (ColumnStatus oldCol : oldTable.getColumns()) {
            if (oldCol == null)
              continue;
            if (newTable.getColumnById(oldCol.getId()) == null) {
              checkDependents(graph, oldCol.getId(), "Column", oldCol.getName(), violations,
                "removed from virtual table");
            }
          }
        }
      }
    }
  }

  private boolean isVirtualTableModified(TableStatus oldTable, TableStatus newTable) {
    String oldSource = oldTable.getVirtualTableStatus().getSourceTableUUID();
    String newSource = newTable.getVirtualTableStatus().getSourceTableUUID();
    boolean oldFkFlag = oldTable.getVirtualTableStatus().getUseSourceTableForeignKeys();
    boolean newFkFlag = newTable.getVirtualTableStatus().getUseSourceTableForeignKeys();

    return !Objects.equals(oldSource, newSource) || oldFkFlag != newFkFlag;
  }
}
