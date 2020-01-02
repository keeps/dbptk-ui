package com.databasepreservation.common.client.models.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTable implements Serializable {
  // used to identify the collection containing data from this table
  private String uuid;

  private String id;

  private String name;

  private String description;

  private List<ViewerColumn> columns;

  private ViewerPrimaryKey primaryKey;

  private List<ViewerForeignKey> foreignKeys;

  private List<ViewerCandidateKey> candidateKeys;

  private List<ViewerCheckConstraint> checkConstraints;

  private List<ViewerTrigger> triggers;

  private long countRows;

  private String schemaUUID;

  private String schemaName;

  private String nameWithoutPrefix;

  private boolean customView;

  private boolean materializedView;

  // private HashMap<String, String> udtAlias;

  public ViewerTable() {
    columns = new ArrayList<>();
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<ViewerColumn> getColumns() {
    return columns;
  }

  public void setColumns(List<ViewerColumn> columns) {
    this.columns = columns;
  }

  public long getCountRows() {
    return countRows;
  }

  public void setCountRows(long countRows) {
    this.countRows = countRows;
  }

  public String getSchemaUUID() {
    return schemaUUID;
  }

  public void setSchemaUUID(String schemaUUID) {
    this.schemaUUID = schemaUUID;
  }

  public List<ViewerTrigger> getTriggers() {
    return triggers;
  }

  public void setTriggers(List<ViewerTrigger> triggers) {
    this.triggers = triggers;
  }

  public ViewerPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(ViewerPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public List<ViewerForeignKey> getForeignKeys() {
    return foreignKeys;
  }

  public void setForeignKeys(List<ViewerForeignKey> foreignKeys) {
    this.foreignKeys = foreignKeys;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public List<ViewerCheckConstraint> getCheckConstraints() {
    return checkConstraints;
  }

  public void setCheckConstraints(List<ViewerCheckConstraint> checkConstraints) {
    this.checkConstraints = checkConstraints;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setCandidateKeys(List<ViewerCandidateKey> candidateKeys) {
    this.candidateKeys = candidateKeys;
  }

  public List<ViewerCandidateKey> getCandidateKeys() {
    return candidateKeys;
  }

  public String getNameWithoutPrefix() {
    return nameWithoutPrefix;
  }

  public void setNameWithoutPrefix(String nameWithoutPrefix) {
    this.nameWithoutPrefix = nameWithoutPrefix;
  }

  public boolean isCustomView() {
    return customView;
  }

  public void setCustomView(boolean customView) {
    this.customView = customView;
  }

  public boolean isMaterializedView() {
    return materializedView;
  }

  public void setMaterializedView(boolean materializedView) {
    this.materializedView = materializedView;
  }

  @JsonIgnore
  public List<String> getCSVHeaders(List<String> fieldsToReturn, boolean exportDescriptions) {
    List<String> values = new ArrayList<>();
    for (ViewerColumn column : columns) {
      if (fieldsToReturn.contains(column.getSolrName())) {
        if (exportDescriptions) {
          if (ViewerStringUtils.isBlank(column.getDescription())) {
            values.add(column.getDisplayName());
          } else {
            values.add(column.getDisplayName() + ": " + column.getDescription());
          }
        } else {
          values.add(column.getDisplayName());
        }
      }
    }
    return values;
  }

  @JsonIgnore
  public List<ViewerColumn> getBinaryColumns() {
    List<ViewerColumn> binaryColumns = new ArrayList<>();
    for (ViewerColumn column : columns) {
      if (column.getType().getDbType().equals(ViewerType.dbTypes.BINARY)) {
        binaryColumns.add(column);
      }
    }

    return binaryColumns;
  }

  @JsonIgnore
  public ViewerColumn getColumnByIndexInEnclosingTable(int index) {
    for (ViewerColumn column : columns) {
      if (column.getColumnIndexInEnclosingTable() == index)
        return column;
    }
    return null;
  }

  public boolean containsBinaryColumns() {
    for (ViewerColumn column : columns) {
      if (column.getType().getDbType().equals(ViewerType.dbTypes.BINARY)) {
        return true;
      }
    }
    return false;
  }
}
