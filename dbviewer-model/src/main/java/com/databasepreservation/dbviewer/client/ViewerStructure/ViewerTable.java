package com.databasepreservation.dbviewer.client.ViewerStructure;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTable {
  // used to identify the collection containing data from this table
  private String uuid;

  private String name;

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
}
