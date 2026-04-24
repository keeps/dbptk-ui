/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.configuration.migration.v1Tov2;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.configuration.migration.VersionMigrator;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DatabaseMigratorV1ToV2 implements VersionMigrator {

  @Override
  public MigrationType getType() {
    return MigrationType.DATABASE;
  }

  @Override
  public String getSourceVersion() {
    return "1.0.0";
  }

  @Override
  public String getTargetVersion() {
    return ViewerConstants.DATABASE_STATUS_VERSION;
  }

  @Override
  public ObjectNode migrate(ObjectNode node, ViewerDatabase database) {
    // No structural changes required for DatabaseStatus from V1 to V2
    return node;
  }
}