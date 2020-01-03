package com.databasepreservation.common.client.models.activity.logs;

import java.io.Serializable;

import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.index.ExportRequest;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogWrapper implements Serializable {

  private ActivityLogEntry activityLogEntry;
  private boolean parameters = true;

  private PresenceState databasePresence = PresenceState.NONE;
  private ViewerDatabase database;

  private PresenceState tablePresence = PresenceState.NONE;
  private ViewerTable table;

  private PresenceState columnPresence = PresenceState.NONE;
  private String columnName;

  private PresenceState rowPresence = PresenceState.NONE;
  private ViewerRow row;

  private PresenceState savedSearchPresence = PresenceState.NONE;
  private SavedSearch savedSearch;

  private PresenceState filterPresence = PresenceState.NONE;
  private Filter filter;

  private PresenceState sublistPresence = PresenceState.NONE;
  private Sublist sublist;

  private PresenceState facetsPresence = PresenceState.NONE;
  private Facets facets;

  private PresenceState exportRequestPresence = PresenceState.NONE;
  private ExportRequest exportRequest;

  public ActivityLogWrapper() {
  }

  public ActivityLogWrapper(ActivityLogEntry entry) {
    this.activityLogEntry = entry;
  }

  public ActivityLogEntry getActivityLogEntry() {
    return activityLogEntry;
  }

  public void setActivityLogEntry(ActivityLogEntry activityLogEntry) {
    this.activityLogEntry = activityLogEntry;
  }

  public boolean isParameters() {
    return parameters;
  }

  public void setParameters(boolean parameters) {
    this.parameters = parameters;
  }

  public PresenceState getDatabasePresence() {
    return databasePresence;
  }

  public void setDatabasePresence(PresenceState databasePresence) {
    this.databasePresence = databasePresence;
  }

  public ViewerDatabase getDatabase() {
    return database;
  }

  public void setDatabase(ViewerDatabase database) {
    this.database = database;
  }

  public PresenceState getTablePresence() {
    return tablePresence;
  }

  public void setTablePresence(PresenceState tablePresence) {
    this.tablePresence = tablePresence;
  }

  public ViewerTable getTable() {
    return table;
  }

  public void setTable(ViewerTable table) {
    this.table = table;
  }

  public PresenceState getFilterPresence() {
    return filterPresence;
  }

  public void setFilterPresence(PresenceState filterPresence) {
    this.filterPresence = filterPresence;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public PresenceState getSublistPresence() {
    return sublistPresence;
  }

  public void setSublistPresence(PresenceState sublistPresence) {
    this.sublistPresence = sublistPresence;
  }

  public Sublist getSublist() {
    return sublist;
  }

  public void setSublist(Sublist sublist) {
    this.sublist = sublist;
  }

  public PresenceState getFacetsPresence() {
    return facetsPresence;
  }

  public void setFacetsPresence(PresenceState facetsPresence) {
    this.facetsPresence = facetsPresence;
  }

  public Facets getFacets() {
    return facets;
  }

  public void setFacets(Facets facets) {
    this.facets = facets;
  }

  public PresenceState getExportRequestPresence() {
    return exportRequestPresence;
  }

  public void setExportRequestPresence(PresenceState exportRequestPresence) {
    this.exportRequestPresence = exportRequestPresence;
  }

  public ExportRequest getExportRequest() {
    return exportRequest;
  }

  public void setExportRequest(ExportRequest exportRequest) {
    this.exportRequest = exportRequest;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public PresenceState getColumnPresence() {
    return columnPresence;
  }

  public void setColumnPresence(PresenceState columnPresence) {
    this.columnPresence = columnPresence;
  }

  public PresenceState getRowPresence() {
    return rowPresence;
  }

  public void setRowPresence(PresenceState rowPresence) {
    this.rowPresence = rowPresence;
  }

  public ViewerRow getRow() {
    return row;
  }

  public void setRow(ViewerRow row) {
    this.row = row;
  }

  public PresenceState getSavedSearchPresence() {
    return savedSearchPresence;
  }

  public void setSavedSearchPresence(PresenceState savedSearchPresence) {
    this.savedSearchPresence = savedSearchPresence;
  }

  public SavedSearch getSavedSearch() {
    return savedSearch;
  }

  public void setSavedSearch(SavedSearch savedSearch) {
    this.savedSearch = savedSearch;
  }
}
