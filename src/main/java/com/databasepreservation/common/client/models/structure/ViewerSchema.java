package com.databasepreservation.common.client.models.structure;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerSchema implements Serializable {
  private String uuid;

  private String name;

  private String siardName;

  private String description;

  private List<ViewerTable> tables;

  private List<ViewerView> views;

  private List<ViewerRoutine> routines;

  public ViewerSchema() {
    tables = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSiardName() {
    return siardName;
  }

  public void setSiardName(String siardName) {
    this.siardName = siardName;
  }

  public List<ViewerTable> getTables() {
    return tables;
  }

  public void setTables(List<ViewerTable> tables) {
    this.tables = tables;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public List<ViewerView> getViews() {
    return views;
  }

  public void setViews(List<ViewerView> views) {
    this.views = views;
  }

  public List<ViewerRoutine> getRoutines() {
    return routines;
  }

  public void setRoutines(List<ViewerRoutine> routines) {
    this.routines = routines;
  }

  public void setViewsSchemaUUID() {
    for (ViewerView viewerView : views) {
      viewerView.setSchemaUUID(uuid);
      viewerView.setSchemaName(name);
    }
  }

  public ViewerTable getMaterializedTable(final String viewName) {
    for (ViewerTable table : tables) {
      if (table.isMaterializedView() && table.getNameWithoutPrefix().equals(viewName)) {
        return table;
      }
    }
    return null;
  }

  public ViewerTable getCustomViewTable(final String viewName) {
    for (ViewerTable table : tables) {
      if (table.isCustomView() && table.getName().equals(viewName)) {
        return table;
      }
    }
    return null;
  }
}
