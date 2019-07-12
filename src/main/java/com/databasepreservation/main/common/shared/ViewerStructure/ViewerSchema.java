package com.databasepreservation.main.common.shared.ViewerStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerSchema implements Serializable {
  private String uuid;

  private String name;

  private String description;

  private List<ViewerTable> tables;

  private List<ViewerView> views;
  //private Map<String, ViewerView> views;

  private List<ViewerRoutine> routines;

  // private List<DbvComposedType> userDefinedTypes;

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

  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
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

//  public Map<String, ViewerView> getViews() { return views; }
//
//  public void setViews(List<ViewerView> viewsList) {
//    views = new LinkedHashMap<>();
//    for (ViewerView view: viewsList) {
//      views.put(view.getUUID(), view);
//    }
//  }
//
//  public ViewerView getView(String viewUUID){
//    return views.get(viewUUID);
//  }
}
