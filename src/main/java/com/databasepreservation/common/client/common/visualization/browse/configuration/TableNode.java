package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
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

  public TableNode(ViewerDatabase database, ViewerTable table) {
    this.children = new HashMap<>();
    this.database = database;
    this.metadata = database.getMetadata();
    this.table = table;
  }

  /**
   * Adds all tables referenced by foreign keys as children and also adds tables
   * that have references to this
   */
  public void setupChildren() {
    // if this table has reference to another tables
    for (ViewerForeignKey foreignKey : table.getForeignKeys()) {
      ViewerTable viewerTable = metadata.getTable(foreignKey.getReferencedTableUUID());
      // avoid to add the same table in the same tree path
      if (this.searchTop(viewerTable) == null) {
        TableNode childNode = new TableNode(database, viewerTable);
        childNode.uuid = generateUUID(foreignKey, viewerTable);
        children.put(foreignKey, childNode);
      }
    }

    // if this table is referenced by another tables
    for (ViewerSchema schema : metadata.getSchemas()) {
      for (ViewerTable viewerTable : schema.getTables()) {
        for (ViewerForeignKey foreignKey : viewerTable.getForeignKeys()) {
          if (foreignKey.getReferencedTableUUID().equals(table.getUuid()) && this.searchTop(viewerTable) == null) {
            TableNode childNode = new TableNode(database, viewerTable);
            childNode.uuid = generateUUID(foreignKey, viewerTable);
            children.put(foreignKey, childNode);
          }
        }
      }
    }
  }

  private String generateUUID(ViewerForeignKey foreignKey, ViewerTable viewerTable){
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

  public TableNode getChildrenByID(String tableUuid) {
    for (Map.Entry<ViewerForeignKey, TableNode> entry : children.entrySet()) {
      if (entry.getValue().table.getUuid().equals(tableUuid)) {
        return entry.getValue();
      }
    }
    return null;
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
}
