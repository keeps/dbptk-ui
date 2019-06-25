package com.databasepreservation.main.common.shared.ViewerStructure;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerView implements IsSerializable {
  // mandatory in SIARD2
  private String name;
  // "columns" in SIARD metadata.xml
  private List<ViewerColumn> columns;

  // optional in SIARD2
  private String query;
  private String queryOriginal;
  private String description;

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
}
