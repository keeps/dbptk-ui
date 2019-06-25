package com.databasepreservation.main.common.shared.ViewerStructure;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerColumn implements IsSerializable {
  private String solrName;

  private String displayName;

  private ViewerType type;

  private String defaultValue;

  private Boolean nillable;

  private String description;

  private Boolean isAutoIncrement;

  private int columnIndexInEnclosingTable;

  public ViewerColumn() {
  }

  public String getSolrName() {
    return solrName;
  }

  public void setSolrName(String solrName) {
    this.solrName = solrName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ViewerType getType() {
    return type;
  }

  public void setType(ViewerType type) {
    this.type = type;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public Boolean getNillable() {
    return nillable;
  }

  public void setNillable(Boolean nillable) {
    this.nillable = nillable;
  }

  public Boolean getAutoIncrement() {
    return isAutoIncrement;
  }

  public void setAutoIncrement(Boolean autoIncrement) {
    isAutoIncrement = autoIncrement;
  }

  /**
   * @return true if the column should be sortable when displayed in a UI table
   */
  public boolean sortable() {
    // TODO: add some logic to decide if the column should be sortable
    return true;
  }

  @Override
  public String toString() {
    return "ViewerColumn{" + "defaultValue='" + defaultValue + '\'' + ", displayName='" + displayName + '\'' + ", type="
      + type + ", nillable=" + nillable + ", description='" + description + '\'' + ", isAutoIncrement="
      + isAutoIncrement + '}';
  }

  public int getColumnIndexInEnclosingTable() {
    return columnIndexInEnclosingTable;
  }

  public void setColumnIndexInEnclosingTable(int columnIndexInEnclosingTable) {
    this.columnIndexInEnclosingTable = columnIndexInEnclosingTable;
  }
}
