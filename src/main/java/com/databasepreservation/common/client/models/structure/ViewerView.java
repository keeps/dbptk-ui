/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerView implements Serializable {
  @Serial
  private static final long serialVersionUID = -5173205690996649126L;
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

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
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

  @JsonIgnore
  public ViewerColumn getColumnByIndexInEnclosing(int index) {
    for (ViewerColumn column : columns) {
      if (column.getColumnIndexInEnclosingTable() == index)
        return column;
    }
    return null;
  }
}
