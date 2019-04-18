package com.databasepreservation.visualization.shared;

import com.databasepreservation.visualization.shared.ViewerStructure.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SavedSearch extends IsIndexed {
  private String uuid;
  private String name;
  private String description;
  private String dateAdded;
  private String tableUUID;
  private String tableName;
  private String databaseUUID;
  private String searchInfoJson;

  public SavedSearch() {
  }

  public String getDatabaseUUID() {
    return databaseUUID;
  }

  public void setDatabaseUUID(String databaseUUID) {
    this.databaseUUID = databaseUUID;
  }

  public String getDateAdded() {
    return dateAdded;
  }

  public void setDateAdded(String dateAdded) {
    this.dateAdded = dateAdded;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSearchInfoJson() {
    return searchInfoJson;
  }

  public void setSearchInfoJson(String searchInfoJson) {
    this.searchInfoJson = searchInfoJson;
  }

  public String getTableUUID() {
    return tableUUID;
  }

  public void setTableUUID(String tableUUID) {
    this.tableUUID = tableUUID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public String toString() {
    return "SavedSearch{" + "databaseUUID='" + databaseUUID + '\'' + ", uuid='" + uuid + '\'' + ", name='" + name + '\''
      + ", description='" + description + '\'' + ", dateAdded='" + dateAdded + '\'' + ", tableUUID='" + tableUUID + '\''
      + ", tableName='" + tableName + '\'' + ", searchInfoJson='" + searchInfoJson + '\'' + '}';
  }
}
