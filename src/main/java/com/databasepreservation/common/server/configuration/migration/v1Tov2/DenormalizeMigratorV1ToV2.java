/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.configuration.migration.v1Tov2;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.configuration.migration.VersionMigrator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeMigratorV1ToV2 implements VersionMigrator {

  @Override
  public MigrationType getType() {
    return MigrationType.DENORMALIZE;
  }

  @Override
  public String getSourceVersion() {
    return "1.0.0";
  }

  @Override
  public String getTargetVersion() {
    return ViewerConstants.DENORMALIZATION_STATUS_VERSION;
  }

  @Override
  public ObjectNode migrate(ObjectNode node, ViewerDatabase database) {
    node.put("processingState", ProcessingState.TO_PROCESS.name());

    JsonNode relatedTablesNode = node.get("relatedTables");
    if (relatedTablesNode != null && relatedTablesNode.isArray()) {
      for (JsonNode rtNode : relatedTablesNode) {
        if (rtNode.isObject()) {
          migrateRelatedTablesRecursive((ObjectNode) rtNode);
        }
      }
    }
    return node;
  }

  private void migrateRelatedTablesRecursive(ObjectNode relatedTableNode) {
    JsonNode columnsIncluded = relatedTableNode.get("columnsIncluded");
    if (columnsIncluded != null && columnsIncluded.isArray()) {
      for (JsonNode colNode : columnsIncluded) {
        if (colNode.isObject()) {
          ObjectNode colObj = (ObjectNode) colNode;
          if (!colObj.has("groupId")) {
            colObj.put("groupId", 1);
          }
        }
      }
    }

    JsonNode innerRelatedTables = relatedTableNode.get("relatedTables");
    if (innerRelatedTables != null && innerRelatedTables.isArray()) {
      for (JsonNode innerRtNode : innerRelatedTables) {
        if (innerRtNode.isObject()) {
          migrateRelatedTablesRecursive((ObjectNode) innerRtNode);
        }
      }
    }
  }
}