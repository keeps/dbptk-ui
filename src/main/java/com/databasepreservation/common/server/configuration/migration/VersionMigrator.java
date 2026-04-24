/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.configuration.migration;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface VersionMigrator {
  enum MigrationType {
    DATABASE, COLLECTION, DENORMALIZE
  }

  MigrationType getType();

  String getSourceVersion();

  String getTargetVersion();

  ObjectNode migrate(ObjectNode node, ViewerDatabase database);
}
