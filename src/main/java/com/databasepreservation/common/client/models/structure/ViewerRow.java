package com.databasepreservation.common.client.models.structure;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IsIndexed;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRow extends IsIndexed {
  private String UUID;
  private String tableUUID;
  private String tableId;
  private Map<String, ViewerCell> cells;
  private List<ViewerRow> nestedRowList;
  private String nestedUUID;
  private String nestedOriginalUUID;

  public ViewerRow() {
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

  public List<ViewerRow> getNestedRowList() {
    return nestedRowList;
  }

  public void setNestedRowList(List<ViewerRow> nestedRowList) {
    this.nestedRowList = nestedRowList;
  }

  public void addNestedRow(ViewerRow nestedRowList) {
    if(this.nestedRowList == null) {
      this.nestedRowList = new ArrayList<>();
    }
    this.nestedRowList.add(nestedRowList);
  }

  public String getNestedUUID() {
    return nestedUUID;
  }

  public void setNestedUUID(String nestedUUID) {
    this.nestedUUID = nestedUUID;
  }

  public String getNestedOriginalUUID() {
    return nestedOriginalUUID;
  }

  public void setNestedOriginalUUID(String nestedOriginalUUID) {
    this.nestedOriginalUUID = nestedOriginalUUID;
  }
}
