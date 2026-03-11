package com.databasepreservation.common.server.configuration.validation;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.databasepreservation.common.client.models.status.ConfigurationDependencyGraph;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.exceptions.DependencyViolationException;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.configuration.validation.rules.IntegrityRule;
import com.databasepreservation.common.server.configuration.validation.rules.VirtualColumnRule;
import com.databasepreservation.common.server.configuration.validation.rules.VirtualForeignKeyRule;
import com.databasepreservation.common.server.configuration.validation.rules.VirtualTableRule;

/**
 * @author Gabriel Barros
 * 
 *         Validator that applies a set of Integrity Rules over the
 *         configuration graph.
 * 
 */
public class ConfigurationIntegrityValidator {

  private final ConfigurationGraphBuilder graphBuilder;
  private final List<IntegrityRule> validationRules;

  public ConfigurationIntegrityValidator(ConfigurationManager configManager) {
    this.graphBuilder = new ConfigurationGraphBuilder(configManager);

    // Register all integrity rules here.
    this.validationRules = Arrays.asList(new VirtualTableRule(), new VirtualColumnRule(), new VirtualForeignKeyRule());
  }

  public void validateStateTransitions(String databaseUUID, CollectionStatus oldStatus, CollectionStatus newStatus)
    throws DependencyViolationException {

    if (oldStatus == null)
      return;

    // 1. Build the merged Dependency Graph
    ConfigurationDependencyGraph graph = graphBuilder.buildMergedGraph(databaseUUID, oldStatus, newStatus);

    // 2. Evaluate all registered strategies
    Set<String> violations = new LinkedHashSet<>();
    for (IntegrityRule rule : validationRules) {
      rule.evaluate(oldStatus, newStatus, graph, violations);
    }

    // 3. Aggregate results and throw if violations exist
    if (!violations.isEmpty()) {
      String errorMessage = "<br><b>Action required:</b> Please remove or update the following dependencies before proceeding:<br><br>"
        + String.join("<br><br>", violations);
      throw new DependencyViolationException(errorMessage);
    }
  }
}
