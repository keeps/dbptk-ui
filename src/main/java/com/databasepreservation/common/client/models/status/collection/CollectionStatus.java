/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import com.databasepreservation.common.client.tools.ViewerCelllUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"version", "id", "solrCollectionPrefix", "databaseUUID", "name", "description",
  "consolidateProperty", "tables", "savedSearches", "denormalizations", "permissions"})
public class CollectionStatus implements Serializable {

  private String version;
  private String databaseUUID;
  private String id;
  private String solrCollectionPrefix;
  private String name;
  private String description;
  private LargeObjectConsolidateProperty consolidateProperty;
  private List<TableStatus> tables;
  private List<SavedSearch> savedSearches;
  private Set<String> denormalizations;

  public CollectionStatus() {
    tables = new ArrayList<>();
    savedSearches = new ArrayList<>();
    denormalizations = new HashSet<>();
    consolidateProperty = LargeObjectConsolidateProperty.CONSOLIDATED;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDatabaseUUID() {
    return databaseUUID;
  }

  public void setDatabaseUUID(String databaseUUID) {
    this.databaseUUID = databaseUUID;
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

  public LargeObjectConsolidateProperty getConsolidateProperty() {
    return consolidateProperty;
  }

  public void setConsolidateProperty(LargeObjectConsolidateProperty consolidateProperty) {
    this.consolidateProperty = consolidateProperty;
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

  public void addDenormalization(String denormalization) {
    this.denormalizations.add(denormalization);
  }

  public void addSavedSearch(SavedSearch savedSearch) {
    this.savedSearches.add(savedSearch);
  }

  @JsonIgnore
  public SavedSearch getSavedSearch(String id) {
    for (SavedSearch savedSearch : savedSearches) {
      if (savedSearch.getUuid().equals(id)) {
        return savedSearch;
      }
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

  public List<String> getFieldsToReturn(String tableId) {
    List<String> fieldsToReturn = new ArrayList<>();
    final List<ColumnStatus> columns = getTableStatusByTableId(tableId).getColumns();
    columns.forEach(column -> {
      if (column.getDetailsStatus().isShow()) {
        fieldsToReturn.add(column.getId());
      }
    });

    return fieldsToReturn;
  }

  @JsonIgnore
  public TableStatus getTableStatus(String uuid) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(uuid))
        return table;
    }

    return null;
  }

  @JsonIgnore
  public TableStatus getTableStatusByTableId(String id) {
    for (TableStatus table : tables) {
      if (table.getId().equals(id))
        return table;
    }

    return null;
  }

  public boolean showTable(String id) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id)) {
        return table.isShow();
      }
    }

    return true;
  }

  public boolean showColumn(String tableId, String columnId) {
    final TableStatus tableStatus = getTableStatus(tableId);
    if (tableStatus != null) {
      for (ColumnStatus column : tableStatus.getColumns()) {
        if (column.getId().equals(columnId)) {
          return column.getSearchStatus().getList().isShow();
        }
      }
    }

    return true;
  }

  public boolean showColumnInDetail(String tableId, String columnId) {
    final TableStatus tableStatus = getTableStatus(tableId);
    if (tableStatus != null) {
      for (ColumnStatus column : tableStatus.getColumns()) {
        if (column.getId().equals(columnId)) {
          return column.getDetailsStatus().isShow();
        }
      }
    }

    return true;
  }

  public ColumnStatus getColumnByTableIdAndColumn(String tableId, String columnId) {
    final TableStatus tableStatus = getTableStatusByTableId(tableId);
    if (tableStatus != null) {
      for (ColumnStatus column : tableStatus.getColumns()) {
        if (column.getId().equals(columnId)) {
          return column;
        }
      }
    }

    return null;
  }

  public ColumnStatus getColumnByTableAndColumn(String tableUUID, String columnId) {
    final TableStatus tableStatus = getTableStatus(tableUUID);
    if (tableStatus != null) {
      for (ColumnStatus column : tableStatus.getColumns()) {
        if (column.getId().equals(columnId)) {
          return column;
        }
      }
    }
    return null;
  }

  public boolean showAdvancedSearch(String tableId, String columnId) {
    final TableStatus tableStatus = getTableStatus(tableId);
    if (tableStatus != null) {
      for (ColumnStatus column : tableStatus.getColumns()) {
        if (column.getId().equals(columnId)) {
          return column.getSearchStatus().getAdvanced().isFixed();
        }
      }
    }

    return true;
  }

  public void updateTableShowCondition(String id, boolean value) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id)) {
        table.setShow(value);
      }
    }
  }

  public void updateTableCustomName(String id, String value) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id)) {
        table.setCustomName(value);
      }
    }
  }

  public void updateTableCustomDescription(String id, String value) {
    for (TableStatus table : tables) {
      if (table.getUuid().equals(id)) {
        table.setCustomDescription(value);
      }
    }
  }

  @JsonIgnore
  public String getFirstTableVisible() {
    for (TableStatus table : tables) {
      if (table.isShow()) {
        return table.getId();
      }
    }

    return null;
  }

  public void updateColumnMimeType(String uuid, int colIndex) {
    ColumnStatus columnStatus = getTableStatus(uuid).getColumnByIndex(colIndex);
    columnStatus.setApplicationType(ViewerCelllUtils.getAutoDetectMimeTypeTemplate());
  }

  public void updateLobFileName(String uuid, int colIndex) {
    ColumnStatus columnStatus = getTableStatus(uuid).getColumnByIndex(colIndex);
    String template = ViewerConstants.SIARD_RECORD_PREFIX + "_" + ViewerCelllUtils.getRowIndexTemplate() + "_"
      + ViewerCelllUtils.getColIndexTemplate() + ViewerCelllUtils.getAutoDetectedExtensionTemplate();
    columnStatus.getExportStatus().getTemplateStatus().setTemplate(template);
  }
}
