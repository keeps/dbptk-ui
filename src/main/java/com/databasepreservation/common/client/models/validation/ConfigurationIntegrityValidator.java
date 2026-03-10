package com.databasepreservation.common.client.models.validation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.databasepreservation.common.client.models.status.ConfigurationDependencyGraph;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.exceptions.DependencyViolationException;
import com.databasepreservation.common.server.ConfigurationManager;

/**
 * Validates structural changes and deletions in the configuration to prevent
 * broken dependencies in downstream entities like Denormalizations or Virtual
 * Relations.
 */
public class ConfigurationIntegrityValidator {

  private final ConfigurationManager configManager;

  public ConfigurationIntegrityValidator(ConfigurationManager configManager) {
    this.configManager = configManager;
  }

  /**
   * Validates state transitions by comparing the existing state (oldStatus)
   * against the requested changes (newStatus). Accumulates all violations and
   * throws a single formatted exception if any breaking changes are detected.
   *
   * @param databaseUUID
   *          The database identifier.
   * @param oldStatus
   *          The current state of the collection saved on disk.
   * @param newStatus
   *          The requested new state from the user.
   * @throws DependencyViolationException
   *           if breaking changes are found.
   */
  public void validateStateTransitions(String databaseUUID, CollectionStatus oldStatus, CollectionStatus newStatus)
    throws DependencyViolationException {

    // If there is no old status, this is a fresh creation; no dependencies to
    // break.
    if (oldStatus == null)
      return;

    // Build the dependency graph based on the OLD status to map existing active
    // links.
    ConfigurationDependencyGraph oldGraph = buildDAG(databaseUUID, oldStatus);

    // Using LinkedHashSet to maintain insertion order and avoid duplicate error
    // messages.
    Set<String> violations = new LinkedHashSet<>();

    for (TableStatus newTable : newStatus.getTables()) {
      TableStatus oldTable = getTableByIdOrUuid(oldStatus, newTable);

      if (oldTable != null) {

        // 1. Validate Virtual Tables
        if (newTable.getVirtualTableStatus() != null && oldTable.getVirtualTableStatus() != null) {
          if (newTable.getVirtualTableStatus().getProcessingState() == ProcessingState.TO_REMOVE) {
            checkDependents(oldGraph, oldTable.getUuid(), "Virtual Table", newTable.getName(), violations, "deleted");
          } else if (isVirtualTableModified(oldTable, newTable)) {
            checkDependents(oldGraph, oldTable.getUuid(), "Virtual Table", newTable.getName(), violations, "modified");
          }

          // Check if any specific column was unchecked/removed from the virtual table
          for (ColumnStatus oldCol : oldTable.getColumns()) {
            if (newTable.getColumnById(oldCol.getId()) == null) {
              checkDependents(oldGraph, oldCol.getId(), "Column", oldCol.getName(), violations,
                "removed from virtual table");
            }
          }
        }

        // 2. Validate Virtual Columns
        for (ColumnStatus newCol : newTable.getColumns()) {
          ColumnStatus oldCol = oldTable.getColumnById(newCol.getId());

          if (oldCol != null && oldCol.getVirtualColumnStatus() != null && newCol.getVirtualColumnStatus() != null) {
            if (newCol.getVirtualColumnStatus().getProcessingState() == ProcessingState.TO_REMOVE) {
              checkDependents(oldGraph, oldCol.getId(), "Virtual Column", newCol.getName(), violations, "deleted");
            } else if (isVirtualColumnModified(oldCol, newCol)) {
              checkDependents(oldGraph, oldCol.getId(), "Virtual Column", newCol.getName(), violations, "modified");
            }
          }
        }

        // 3. Validate Virtual Foreign Keys
        if (newTable.getForeignKeys() != null && oldTable.getForeignKeys() != null) {
          for (ForeignKeysStatus newFk : newTable.getForeignKeys()) {
            ForeignKeysStatus oldFk = getFkById(oldTable, newFk.getId());

            if (oldFk != null && oldFk.getVirtualForeignKeysStatus() != null
              && newFk.getVirtualForeignKeysStatus() != null) {
              if (newFk.getVirtualForeignKeysStatus().getProcessingState() == ProcessingState.TO_REMOVE) {
                checkDependents(oldGraph, oldFk.getId(), "Virtual Relation (FK)", newFk.getName(), violations,
                  "deleted");
              } else if (isVirtualFkModified(oldFk, newFk)) {
                checkDependents(oldGraph, oldFk.getId(), "Virtual Relation (FK)", newFk.getName(), violations,
                  "modified");
              }
            }
          }
        }
      }
    }

    // Throw a formatted HTML exception if any active downstream dependency is
    // broken
    if (!violations.isEmpty()) {
      String errorMessage = "Cannot apply changes due to active dependencies:<br><br>"
        + String.join("<br>", violations);
      throw new DependencyViolationException(errorMessage);
    }
  }

  /**
   * Queries the graph for active dependents. If found, formats an HTML bullet
   * point message and adds it to the violations set.
   */
  private void checkDependents(ConfigurationDependencyGraph graph, String nodeId, String entityType, String entityName,
    Set<String> violations, String action) {
    Set<String> activeDependents = graph.getActiveDependents(nodeId);
    if (!activeDependents.isEmpty()) {
      violations.add(String.format("&nbsp;&nbsp;&bull; Cannot be <b>%s</b>: %s '%s' is required by [%s]", action,
        entityType, entityName, String.join(", ", activeDependents)));
    }
  }

  // =========================================================================
  // MODIFICATION DETECTION HELPERS
  // =========================================================================

  private TableStatus getTableByIdOrUuid(CollectionStatus status, TableStatus targetTable) {
    TableStatus table = status.getTableStatusByTableId(targetTable.getId());
    return table != null ? table : status.getTableStatus(targetTable.getUuid());
  }

  private ForeignKeysStatus getFkById(TableStatus table, String fkId) {
    for (ForeignKeysStatus fk : table.getForeignKeys()) {
      if (fkId.equals(fk.getId()))
        return fk;
    }
    return null;
  }

  private boolean isVirtualTableModified(TableStatus oldTable, TableStatus newTable) {
    String oldSource = oldTable.getVirtualTableStatus().getSourceTableUUID();
    String newSource = newTable.getVirtualTableStatus().getSourceTableUUID();
    boolean oldFkFlag = oldTable.getVirtualTableStatus().getUseSourceTableForeignKeys();
    boolean newFkFlag = newTable.getVirtualTableStatus().getUseSourceTableForeignKeys();

    return !Objects.equals(oldSource, newSource) || oldFkFlag != newFkFlag;
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

  private boolean isVirtualFkModified(ForeignKeysStatus oldFk, ForeignKeysStatus newFk) {
    String oldTemplate = oldFk.getVirtualForeignKeysStatus().getTemplateStatus() != null
      ? oldFk.getVirtualForeignKeysStatus().getTemplateStatus().getTemplate()
      : "";
    String newTemplate = newFk.getVirtualForeignKeysStatus().getTemplateStatus() != null
      ? newFk.getVirtualForeignKeysStatus().getTemplateStatus().getTemplate()
      : "";

    return !Objects.equals(oldTemplate, newTemplate);
  }

  // =========================================================================
  // GRAPH CONSTRUCTION METHODS
  // =========================================================================

  private ConfigurationDependencyGraph buildDAG(String databaseUUID, CollectionStatus status) {
    ConfigurationDependencyGraph graph = new ConfigurationDependencyGraph();

    for (TableStatus table : status.getTables()) {
      mapVirtualTables(graph, table);
      mapVirtualColumns(graph, table);
      mapVirtualForeignKeys(graph, table);
    }

    mapDenormalizations(graph, databaseUUID, status);
    return graph;
  }

  private void mapVirtualTables(ConfigurationDependencyGraph graph, TableStatus table) {
    ProcessingState state = table.getVirtualTableStatus() != null ? table.getVirtualTableStatus().getProcessingState()
      : ProcessingState.PROCESSED;
    graph.addNode(table.getUuid(), state);

    if (table.getVirtualTableStatus() != null && table.getVirtualTableStatus().getSourceTableUUID() != null) {
      graph.addDependencyEdge(table.getVirtualTableStatus().getSourceTableUUID(), table.getUuid());
    }
  }

  private void mapVirtualColumns(ConfigurationDependencyGraph graph, TableStatus table) {
    for (ColumnStatus col : table.getColumns()) {
      ProcessingState state = col.getVirtualColumnStatus() != null ? col.getVirtualColumnStatus().getProcessingState()
        : ProcessingState.PROCESSED;
      graph.addNode(col.getId(), state);

      if (col.getVirtualColumnStatus() != null && col.getVirtualColumnStatus().getSourceColumnsIds() != null) {
        for (String sourceColId : col.getVirtualColumnStatus().getSourceColumnsIds()) {
          graph.addDependencyEdge(sourceColId, col.getId());
        }
      }
    }
  }

  private void mapVirtualForeignKeys(ConfigurationDependencyGraph graph, TableStatus table) {
    if (table.getForeignKeys() == null)
      return;

    for (ForeignKeysStatus fk : table.getForeignKeys()) {
      ProcessingState state = fk.getVirtualForeignKeysStatus() != null
        ? fk.getVirtualForeignKeysStatus().getProcessingState()
        : ProcessingState.PROCESSED;
      graph.addNode(fk.getId(), state);

      // The FK depends on its own source table
      if (table.getUuid() != null) {
        graph.addDependencyEdge(table.getUuid(), fk.getId());
      }

      // The FK depends on the Virtual Table it is referencing
      if (fk.getReferencedTableUUID() != null) {
        graph.addDependencyEdge(fk.getReferencedTableUUID(), fk.getId());
      }

      if (fk.getReferences() != null) {
        for (ForeignKeysStatus.ReferencedColumnStatus ref : fk.getReferences()) {
          graph.addDependencyEdge(ref.getSourceColumnId(), fk.getId());
          graph.addDependencyEdge(ref.getReferencedColumnId(), fk.getId());
        }
      }
    }
  }

  private void mapDenormalizations(ConfigurationDependencyGraph graph, String databaseUUID, CollectionStatus status) {
    if (status.getDenormalizations() == null)
      return;

    for (String denormId : status.getDenormalizations()) {
      try {
        DenormalizeConfiguration denormConfig = configManager
          .getDenormalizeConfigurationFromCollectionStatusEntry(databaseUUID, denormId);

        if (denormConfig != null) {
          graph.addNode(denormConfig.getId(), denormConfig.getProcessingState());

          if (denormConfig.getTableUUID() != null) {
            graph.addDependencyEdge(denormConfig.getTableUUID(), denormConfig.getId());
          }

          if (denormConfig.getRelatedTables() != null) {
            extractDenormDependencies(graph, denormConfig.getId(), denormConfig.getRelatedTables());
          }
        }
      } catch (Exception e) {
        // Silently skip unreadable or missing configuration files to prevent full
        // process crash
      }
    }
  }

  private void extractDenormDependencies(ConfigurationDependencyGraph graph, String denormId,
    List<RelatedTablesConfiguration> relatedTables) {

    for (RelatedTablesConfiguration relatedTable : relatedTables) {
      if (relatedTable.getTableUUID() != null) {
        graph.addDependencyEdge(relatedTable.getTableUUID(), denormId);
      }
      if (relatedTable.getReferencedTableUUID() != null) {
        graph.addDependencyEdge(relatedTable.getReferencedTableUUID(), denormId);
      }

      // Map direct dependencies on relation columns (source/target columns mapped via
      // SolrName)
      if (relatedTable.getReferences() != null) {
        for (ReferencesConfiguration ref : relatedTable.getReferences()) {
          if (ref.getSourceTable() != null && ref.getSourceTable().getSolrName() != null) {
            graph.addDependencyEdge(ref.getSourceTable().getSolrName(), denormId);
          }
          if (ref.getReferencedTable() != null && ref.getReferencedTable().getSolrName() != null) {
            graph.addDependencyEdge(ref.getReferencedTable().getSolrName(), denormId);
          }
        }
      }

      // Map direct dependencies on the columns targeted for index inclusion
      if (relatedTable.getColumnsIncluded() != null) {
        for (RelatedColumnConfiguration colIncluded : relatedTable.getColumnsIncluded()) {
          if (colIncluded.getSolrName() != null) {
            graph.addDependencyEdge(colIncluded.getSolrName(), denormId);
          }
        }
      }

      // Recursively evaluate deeper levels of related tables
      if (relatedTable.getRelatedTables() != null && !relatedTable.getRelatedTables().isEmpty()) {
        extractDenormDependencies(graph, denormId, relatedTable.getRelatedTables());
      }
    }
  }
}
