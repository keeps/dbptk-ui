package com.databasepreservation.visualization.client;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SavedSearch implements Serializable, IsIndexed {
  private String uuid;
  private String name;
  private String description;
  private DateTime dateAdded;
  private String tableUUID;
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

  public DateTime getDateAdded() {
    return dateAdded;
  }

  public DateTime getOrCreateDateAdded() {
    if (dateAdded == null) {
      dateAdded = DateTime.now(DateTimeZone.UTC);
    }
    return dateAdded;
  }

  public void setDateAdded(DateTime dateAdded) {
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
}
