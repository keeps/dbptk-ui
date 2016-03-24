package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerSchema implements Serializable {
  private String name;

  // private String description;

  private List<ViewerTable> tables;

  // private List<DbvView> views;

  // private List<DbvRoutine> routines;

  // private List<DbvComposedType> userDefinedTypes;

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
}
