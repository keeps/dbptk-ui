package com.databasepreservation.common.client.models.status;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.databasepreservation.common.client.models.status.collection.ProcessingState;

/**
 * A graph data structure representing configuration dependencies. Although it
 * functions primarily as a Directed Acyclic Graph (DAG), it safely supports
 * cyclic dependencies (e.g., Table A depends on Table B, and Table B depends on
 * Table A) through a Breadth-First Search (BFS) traversal. * Edge direction:
 * Provider (Dependency) -> Consumer (Dependent)
 */
public class ConfigurationDependencyGraph {

  private final Map<String, Set<String>> adjacencyList = new HashMap<>();
  private final Map<String, ProcessingState> nodeStates = new HashMap<>();
  private final Map<String, String> nodeLabels = new HashMap<>();

  /**
   * Registers a node in the graph with its current processing state. If the state
   * is null, it defaults to PROCESSED.
   *
   * @param id
   *          The unique identifier of the entity (UUID or ID).
   * @param state
   *          The current processing state of the entity.
   * @param label
   *          A human-readable label for the entity (e.g., "Virtual Column
   *          'col_name'")
   */
  public void addNode(String id, ProcessingState state, String label) {
    if (id != null) {
      adjacencyList.putIfAbsent(id, new HashSet<>());
      nodeStates.put(id, state != null ? state : ProcessingState.PROCESSED);
      nodeLabels.put(id, label != null ? label : id);
    }
  }

  /*
   * Updates the processing state of an existing node. This is crucial for
   * reflecting intended changes (e.g., marking a node as TO_REMOVE) before
   * evaluating dependencies.
   *
   * @param id The unique identifier of the entity to update.
   * 
   * @param state The new processing state to assign to the entity.
   */
  public void updateNodeState(String id, ProcessingState state) {
    if (id != null && nodeStates.containsKey(id) && state != null) {
      nodeStates.put(id, state);
    }
  }

  /**
   * Adds a dependency edge indicating that the consumer cannot exist without the
   * provider.
   *
   * @param providerId
   *          The entity that provides data/structure (e.g., Source Column).
   * @param dependentId
   *          The entity that consumes the data/structure (e.g., Virtual Column).
   */
  public void addDependencyEdge(String providerId, String dependentId) {
    if (providerId != null && dependentId != null) {
      adjacencyList.computeIfAbsent(providerId, k -> new HashSet<>()).add(dependentId);
    }
  }

  /**
   * Performs a BFS traversal to find all active dependents of a given node.
   * Active dependents are those that are not marked for removal (i.e., their
   * state is not TO_REMOVE).
   * 
   * @param startNodeId
   * @return
   */
  public Set<String> getActiveDependentLabels(String startNodeId) {
    Set<String> activeDependents = new HashSet<>();
    Set<String> visited = new HashSet<>();
    Queue<String> queue = new LinkedList<>();

    if (startNodeId != null) {
      queue.add(startNodeId);
      visited.add(startNodeId);
    }

    while (!queue.isEmpty()) {
      String current = queue.poll();
      Set<String> directDependents = adjacencyList.getOrDefault(current, Collections.emptySet());

      for (String dependent : directDependents) {
        if (!visited.contains(dependent)) {
          visited.add(dependent);
          queue.add(dependent);

          if (nodeStates.getOrDefault(dependent, ProcessingState.PROCESSED) != ProcessingState.TO_REMOVE) {
            activeDependents.add(nodeLabels.getOrDefault(dependent, dependent));
          }
        }
      }
    }

    return activeDependents;
  }
}
