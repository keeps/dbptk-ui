/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerCandidateKey;
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

  public void setupChildren() {
    processDirectForeignKeys();
    processInverseForeignKeys();
  }

  private void processDirectForeignKeys() {
    if (table.getForeignKeys() != null) {
      for (ViewerForeignKey fk : table.getForeignKeys()) {
        processDirectForeignKeyIfValid(fk);
      }
    }
  }

  private void processDirectForeignKeyIfValid(ViewerForeignKey fk) {
    ViewerTable referencedTable = metadata.getTable(fk.getReferencedTableUUID());

    if (referencedTable != null && this.searchTop(referencedTable) == null) {
      boolean isMultiValue = this.parentIsMultiValue(this) || !isTargetUnique(fk, referencedTable);
      addChildNode(fk, referencedTable, isMultiValue);
    }
  }

  /**
   * Evaluates if the foreign key target guarantees uniqueness. Virtual keys must
   * explicitly match the target's Primary or Candidate Keys.
   */
  private boolean isTargetUnique(ViewerForeignKey fk, ViewerTable referencedTable) {
    if (!ViewerSourceType.VIRTUAL.equals(fk.getSourceType())) {
      return true;
    }

    if (fk.getReferences() == null || fk.getReferences().isEmpty()) {
      return false;
    }

    List<Integer> referencedIndexes = new ArrayList<>();
    for (ViewerReference ref : fk.getReferences()) {
      referencedIndexes.add(ref.getReferencedColumnIndex());
    }

    if (referencedTable.getPrimaryKey() != null) {
      List<Integer> pkIndexes = referencedTable.getPrimaryKey().getColumnIndexesInViewerTable();
      if (matchesKey(referencedIndexes, pkIndexes)) {
        return true;
      }
    }

    if (referencedTable.getCandidateKeys() != null) {
      for (ViewerCandidateKey candidateKey : referencedTable.getCandidateKeys()) {
        List<Integer> ckIndexes = candidateKey.getColumnIndexesInViewerTable();
        if (matchesKey(referencedIndexes, ckIndexes)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean matchesKey(List<Integer> referencedIndexes, List<Integer> keyIndexes) {
    if (keyIndexes == null || keyIndexes.isEmpty()) {
      return false;
    }

    return keyIndexes.size() == referencedIndexes.size() && keyIndexes.containsAll(referencedIndexes);
  }

  private void processInverseForeignKeys() {
    if (metadata.getSchemas() != null) {
      for (ViewerSchema schema : metadata.getSchemas()) {
        processSchemaTablesForInverseKeys(schema);
      }
    }
  }

  private void processSchemaTablesForInverseKeys(ViewerSchema schema) {
    if (schema.getTables() != null) {
      for (ViewerTable otherTable : schema.getTables()) {
        processOtherTableForeignKeys(otherTable);
      }
    }
  }

  private void processOtherTableForeignKeys(ViewerTable otherTable) {
    if (otherTable.getForeignKeys() != null) {
      for (ViewerForeignKey fk : otherTable.getForeignKeys()) {
        processInverseForeignKeyIfValid(fk, otherTable);
      }
    }
  }

  private void processInverseForeignKeyIfValid(ViewerForeignKey fk, ViewerTable otherTable) {
    if (table.getUuid().equals(fk.getReferencedTableUUID()) && this.searchTop(otherTable) == null) {
      addChildNode(fk, otherTable, true);
    }
  }

  private void addChildNode(ViewerForeignKey fk, ViewerTable targetTable, Boolean multiValue) {
    TableNode childNode = new TableNode(database, targetTable, collectionStatus);
    childNode.uuid = generateUUID(fk, targetTable);
    childNode.multiValue = multiValue;
    childNode.isVirtual = ViewerSourceType.VIRTUAL.equals(fk.getSourceType());
    children.put(fk, childNode);
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
