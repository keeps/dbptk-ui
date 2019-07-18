package com.databasepreservation.main.common.shared.ViewerStructure;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerView implements Serializable {
  // mandatory in SIARD2
  private String name;
  // "columns" in SIARD metadata.xml
  private List<ViewerColumn> columns;

  // optional in SIARD2
  private String query;
  private String queryOriginal;
  private String description;

  private String uuid;

  private String schemaUUID;
  private String schemaName;

  public ViewerView() {
  }

  public List<ViewerColumn> getColumns() {
    return columns;
  }

  public void setColumns(List<ViewerColumn> columns) {
    this.columns = columns;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getQueryOriginal() {
    return queryOriginal;
  }

  public void setQueryOriginal(String queryOriginal) {
    this.queryOriginal = queryOriginal;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public String getUUID() {
    return uuid;
  }

  public String getSchemaUUID() {
    return schemaUUID;
  }

  public void setSchemaUUID(String schemaUUID) {
    this.schemaUUID = schemaUUID;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }
}
