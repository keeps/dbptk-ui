package com.databasepreservation.common.server.configuration.validation;

import java.util.List;

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
import com.databasepreservation.common.server.ConfigurationManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationGraphBuilder {
  private final ConfigurationManager configManager;

  public ConfigurationGraphBuilder(ConfigurationManager configManager) {
    this.configManager = configManager;
  }

  /**
   * Builds the DAG based on the old topology, but injects the processing states
   * from the new configuration so the algorithm knows what is intended to be
   * deleted.
   */
  public ConfigurationDependencyGraph buildMergedGraph(String databaseUUID, CollectionStatus oldStatus,
    CollectionStatus newStatus) {
    ConfigurationDependencyGraph graph = new ConfigurationDependencyGraph();

    if (oldStatus.getTables() != null) {
      for (TableStatus table : oldStatus.getTables()) {
        if (table == null)
          continue;
        mapVirtualTables(graph, table);
        mapVirtualColumns(graph, table);
        mapVirtualForeignKeys(graph, table);
      }
    }

    mapDenormalizations(graph, databaseUUID, oldStatus);
    injectNewStates(graph, newStatus);

    return graph;
  }

  private void injectNewStates(ConfigurationDependencyGraph graph, CollectionStatus newStatus) {
    if (newStatus.getTables() == null)
      return;
    for (TableStatus table : newStatus.getTables()) {
      if (table == null)
        continue;

      if (table.getVirtualTableStatus() != null) {
        graph.updateNodeState(table.getUuid(), table.getVirtualTableStatus().getProcessingState());
      }

      if (table.getColumns() != null) {
        for (ColumnStatus col : table.getColumns()) {
          if (col != null && col.getVirtualColumnStatus() != null) {
            graph.updateNodeState(col.getId(), col.getVirtualColumnStatus().getProcessingState());
          }
        }
      }

      if (table.getForeignKeys() != null) {
        for (ForeignKeysStatus fk : table.getForeignKeys()) {
          if (fk != null && fk.getVirtualForeignKeysStatus() != null) {
            graph.updateNodeState(fk.getId(), fk.getVirtualForeignKeysStatus().getProcessingState());
          }
        }
      }
    }
  }

  private void mapVirtualTables(ConfigurationDependencyGraph graph, TableStatus table) {
    ProcessingState state = table.getVirtualTableStatus() != null ? table.getVirtualTableStatus().getProcessingState()
      : ProcessingState.PROCESSED;
    graph.addNode(table.getUuid(), state, "Virtual Table '" + table.getName() + "'");

    if (table.getVirtualTableStatus() != null && table.getVirtualTableStatus().getSourceTableUUID() != null) {
      graph.addDependencyEdge(table.getVirtualTableStatus().getSourceTableUUID(), table.getUuid());
    }
  }

  private void mapVirtualColumns(ConfigurationDependencyGraph graph, TableStatus table) {
    if (table.getColumns() == null)
      return;
    for (ColumnStatus col : table.getColumns()) {
      if (col == null)
        continue;
      ProcessingState state = col.getVirtualColumnStatus() != null ? col.getVirtualColumnStatus().getProcessingState()
        : ProcessingState.PROCESSED;
      String type = col.getVirtualColumnStatus() != null ? "Virtual Column" : "Column";

      graph.addNode(col.getId(), state, type + " '" + col.getName() + "' (Table: '" + table.getName() + "')");

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
      if (fk == null)
        continue;
      ProcessingState state = fk.getVirtualForeignKeysStatus() != null
        ? fk.getVirtualForeignKeysStatus().getProcessingState()
        : ProcessingState.PROCESSED;
      String type = fk.getVirtualForeignKeysStatus() != null ? "Virtual Relation" : "Relation";

      graph.addNode(fk.getId(), state, type + " '" + fk.getName() + "' (Table: '" + table.getName() + "')");

      if (table.getUuid() != null)
        graph.addDependencyEdge(table.getUuid(), fk.getId());
      if (fk.getReferencedTableUUID() != null)
        graph.addDependencyEdge(fk.getReferencedTableUUID(), fk.getId());

      if (fk.getReferences() != null) {
        for (ForeignKeysStatus.ReferencedColumnStatus ref : fk.getReferences()) {
          if (ref == null)
            continue;
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

          TableStatus t = getTableByUuid(status, denormConfig.getTableUUID());
          String tableName = t != null ? t.getName() : "Unknown";

          graph.addNode(denormConfig.getId(), denormConfig.getProcessingState(),
            "Denormalization on table '" + tableName + "'");

          if (denormConfig.getTableUUID() != null)
            graph.addDependencyEdge(denormConfig.getTableUUID(), denormConfig.getId());
          if (denormConfig.getRelatedTables() != null)
            extractDenormDependencies(graph, denormConfig.getId(), denormConfig.getRelatedTables());
        }
      } catch (Exception e) {
        // Silently skip
      }
    }
  }

  private void extractDenormDependencies(ConfigurationDependencyGraph graph, String denormId,
    List<RelatedTablesConfiguration> relatedTables) {
    for (RelatedTablesConfiguration relatedTable : relatedTables) {
      if (relatedTable == null)
        continue;

      if (relatedTable.getTableUUID() != null)
        graph.addDependencyEdge(relatedTable.getTableUUID(), denormId);
      if (relatedTable.getReferencedTableUUID() != null)
        graph.addDependencyEdge(relatedTable.getReferencedTableUUID(), denormId);

      if (relatedTable.getReferences() != null) {
        for (ReferencesConfiguration ref : relatedTable.getReferences()) {
          if (ref == null)
            continue;
          if (ref.getSourceTable() != null && ref.getSourceTable().getSolrName() != null)
            graph.addDependencyEdge(ref.getSourceTable().getSolrName(), denormId);
          if (ref.getReferencedTable() != null && ref.getReferencedTable().getSolrName() != null)
            graph.addDependencyEdge(ref.getReferencedTable().getSolrName(), denormId);
        }
      }

      if (relatedTable.getColumnsIncluded() != null) {
        for (RelatedColumnConfiguration colIncluded : relatedTable.getColumnsIncluded()) {
          if (colIncluded != null && colIncluded.getSolrName() != null)
            graph.addDependencyEdge(colIncluded.getSolrName(), denormId);
        }
      }

      if (relatedTable.getRelatedTables() != null && !relatedTable.getRelatedTables().isEmpty()) {
        extractDenormDependencies(graph, denormId, relatedTable.getRelatedTables());
      }
    }
  }

  private TableStatus getTableByUuid(CollectionStatus status, String uuid) {
    if (status.getTables() == null || uuid == null)
      return null;
    for (TableStatus table : status.getTables()) {
      if (uuid.equals(table.getUuid()) || uuid.equals(table.getId()))
        return table;
    }
    return null;
  }
}
