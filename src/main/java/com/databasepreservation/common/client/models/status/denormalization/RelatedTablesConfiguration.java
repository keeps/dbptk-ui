package com.databasepreservation.common.client.models.status.denormalization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.structure.ViewerColumn;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RelatedTablesConfiguration implements Serializable {
  private String uuid;
  private String tableUUID;
  private String tableID;
  private String referencedTableUUID;
  private String referencedTableID;
  private String path;
  private Boolean multiValue = false;
  private List<ReferencesConfiguration> references;
  private List<RelatedColumnConfiguration> columnsIncluded;
  private List<RelatedTablesConfiguration> relatedTables;

  public RelatedTablesConfiguration() {
    references = new ArrayList<>();
    columnsIncluded = new ArrayList<>();
    relatedTables = new ArrayList<>();
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getTableUUID() {
    return tableUUID;
  }

  public void setTableUUID(String tableUUID) {
    this.tableUUID = tableUUID;
  }

  public String getTableID() {
    return tableID;
  }

  public void setTableID(String tableID) {
    this.tableID = tableID;
  }

  public String getReferencedTableUUID() {
    return referencedTableUUID;
  }

  public void setReferencedTableUUID(String referencedTableUUID) {
    this.referencedTableUUID = referencedTableUUID;
  }

  public String getReferencedTableID() {
    return referencedTableID;
  }

  public void setReferencedTableID(String referencedTableID) {
    this.referencedTableID = referencedTableID;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<ReferencesConfiguration> getReferences() {
    return references;
  }

  public void setReferences(List<ReferencesConfiguration> references) {
    this.references = references;
  }

  public List<RelatedColumnConfiguration> getColumnsIncluded() {
    return columnsIncluded;
  }

  public void setColumnsIncluded(List<RelatedColumnConfiguration> columnsIncluded) {
    this.columnsIncluded = columnsIncluded;
  }

  public void addColumnToInclude(ViewerColumn column) {
    columnsIncluded.add(new RelatedColumnConfiguration(column));
  }

  public List<RelatedTablesConfiguration> getRelatedTables() {
    return relatedTables;
  }

  public RelatedTablesConfiguration getRelatedTable(String uuid) {
    for (RelatedTablesConfiguration relatedTable : relatedTables) {
      if (relatedTable.getUuid().equals(uuid)) {
        return relatedTable;
      } else {
        if (!relatedTable.getRelatedTables().isEmpty()) {
          RelatedTablesConfiguration innerRelatedTable = relatedTable.getRelatedTable(uuid);
          if (innerRelatedTable != null)
            return innerRelatedTable; 
        }
      }
    }
    return null;
  }

  public void addRelatedTable(RelatedTablesConfiguration relatedTable){
          relatedTables.add(relatedTable);
      }

  public void setRelatedTables(List<RelatedTablesConfiguration> relatedTables) {
    this.relatedTables = relatedTables;
  }

  public Boolean getMultiValue() {
    return multiValue;
  }

  public void setMultiValue(Boolean multiValue) {
    this.multiValue = multiValue;
  }
}
