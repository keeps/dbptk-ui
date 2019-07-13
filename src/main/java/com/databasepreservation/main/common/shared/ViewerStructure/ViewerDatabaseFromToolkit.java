package com.databasepreservation.main.common.shared.ViewerStructure;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerDatabaseFromToolkit extends ViewerDatabase {
  private Map<String, ViewerSchema> toolkitSchemas;
  private Map<String, ViewerTable> toolkitTables;
  private Map<String, ViewerView> toolkitViews;
  private Map<String, ViewerRoutine> toolkitRoutines;

  public ViewerDatabaseFromToolkit() {
    super();
    toolkitSchemas = new HashMap<>();
    toolkitTables = new HashMap<>();
    toolkitViews = new HashMap<>();
    toolkitRoutines = new HashMap<>();
  }

  /**
   * @param id
   *          The schema name (used to uniquely identify a schema in DBPTK)
   * @param schema
   *          The corresponding schema to add
   */
  public void putSchema(String id, ViewerSchema schema) {
    toolkitSchemas.put(id, schema);
  }

  /**
   * @param id
   *          The schema name followed by a '.' and the table name (used to
   *          uniquely identify a table in DBPTK)
   * @param table
   *          The corresponding table to add
   */
  public void putTable(String id, ViewerTable table) {
    toolkitTables.put(id, table);
  }

  /**
   * @param id
   *          The schema name followed by a '.' and the view name (used to
   *          uniquely identify a table in DBPTK)
   * @param view
   *          The corresponding view to add
   */
  public void putView(String id, ViewerView view) {
    toolkitViews.put(id, view);
  }

  /**
   * @param id
   *          The schema name followed by a '.' and the routine name (used to
   *          uniquely identify a routine in DBPTK)
   * @param view
   *          The corresponding routine to add
   */
  public void putRoutine(String id, ViewerRoutine routine) {
    toolkitRoutines.put(id, routine);
  }

  /**
   * @param id
   *          The schema name (used to uniquely identify a schema in DBPTK)
   */
  public ViewerSchema getSchema(String id) {
    return toolkitSchemas.get(id);
  }

  /**
   * @param id
   *          The schema name followed by a '.' and the table name (used to
   *          uniquely identify a table in DBPTK)
   */
  public ViewerTable getTable(String id) {
    return toolkitTables.get(id);
  }

  /**
   * @param id
   *          The schema name followed by a '.' and the view name (used to
   *          uniquely identify a view in DBPTK)
   */
  public ViewerView getView(String id) {
    return toolkitViews.get(id);
  }

  /**
   * @param id
   *          The schema name followed by a '.' and the view name (used to
   *          uniquely identify a view in DBPTK)
   */
  public ViewerRoutine getRoutine(String id) {
    return toolkitRoutines.get(id);
  }
}
