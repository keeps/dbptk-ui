package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.List;

import com.databasepreservation.common.client.common.search.SavedSearch;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CollectionStatus implements Serializable {

  private String version;
  private String id;
  private String solrCollectionPrefix;
  private String name;
  private String description;
  private List<TableStatus> tables;
  private List<SavedSearch> savedSearches;

  public CollectionStatus() {
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSolrCollectionPrefix() {
    return solrCollectionPrefix;
  }

  public void setSolrCollectionPrefix(String solrCollectionPrefix) {
    this.solrCollectionPrefix = solrCollectionPrefix;
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

  public List<TableStatus> getTables() {
    return tables;
  }

  public void setTables(List<TableStatus> tables) {
    this.tables = tables;
  }

  public List<SavedSearch> getSavedSearches() {
    return savedSearches;
  }

  public void setSavedSearches(List<SavedSearch> savedSearches) {
    this.savedSearches = savedSearches;
  }
}
