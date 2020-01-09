package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.databasepreservation.common.client.common.search.SavedSearch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.fusesource.restygwt.client.Json;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"version", "id", "solrCollectionPrefix", "name", "description", "tables", "savedSearches" , "denormalizations"})
public class CollectionStatus implements Serializable {

  private String version;
  private String id;
  private String solrCollectionPrefix;
  private String name;
  private String description;
  private List<TableStatus> tables;
  private List<SavedSearch> savedSearches;
  private Set<String> denormalizations;

  public CollectionStatus() {
    tables = new ArrayList<>();
    savedSearches = new ArrayList<>();
    denormalizations = new HashSet<>();
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

  public void addTableStatus(TableStatus status) {
    this.tables.add(status);
  }

  public Set<String> getDenormalizations() {
    return denormalizations;
  }

  public void setDenormalizations(Set<String> denormalizations) {
    this.denormalizations = denormalizations;
  }

  public void addDenormalization(String denormalization) { this.denormalizations.add(denormalization);}

  public void addSavedSearch(SavedSearch savedSearch) {
    this.savedSearches.add(savedSearch);
  }

  @JsonIgnore
  public SavedSearch getSavedSearch(String id) {
    for (SavedSearch savedSearch : savedSearches) {
      if (savedSearch.getUuid().equals(id)) return savedSearch;
    }
    return null;
  }

  public void updateSavedSearch(SavedSearch savedSearch) {
    for (SavedSearch element : savedSearches) {
      if (element.getUuid().equals(savedSearch.getUuid())) {
        element.setDescription(savedSearch.getDescription());
        element.setName(savedSearch.getName());
      }
    }
  }

  @JsonIgnore
  public TableStatus getTableStatus(String id) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id))
        return table;
    }

    return null;
  }

  public boolean showTable(String id) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id))
        return !table.isHide();
    }

    return true;
  }

  public void updateTableHidingCondition(String id, boolean value) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id))
        table.setHide(value);
    }
  }

  public void updateTableCustomName(String id, String value) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id))
        table.setCustomName(value);
    }
  }

  public void updateTableCustomDescription(String id, String value) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id))
        table.setCustomDescription(value);
    }
  }
  @JsonIgnore
  public TableStatus getTable(String uuid){
    for (TableStatus table : tables) {
     if(table.getUuid().equals(uuid)) return table;
    }
    return null;
  }
}
