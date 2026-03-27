/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.core.client.GWT;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TableNode {
  private String uuid;
  private TableNode parentNode;
  private ViewerForeignKey foreignKey;
  private Map<ViewerForeignKey, TableNode> children;
  private ViewerDatabase database;
  private ViewerMetadata metadata;
  private ViewerTable table;
  private Boolean multiValue = false;
  private CollectionStatus collectionStatus;
  private Boolean isVirtual = false;

  public TableNode(ViewerDatabase database, ViewerTable table, CollectionStatus collectionStatus) {
    this.children = new HashMap<>();
    this.database = database;
    this.metadata = database.getMetadata();
    this.table = table;
    this.collectionStatus = collectionStatus;
  }

  /**
   * Adds all tables referenced by foreign keys as children and also adds tables
   * that have references to this
   */
  public void setupChildren() {
    // if this table has reference to another tables
    for (ForeignKeysStatus foreignKeysStatus : collectionStatus.getForeignKeysByTableUUID(table.getUuid())) {
      ViewerForeignKey foreignKey = DataTransformationUtils.convertToViewerForeignKey(foreignKeysStatus,
        collectionStatus, table.getUuid());

      ViewerTable viewerTable = metadata.getTable(foreignKey.getReferencedTableUUID());
      // avoid to add the same table in the same tree path
      if (this.searchTop(viewerTable) == null && viewerTable != null) {
        TableNode childNode = new TableNode(database, viewerTable, collectionStatus);
        childNode.uuid = generateUUID(foreignKey, viewerTable);
        childNode.multiValue = this.parentIsMultiValue(this);
        childNode.isVirtual = foreignKey.getSourceType() != null
          && foreignKey.getSourceType().equals(ViewerSourceType.VIRTUAL);
        children.put(foreignKey, childNode);
      }
    }

    // if this table is referenced by another tables
    for (ViewerSchema schema : metadata.getSchemas()) {
      for (ViewerTable viewerTable : schema.getTables()) {
        for (ForeignKeysStatus foreignKeysStatus : collectionStatus.getForeignKeysByTableUUID(viewerTable.getUuid())) {
          if (foreignKeysStatus.getReferencedTableUUID().equals(table.getUuid())
            && this.searchTop(viewerTable) == null) {

            ViewerForeignKey foreignKey = DataTransformationUtils.convertToViewerForeignKey(foreignKeysStatus,
              collectionStatus, viewerTable.getUuid());
            GWT.log("is referenced : foreignKey: " + foreignKey.getName() + " sourceType: " + foreignKey.getSourceType()
              + " from table: " + viewerTable.getName());
            TableNode childNode = new TableNode(database, viewerTable, collectionStatus);
            childNode.uuid = generateUUID(foreignKey, viewerTable);
            childNode.multiValue = true;
            childNode.isVirtual = foreignKey.getSourceType() != null
              && foreignKey.getSourceType().equals(ViewerSourceType.VIRTUAL);
            children.put(foreignKey, childNode);
          }
        }
      }
    }
  }

  private String generateUUID(ViewerForeignKey foreignKey, ViewerTable viewerTable) {
    StringBuilder uuid = new StringBuilder();
    uuid.append(this.uuid);

    uuid.append(ViewerConstants.API_SEP).append(viewerTable.getUuid());

    for (ViewerReference reference : foreignKey.getReferences()) {
      uuid.append(ViewerConstants.API_SEP).append(reference.getSourceColumnIndex());
    }

    return uuid.toString();
  }

  public void setChildren(Map<ViewerForeignKey, TableNode> children) {
    this.children = children;
  }

  public TableNode searchTop(ViewerTable table) {
    if (this.getParentNode() == null) {
      return null;
    }
    if (this.getParentNode().table.equals(table)) {
      return this.getParentNode();
    }
    return this.getParentNode().searchTop(table);
  }

  public Boolean parentIsMultiValue(TableNode table) {
    if (table == null) {
      return false;
    }
    if (table.multiValue) {
      return true;
    } else {
      return parentIsMultiValue(table.getParentNode());
    }
  }

  public Boolean getIsVirtual() {
    return isVirtual;
  }

  public TableNode getParentNode() {
    return parentNode;
  }

  public void setParentNode(TableNode parentNode, ViewerForeignKey foreignKey) {
    this.parentNode = parentNode;
    this.foreignKey = foreignKey;
  }

  public ViewerTable getTable() {
    return table;
  }

  public Map<ViewerForeignKey, TableNode> getChildren() {
    return children;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public ViewerForeignKey getForeignKey() {
    return foreignKey;
  }

  public Boolean getMultiValue() {
    return multiValue;
  }
}
