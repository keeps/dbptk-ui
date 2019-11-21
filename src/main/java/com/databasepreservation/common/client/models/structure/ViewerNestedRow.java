package com.databasepreservation.common.client.models.structure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerNestedRow extends IsIndexed {
  private String UUID;
  private String tableUUID;
  private String tableId;
  private Map<String, ViewerCell> cells;
  private ViewerNestedRow nestedRow;

  public ViewerNestedRow() {
    cells = new LinkedHashMap<>();
  }

  @Override
  public String getUuid() {
    return UUID;
  }

  public void setUuid(String UUID) {
    this.UUID = UUID;
  }

  /**
   * @return Map of solrColumnName to cell value as string
   */
  public Map<String, ViewerCell> getCells() {
    return cells;
  }

  /**
   * @param cells
   *          Map of solrColumnName to value as String
   */
  public void setCells(Map<String, ViewerCell> cells) {
    this.cells = cells;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public String getTableUUID() {
    return tableUUID;
  }

  public void setTableUUID(String tableUUID) {
    this.tableUUID = tableUUID;
  }

  public List<String> getCellValues(List<String> fieldsToReturn) {
    List<String> values = new ArrayList<>();
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_ID);
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_UUID);
    for (String solrColumnName : fieldsToReturn) {
      if (cells.get(solrColumnName) == null) {
        values.add("");
      } else {
        values.add(cells.get(solrColumnName).getValue());
      }
    }

    return values;
  }

  public ViewerNestedRow getNestedRow() {
    return nestedRow;
  }

  public void setNestedRow(ViewerNestedRow nestedRow) {
    this.nestedRow = nestedRow;
  }
}
