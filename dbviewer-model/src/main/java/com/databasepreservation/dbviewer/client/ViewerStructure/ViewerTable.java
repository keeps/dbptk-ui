package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTable implements Serializable {
  // used to identify the collection containing data from this table
  private String uuid;

  private String name;

  private String description;

  private List<ViewerColumn> columns;
  //
  // private PrimaryKey primaryKey;
  //
  // private List<ForeignKey> foreignKeys;
  //
  // private List<CandidateKey> candidateKeys;
  //
  // private List<CheckConstraint> checkConstraints;
  //
  // private List<Trigger> triggers;

  private long countRows;

  private String schema;

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

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }
}
