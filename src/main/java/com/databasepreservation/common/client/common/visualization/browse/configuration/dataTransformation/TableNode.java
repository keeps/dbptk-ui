/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
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
 * 
 *         <p>
 *         A TableNode, representing a table that relates to other tables via
 *         denormalizations. It can be both a target node, if it is the target
 *         TableNode of another source TableNode's denormalization, or it can be
 *         a source node for any of its possible denormalization targets. At the
 *         top of a hierarchy of TableNodes is a root node with no source nodes.
 *         </p>
 *
 */
public class TableNode {
  // General properties
  private final ViewerDatabase database;
  private final ViewerMetadata metadata;
  private final ViewerTable table;
  private final CollectionStatus collectionStatus;
  // Source Node properties
  private List<TableNode> possibleTargetTables;

  // Target Node properties
  private String denormalizationUUID;
  /**
   * If this node is a target node for a denormalization, this is that
   * denormalization's relevant foreign key. Null otherwise.
   */
  private ViewerForeignKey foreignKey;
  /**
   * If this node is a target node for a denormalization, this is that
   * denormalization's source node. Null otherwise.
   */
  private TableNode sourceNode;
  /**
   * If this node is a target node for a denormalization, this is that
   * denormalization's reference direction.
   */
  private String sourceDenormalizationDirection;
  private Boolean multiValue = false;
  private Boolean isVirtual = false;


  public TableNode(ViewerDatabase database, ViewerTable table, CollectionStatus collectionStatus) {
    this.possibleTargetTables = new ArrayList<>();
    this.database = database;
    this.metadata = database.getMetadata();
    this.table = table;
    this.collectionStatus = collectionStatus;
  }

  public void setupPossibleTargets(List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables) {
    processDirectForeignKeys(alreadyIncludedSourceRelatedTables);
    processInverseForeignKeys(alreadyIncludedSourceRelatedTables);
  }

  private void processDirectForeignKeys(List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables) {
    if (table.getForeignKeys() != null) {
      for (ViewerForeignKey fk : table.getForeignKeys()) {
        processDirectForeignKeyIfValid(fk, alreadyIncludedSourceRelatedTables);
      }
    }
  }

  private void processDirectForeignKeyIfValid(ViewerForeignKey fk,
    List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables) {
    ViewerTable referencedTable = metadata.getTable(fk.getReferencedTableUUID());

    if (referencedTable != null && this.searchTop(referencedTable) == null) {
      boolean isMultiValue = this.parentIsMultiValue(this) || !isTargetUnique(fk, referencedTable);
      addPossibleTargetTable(fk, alreadyIncludedSourceRelatedTables, referencedTable, isMultiValue,
        ViewerConstants.DENORMALIZATION_DIRECTION_SOURCE_TO_TARGET);
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

  private void processInverseForeignKeys(List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables) {
    if (metadata.getSchemas() != null) {
      for (ViewerSchema schema : metadata.getSchemas()) {
        processSchemaTablesForInverseKeys(schema, alreadyIncludedSourceRelatedTables);
      }
    }
  }

  private void processSchemaTablesForInverseKeys(ViewerSchema schema,
    List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables) {
    if (schema.getTables() != null) {
      for (ViewerTable otherTable : schema.getTables()) {
        processOtherTableForeignKeys(otherTable, alreadyIncludedSourceRelatedTables);
      }
    }
  }

  private void processOtherTableForeignKeys(ViewerTable otherTable,
    List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables) {
    if (otherTable.getForeignKeys() != null) {
      for (ViewerForeignKey fk : otherTable.getForeignKeys()) {
        processInverseForeignKeyIfValid(fk, alreadyIncludedSourceRelatedTables, otherTable);
      }
    }
  }

  private void processInverseForeignKeyIfValid(ViewerForeignKey fk,
    List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables, ViewerTable otherTable) {
    if (table.getUuid().equals(fk.getReferencedTableUUID()) && this.searchTop(otherTable) == null) {
      addPossibleTargetTable(fk, alreadyIncludedSourceRelatedTables, otherTable, true,
        ViewerConstants.DENORMALIZATION_DIRECTION_TARGET_TO_SOURCE);
    }
  }

  private void addPossibleTargetTable(ViewerForeignKey fk,
    List<RelatedTablesConfiguration> alreadyIncludedSourceRelatedTables, ViewerTable targetTable, Boolean multiValue,
    String sourceDenormalizationDirection) {
    TableNode childNode = new TableNode(database, targetTable, collectionStatus);

    RelatedTablesConfiguration relatedTableConfiguration = DataTransformationUtils.getRelatedTableConfiguration(
      alreadyIncludedSourceRelatedTables, targetTable.getUuid(), sourceDenormalizationDirection);
    if (relatedTableConfiguration == null) {
      childNode.denormalizationUUID = UUID.randomUUID().toString();
    } else {
      childNode.denormalizationUUID = relatedTableConfiguration.getUuid();
    }
    childNode.multiValue = multiValue;
    childNode.isVirtual = ViewerSourceType.VIRTUAL.equals(fk.getSourceType());
    childNode.sourceNode = this;
    childNode.foreignKey = fk;
    childNode.sourceDenormalizationDirection = sourceDenormalizationDirection;
    possibleTargetTables.add(childNode);
  }

  public List<TableNode> getPossibleTargetTables() {
    return possibleTargetTables;
  }

  public void setPossibleTargetTables(List<TableNode> possibleTargetTables) {
    this.possibleTargetTables = possibleTargetTables;
  }

  public TableNode searchTop(ViewerTable table) {
    if (this.getSourceNode() == null)
      return null;
    if (this.getSourceNode().table.equals(table))
      return this.getSourceNode();
    return this.getSourceNode().searchTop(table);
  }

  public Boolean parentIsMultiValue(TableNode table) {
    if (table == null)
      return false;
    if (table.multiValue)
      return true;
    return parentIsMultiValue(table.getSourceNode());
  }

  public Boolean getIsVirtual() {
    return isVirtual;
  }

  public TableNode getSourceNode() {
    return sourceNode;
  }

  public ViewerTable getTable() {
    return table;
  }

  public String getSourceDenormalizationDirection() {
    return sourceDenormalizationDirection;
  }

  public void setSourceDenormalizationDirection(String sourceDenormalizationDirection) {
    this.sourceDenormalizationDirection = sourceDenormalizationDirection;
  }

  public String getDenormalizationUUID() {
    return denormalizationUUID;
  }

  public void setDenormalizationUUID(String denormalizationUUID) {
    this.denormalizationUUID = denormalizationUUID;
  }

  public ViewerForeignKey getForeignKey() {
    return foreignKey;
  }

  public Boolean getMultiValue() {
    return multiValue;
  }
}
