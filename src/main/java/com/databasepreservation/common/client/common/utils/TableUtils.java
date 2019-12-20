package com.databasepreservation.common.client.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TableUtils {

  /**
   * Relationship can be one to one, one to many, many to many and recursive
   * @param tableA
   * @param tableB
   * @return Map with relationship of table A with table B
   */
  public static Map<String, List<ViewerForeignKey>> getRelationship(ViewerTable tableA, ViewerTable tableB) {
    Map<String, List<ViewerForeignKey>> relationShip = new HashMap<>();

    // check if table A has reference to table B
    relationShip.put(tableA.getUUID(), checkForeignKey(tableA, tableB));

    // check if table B has reference to table A
    relationShip.put(tableB.getUUID(), checkForeignKey(tableB, tableA));

    return relationShip;
  }

  /**
   *
   * @param tableA
   * @param tableB
   * @return
   */
  public static List<ViewerForeignKey> checkForeignKey(ViewerTable tableA, ViewerTable tableB) {
    List<ViewerForeignKey> foreignKeyList = new ArrayList<>();
    for (ViewerForeignKey foreignKey : tableA.getForeignKeys()) {
      if (!foreignKey.getReferencedTableUUID().equals(tableB.getUUID())) {
        // this foreign is not related with tableB. check next
        continue;
      }
      foreignKeyList.add(foreignKey);
    }
    return foreignKeyList;
  }

  /**
   * Scans all schemas and checks if table is referenced or appears in other
   * tables
   *
   * @param table
   * @return a List of relation Tables
   */
  public static List<ViewerTable> getListOfRelatedTable(ViewerMetadata metadata, ViewerTable table){
    List<ViewerTable> relatedTables = new ArrayList<>();

    for (ViewerForeignKey foreignKey : table.getForeignKeys()) {
      relatedTables.add(metadata.getTable(foreignKey.getReferencedTableUUID()));
    }

    for (ViewerSchema schema : metadata.getSchemas()) {
      for (ViewerTable schemaTable : schema.getTables()) {
        for (ViewerForeignKey foreignKey : schemaTable.getForeignKeys()) {
          if(foreignKey.getReferencedTableUUID().equals(table.getUUID())){
            relatedTables.add(schemaTable);
          }
        }
      }
    }

    return relatedTables;
  }
}
