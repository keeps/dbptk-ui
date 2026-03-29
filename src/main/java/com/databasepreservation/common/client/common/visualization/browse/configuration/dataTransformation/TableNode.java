/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;

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
    if (table.getForeignKeys() != null) {
      for (ViewerForeignKey fk : table.getForeignKeys()) {
        ViewerTable referencedTable = metadata.getTable(fk.getReferencedTableUUID());

        if (referencedTable != null && this.searchTop(referencedTable) == null) {
          TableNode childNode = new TableNode(database, referencedTable, collectionStatus);
          childNode.uuid = generateUUID(fk, referencedTable);
          childNode.multiValue = this.parentIsMultiValue(this);
          childNode.isVirtual = ViewerSourceType.VIRTUAL.equals(fk.getSourceType());
          children.put(fk, childNode);
        }
      }
    }

    for (ViewerSchema schema : metadata.getSchemas()) {
      for (ViewerTable otherTable : schema.getTables()) {
        if (otherTable.getForeignKeys() != null) {
          for (ViewerForeignKey fk : otherTable.getForeignKeys()) {
            if (table.getUuid().equals(fk.getReferencedTableUUID()) && this.searchTop(otherTable) == null) {

              TableNode childNode = new TableNode(database, otherTable, collectionStatus);
              childNode.uuid = generateUUID(fk, otherTable);
              childNode.multiValue = true;
              childNode.isVirtual = ViewerSourceType.VIRTUAL.equals(fk.getSourceType());
              children.put(fk, childNode);
            }
          }
        }
      }
    }
  }

  private String generateUUID(ViewerForeignKey foreignKey, ViewerTable viewerTable) {
    StringBuilder uuidBuilder = new StringBuilder();
    uuidBuilder.append(this.uuid).append(ViewerConstants.API_SEP).append(viewerTable.getUuid());
    if (foreignKey.getReferences() != null) {
      for (ViewerReference reference : foreignKey.getReferences()) {
        uuidBuilder.append(ViewerConstants.API_SEP).append(reference.getSourceColumnIndex());
      }
    }
    return uuidBuilder.toString();
  }

  public void setChildren(Map<ViewerForeignKey, TableNode> children) {
    this.children = children;
  }

  public TableNode searchTop(ViewerTable table) {
    if (this.getParentNode() == null)
      return null;
    if (this.getParentNode().table.equals(table))
      return this.getParentNode();
    return this.getParentNode().searchTop(table);
  }

  public Boolean parentIsMultiValue(TableNode table) {
    if (table == null)
      return false;
    if (table.multiValue)
      return true;
    return parentIsMultiValue(table.getParentNode());
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
