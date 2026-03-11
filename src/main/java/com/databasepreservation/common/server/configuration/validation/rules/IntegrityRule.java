package com.databasepreservation.common.server.configuration.validation.rules;

import java.util.Set;

import com.databasepreservation.common.client.models.status.ConfigurationDependencyGraph;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 * 
 *         Contract for a validation rule applied to the configuration
 *         dependency graph.
 */
public interface IntegrityRule {
  /**
   * Evaluates the intended configuration changes against the dependency graph.
   *
   * @param oldStatus
   *          The current configuration state on disk.
   * @param newStatus
   *          The new configuration state requested by the user.
   * @param graph
   *          The pre-built dependency graph holding relations and intended
   *          states.
   * @param violations
   *          A set to accumulate HTML formatted error messages.
   */
  void evaluate(CollectionStatus oldStatus, CollectionStatus newStatus, ConfigurationDependencyGraph graph,
    Set<String> violations);
}
