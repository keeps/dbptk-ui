/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerSchema implements Serializable {
  @Serial
  private static final long serialVersionUID = 7592122945058722126L;
  private String uuid;

  private String name;

  private String description;

  private String folder;

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

  public String getFolder() {
    return folder;
  }

  public void setFolder(String folder) {
    this.folder = folder;
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
