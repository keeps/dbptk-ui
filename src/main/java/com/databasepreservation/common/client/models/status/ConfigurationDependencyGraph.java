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

  /**
   * Registers a node in the graph with its current processing state. If the state
   * is null, it defaults to PROCESSED.
   *
   * @param id
   *          The unique identifier of the entity (UUID or ID).
   * @param state
   *          The current processing state of the entity.
   */
  public void addNode(String id, ProcessingState state) {
    if (id != null) {
      adjacencyList.putIfAbsent(id, new HashSet<>());
      nodeStates.put(id, state != null ? state : ProcessingState.PROCESSED);
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
   * Performs a Breadth-First Search (BFS) to find ALL direct and indirect
   * dependents that are still active. * The 'visited' set guarantees that cyclic
   * dependencies will not cause infinite loops.
   *
   * @param startNodeId
   *          The ID of the node being checked for removal or modification.
   * @return A set of IDs representing downstream entities that actively depend on
   *         the start node.
   */
  public Set<String> getActiveDependents(String startNodeId) {
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
        // The visited check prevents infinite loops in cyclic references
        if (!visited.contains(dependent)) {
          visited.add(dependent);
          queue.add(dependent);

          // If the dependent is NOT marked for removal, it acts as an active blocker
          if (nodeStates.getOrDefault(dependent, ProcessingState.PROCESSED) != ProcessingState.TO_REMOVE) {
            activeDependents.add(dependent);
          }
        }
      }
    }

    return activeDependents;
  }
}
