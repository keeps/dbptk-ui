package com.databasepreservation.common.client.models.denormalize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public class DenormalizeConfiguration implements Serializable {
    private String tableUUID;
    private List<ColumnsToIncludeConfiguration> columnsToInclude;

    public String getTableUUID() {
      return tableUUID;
    }

    public void setTableUUID(String tableUUID) {
      this.tableUUID = tableUUID;
    }

    public List<ColumnsToIncludeConfiguration> getColumnsToInclude() {
      return columnsToInclude;
    }

    public void setColumnsToInclude(List<ColumnsToIncludeConfiguration> columnsToInclude) {
      this.columnsToInclude = columnsToInclude;
    }

    public List<ColumnsToIncludeConfiguration> getAllTables() {
      List<ColumnsToIncludeConfiguration> tables = new ArrayList<>();
        for(ColumnsToIncludeConfiguration columnToInclude: this.getColumnsToInclude()){
          tables.add(columnToInclude);
        }
      return tables;
    }

    public List<ReferencedConfiguration> getAllRootReferencedColumn(String tableUUID){
      List<ReferencedConfiguration> referencedColumns = new ArrayList<>();
      for(ColumnsToIncludeConfiguration table : this.getAllTables()){
        if(table.getForeignKey().getReferenced().getTableUUID().equals(tableUUID)){
          referencedColumns.add(table.getForeignKey().getReferenced());
        }
      }
      return referencedColumns;
    }

    public boolean checkIfTableIsReferenced(String tableUUID){
      for (ColumnsToIncludeConfiguration table: this.getAllTables()) {
        if(table.getForeignKey().getReferenced().getTableUUID().equals(tableUUID)){
          return true;
        }
      }
      return false;
    }

    public String getTableDirectReferenced(String tableUUID){
      for (ColumnsToIncludeConfiguration table: this.getAllTables()) {
      if (table.getTableUUID().equals(tableUUID)) {
        return table.getForeignKey().getReferenced().getTableUUID();
        }
      }
      return null;
    }

    public long checkNestedLevel(String tableUUID, long level) {
      String referencedTable = getTableDirectReferenced(tableUUID);
      if(referencedTable == null){
        return level;
      }
    level++;
    return checkNestedLevel(referencedTable, level);
  }
}
