/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.configuration.migration.v1Tov2;

import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.configuration.migration.VersionMigrator;
import com.databasepreservation.common.utils.StatusUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CollectionMigratorV1ToV2 implements VersionMigrator {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public MigrationType getType() {
    return MigrationType.COLLECTION;
  }

  @Override
  public String getSourceVersion() {
    return "1.0.0";
  }

  @Override
  public String getTargetVersion() {
    return ViewerConstants.COLLECTION_STATUS_VERSION;
  }

  @Override
  public ObjectNode migrate(ObjectNode node, ViewerDatabase database) {
    node.put("needsToBeProcessed", hasDenormalizations(node));

    JsonNode tablesNode = node.get("tables");
    if (tablesNode != null && tablesNode.isArray()) {
      for (JsonNode tableNode : tablesNode) {
        if (tableNode.isObject()) {
          migrateTable((ObjectNode) tableNode, database);
        }
      }
    }
    return node;
  }

  private boolean hasDenormalizations(ObjectNode node) {
    JsonNode denormalizationsNode = node.get("denormalizations");
    return denormalizationsNode != null && denormalizationsNode.isArray() && !denormalizationsNode.isEmpty();
  }

  private void migrateTable(ObjectNode tableObj, ViewerDatabase database) {
    tableObj.put("sourceType", ViewerSourceType.NATIVE.name());

    populateForeignKeys(tableObj, database);

    JsonNode columnsNode = tableObj.get("columns");
    if (columnsNode != null && columnsNode.isArray()) {
      for (JsonNode colNode : columnsNode) {
        if (colNode.isObject()) {
          migrateColumn((ObjectNode) colNode);
        }
      }
    }
  }

  private void populateForeignKeys(ObjectNode tableObj, ViewerDatabase database) {
    boolean foreignKeysPopulated = false;

    if (database != null && database.getMetadata() != null && tableObj.has("uuid")) {
      ViewerMetadata metadata = database.getMetadata();
      ViewerTable viewerTable = metadata.getTable(tableObj.get("uuid").asText());

      if (viewerTable != null) {
        TableStatus generatedStatus = StatusUtils.getTableStatus(metadata, viewerTable);
        List<ForeignKeysStatus> fks = generatedStatus.getForeignKeys();
        tableObj.set("foreignKeys", mapper.valueToTree(fks));
        foreignKeysPopulated = true;
      }
    }

    // If it was not possible to populate with metadata, ensure the array exists
    if (!foreignKeysPopulated) {
      if (!tableObj.has("foreignKeys") || tableObj.get("foreignKeys").isNull()) {
        tableObj.putArray("foreignKeys");
      }
    }
  }

  private void migrateColumn(ObjectNode colObj) {
    JsonNode nestedColumnsNode = colObj.get("nestedColumns");

    if (nestedColumnsNode != null && nestedColumnsNode.isObject()) {
      migrateNestedColumnPathAndId(colObj, (ObjectNode) nestedColumnsNode);
    } else {
      colObj.put("sourceType", ViewerSourceType.NATIVE.name());
    }
  }

  private void migrateNestedColumnPathAndId(ObjectNode colObj, ObjectNode nestedObj) {
    // 1. Correct the Path formatting
    JsonNode pathNode = nestedObj.get("path");
    if (pathNode != null && pathNode.isTextual()) {
      String newPath = pathNode.asText();

      // Remove the outer span wrapper
      String prefixToRemove = "<span class=\"table-ref-link\">";
      if (newPath.startsWith(prefixToRemove)) {
        newPath = newPath.substring(prefixToRemove.length());
      }

      // Cut the string right before the last span that contains the <b> element
      String suffixToCut = "<span class=\"table-ref-path\"><b>";
      int cutIndex = newPath.indexOf(suffixToCut);
      if (cutIndex > -1) {
        newPath = newPath.substring(0, cutIndex);
      }

      nestedObj.put("path", newPath);
    }

    // 2. Append "_1" to the ID to indicate it belongs to the first group
    JsonNode idNode = colObj.get("id");
    if (idNode != null) {
      String oldId = idNode.asText();
      if (!oldId.endsWith("_1")) {
        colObj.put("id", oldId + "_1");
      }
    }
  }
}