package com.databasepreservation.dbviewer.transformers;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.utils.SolrUtils;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;

/**
 * Utility class used to convert a DatabaseStructure (used in Database
 * Preservation Toolkit) to a ViewerStructure (used in Database Viewer)
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ToolkitStructure2ViewerStructure {
  /**
   * Private empty constructor
   */
  private ToolkitStructure2ViewerStructure() {
  }

  /**
   * Deep-convert a DatabaseStructure to a ViewerDatabase
   * 
   * @param structure
   *          the database structure used by Database Preservation Toolkit
   * @return an equivalent database that can be used by Database Viewer
   */
  public static ViewerDatabase getDatabase(DatabaseStructure structure) {
    ViewerDatabase result = new ViewerDatabase();
    result.setUuid(SolrUtils.randomUUID());
    // result.setMetadata(getMetadata(structure));
    return result;
  }

  private static ViewerMetadata getMetadata(DatabaseStructure structure) {
    ViewerMetadata result = new ViewerMetadata();
    result.setName(structure.getName());
    result.setSchemas(getSchemas(structure.getSchemas()));
    return result;
  }

  private static List<ViewerSchema> getSchemas(List<SchemaStructure> schemas) {
    List<ViewerSchema> result = new ArrayList<>();
    for (SchemaStructure schema : schemas) {
      result.add(getSchema(schema));
    }
    return result;
  }

  private static ViewerSchema getSchema(SchemaStructure schema) {
    ViewerSchema result = new ViewerSchema();
    result.setName(schema.getName());
    result.setTables(getTables(schema.getTables()));
    return result;
  }

  private static List<ViewerTable> getTables(List<TableStructure> tables) {
    List<ViewerTable> result = new ArrayList<>();
    for (TableStructure table : tables) {
      result.add(getTable(table));
    }
    return result;
  }

  private static ViewerTable getTable(TableStructure table) {
    ViewerTable result = new ViewerTable();
    result.setUuid(SolrUtils.randomUUID());
    result.setName(table.getName());
    return result;
  }
}
