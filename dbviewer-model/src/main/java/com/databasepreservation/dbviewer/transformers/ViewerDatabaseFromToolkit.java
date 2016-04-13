package com.databasepreservation.dbviewer.transformers;

import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerDatabaseFromToolkit extends ViewerDatabase {
  private Map<String, ViewerSchema> toolkitSchemas;
  private Map<String, ViewerTable> toolkitTables;

  public ViewerDatabaseFromToolkit(){
    super();
    toolkitSchemas = new HashMap<>();
    toolkitTables = new HashMap<>();
  }

  public void putSchema(String id, ViewerSchema schema){
    toolkitSchemas.put(id, schema);
  }

  public void putTable(String id, ViewerTable table){
    toolkitTables.put(id, table);
  }

  public ViewerSchema getSchema(String id){
    return toolkitSchemas.get(id);
  }

  public ViewerTable getTable(String id){
    return toolkitTables.get(id);
  }
}
