package com.databasepreservation.common.client.models.configuration.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"uuid", "id", "schemaFolder", "tableFolder", "name", "customName", "description",
  "customDescription", "show", "columns"})
public class ViewerTableConfiguration implements Serializable {

  private String uuid;
  private String id;
  private String schemaFolder;
  private String tableFolder;
  private String name;
  private String customName;
  private String description;
  private String customDescription;
  private boolean show;
  private List<ViewerColumnConfiguration> columns;

  public ViewerTableConfiguration() {
    columns = new ArrayList<>();
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSchemaFolder() {
    return schemaFolder;
  }

  public void setSchemaFolder(String schemaFolder) {
    this.schemaFolder = schemaFolder;
  }

  public String getTableFolder() {
    return tableFolder;
  }

  public void setTableFolder(String tableFolder) {
    this.tableFolder = tableFolder;
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

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  public List<ViewerColumnConfiguration> getColumns() {
    return columns;
  }

  public void setColumns(List<ViewerColumnConfiguration> columns) {
    this.columns = columns;
  }

  public String getCustomName() {
    return customName;
  }

  public void setCustomName(String customName) {
    this.customName = customName;
  }

  public String getCustomDescription() {
    return customDescription;
  }

  public void setCustomDescription(String customDescription) {
    this.customDescription = customDescription;
  }

  public void addColumnStatus(ViewerColumnConfiguration status) {
    this.columns.add(status);
  }

  @JsonIgnore
  public int getLastColumnOrder() {
    int lastIndex = 0;
    for (ViewerColumnConfiguration column : columns) {
      if (column.getOrder() > lastIndex)
        lastIndex = column.getOrder();
    }
    return lastIndex;
  }

  public void reorderColumns() {
    for (int i = 0; i < columns.size(); i++) {
      ViewerColumnConfiguration column = columns.get(i);
      int currentIndex = i + 1;
      if (column.getOrder() != currentIndex) {
        column.setOrder(currentIndex);
      }
    }
  }

  @JsonIgnore
  public List<ViewerColumnConfiguration> getVisibleColumnsList() {
    return columns.stream().filter(c -> c.getViewerSearchConfiguration().getList().isShow()).sorted().collect(Collectors.toList());
  }

  public boolean showAdvancedSearchOption() {
    return columns.stream().anyMatch(c -> c.getViewerSearchConfiguration().getAdvanced().isFixed());
  }

  @JsonIgnore
  public List<ViewerColumnConfiguration> getBinaryColumns() {
    return getVisibleColumnsList().stream().filter(c -> c.getType().equals(ViewerType.dbTypes.BINARY))
      .collect(Collectors.toList());
  }

  @JsonIgnore
  public ViewerColumnConfiguration getColumnById(String id) {
    return columns.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
  }

  @JsonIgnore
  public ViewerColumnConfiguration getColumnByIndex(int index) {
    return columns.stream().filter(c -> c.getColumnIndex() == index).findFirst().orElse(null);
  }

  @JsonIgnore
  public List<String> getCSVHeaders(List<String> fieldsToReturn, boolean exportDescriptions) {
    List<String> values = new ArrayList<>();
    for (ViewerColumnConfiguration column : columns) {
      if (fieldsToReturn.contains(column.getId())) {
        if (exportDescriptions) {
          if (ViewerStringUtils.isBlank(column.getCustomDescription())) {
            values.add(column.getCustomName());
          } else {
            values.add(column.getCustomName() + ": " + column.getCustomDescription());
          }
        } else {
          values.add(column.getCustomName());
        }
      }
    }
    return values;
  }
}
