package com.databasepreservation.common.client.models.configuration.collection;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CollectionConfiguration implements Serializable {
  private String version;
  private String uuid;
  private String name;
  private String description;
  private List<TableConfiguration> tables;

  public CollectionConfiguration(ViewerDatabase database){
    setUuid(database.getUuid());
    setName(database.getMetadata().getName());
    setDescription(database.getMetadata().getDescription());
    setTables(new ArrayList<>());
  }

  public CollectionConfiguration() {
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
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

  public List<TableConfiguration> getTables() {
    return tables;
  }

  public void setTables(List<TableConfiguration> tables) {
    this.tables = tables;
  }
}
