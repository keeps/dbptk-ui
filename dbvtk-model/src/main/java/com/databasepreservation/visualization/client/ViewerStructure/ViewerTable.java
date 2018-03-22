package com.databasepreservation.visualization.client.ViewerStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

  // private List<CandidateKey> candidateKeys;

  private List<ViewerCheckConstraint> checkConstraints;

  private List<ViewerTrigger> triggers;

  private long countRows;

  private String schemaUUID;

  private String schemaName;

  // private HashMap<String, String> udtAlias;

  public ViewerTable() {
    columns = new ArrayList<>();
  }

  public String getUUID() {
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
}
