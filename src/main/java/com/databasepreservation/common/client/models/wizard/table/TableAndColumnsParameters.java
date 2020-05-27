package com.databasepreservation.common.client.models.wizard.table;

import com.google.gwt.core.client.GWT;
import io.swagger.models.auth.In;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumnsParameters implements Serializable {

  private Map<String, TableAndColumnsParameter> tableAndColumnsParameterMap;
  private Map<String, ViewAndColumnsParameter> viewAndColumnsParameterMap;
  private List<String> selectedSchemas;
  private boolean externalLobConfigurationSet;
  private Integer dbptkFilterIndex;

  public TableAndColumnsParameters() {
    this.tableAndColumnsParameterMap = new LinkedHashMap<>();
    this.viewAndColumnsParameterMap = new LinkedHashMap<>();
    this.selectedSchemas = new ArrayList<>();
    this.externalLobConfigurationSet = false;
  }

  public TableAndColumnsParameters(Map<String, TableAndColumnsParameter> tableAndColumnsParameterMap,
    List<String> selectedSchemas) {
    this.tableAndColumnsParameterMap = tableAndColumnsParameterMap;
    this.selectedSchemas = selectedSchemas;
  }

  public Map<String, TableAndColumnsParameter> getTableAndColumnsParameterMap() {
    return tableAndColumnsParameterMap;
  }

  public void setTableAndColumnsParameterMap(Map<String, TableAndColumnsParameter> tableAndColumnsParameterMap) {
    this.tableAndColumnsParameterMap = tableAndColumnsParameterMap;
  }

  public Map<String, ViewAndColumnsParameter> getViewAndColumnsParameterMap() {
    return viewAndColumnsParameterMap;
  }

  public void setViewAndColumnsParameterMap(Map<String, ViewAndColumnsParameter> viewAndColumnsParameterMap) {
    this.viewAndColumnsParameterMap = viewAndColumnsParameterMap;
  }

  public List<String> getSelectedSchemas() {
    return selectedSchemas.stream().distinct().collect(Collectors.toList());
  }

  public void setSelectedSchemas(List<String> schemas) {
    this.selectedSchemas = schemas;
  }

  public boolean isSelectionEmpty() {
    return tableAndColumnsParameterMap.isEmpty() && viewAndColumnsParameterMap.isEmpty();
  }

  public boolean isExternalLobConfigurationSet() {
    return externalLobConfigurationSet;
  }

  public void setExternalLobConfigurationSet(boolean externalLobConfigurationSet) {
    this.externalLobConfigurationSet = externalLobConfigurationSet;
  }

  public Integer getDbptkFilterIndex() {
    return dbptkFilterIndex;
  }

  public void setDbptkFilterIndex(Integer dbptkFilterIndex) {
    this.dbptkFilterIndex = dbptkFilterIndex;
  }
}
