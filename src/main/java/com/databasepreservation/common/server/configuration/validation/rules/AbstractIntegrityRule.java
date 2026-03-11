package com.databasepreservation.common.server.configuration.validation.rules;

import java.util.Set;

import com.databasepreservation.common.client.models.status.ConfigurationDependencyGraph;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class AbstractIntegrityRule implements IntegrityRule {
  /**
   * Queries the graph for active dependents and formats an HTML error message if
   * any are found.
   */
  protected void checkDependents(ConfigurationDependencyGraph graph, String nodeId, String entityType,
    String entityName, Set<String> violations, String action) {
    Set<String> activeDependents = graph.getActiveDependentLabels(nodeId);
    if (!activeDependents.isEmpty()) {
      violations.add(String.format(
        "&nbsp;&nbsp;&bull; <b>%s '%s'</b> cannot be %s because it is currently being used by:<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- %s",
        entityType, entityName, action, String.join("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- ", activeDependents)));
    }
  }

  /**
   * Safely retrieves a TableStatus from a CollectionStatus matching either ID or
   * UUID.
   */
  protected TableStatus getTableByIdOrUuid(CollectionStatus status, TableStatus targetTable) {
    if (status == null || targetTable == null)
      return null;

    TableStatus table = status.getTableStatusByTableId(targetTable.getId());
    return table != null ? table : status.getTableStatus(targetTable.getUuid());
  }
}
