/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRow extends IsIndexed {
  @Serial
  private static final long serialVersionUID = 5337660979887460340L;
  private String UUID;
  private String databaseUUID;
  private String tableUUID;
  private String tableId;
  private Map<String, ViewerCell> cells;
  private List<ViewerRow> nestedRowList;
  private String nestedUUID;
  private String nestedTableId;
  private String nestedOriginalUUID;
  private Map<String, ViewerMimeType> colsMimeTypeList;

  private Map<String, ViewerLobStoreType> colsLobTypeList;

  @Override
  public String toString() {
    return "ViewerRow{" +
      "UUID='" + UUID + '\'' +
      ", tableUUID='" + tableUUID + '\'' +
      ", tableId='" + tableId + '\'' +
      ", cells=" + cells +
      ", nestedRowList=" + nestedRowList +
      ", nestedUUID='" + nestedUUID + '\'' +
      ", nestedTableId='" + nestedTableId + '\'' +
      ", nestedOriginalUUID='" + nestedOriginalUUID + '\'' +
      ", colsMimeTypeList=" + colsMimeTypeList +
      ", colsLobTypeList=" + colsLobTypeList +
      '}';
  }

  public ViewerRow() {
    cells = new LinkedHashMap<>();
    nestedRowList = new ArrayList<>();
    colsMimeTypeList = new HashMap<>();
    colsLobTypeList = new HashMap<>();
  }

  @Override
  public String getUuid() {
    return UUID;
  }

  public void setUuid(String UUID) {
    this.UUID = UUID;
  }

  public String getDatabaseUUID() {
    return databaseUUID;
  }

  public void setDatabaseUUID(String databaseUUID) {
    this.databaseUUID = databaseUUID;
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
      // treat nested
      // if (!nestedRowList.isEmpty()) {
      // String template =
      // status.getSearchStatus().getList().getTemplate().getTemplate();
      // nestedRowList.forEach(row -> {
      // if (row.nestedUUID.equals(solrColumnName)) {
      // if (template != null && !template.isEmpty()) {
      // String json = JSOUtils.cellsToJson(row.cells, status.getNestedColumns());
      // String s = JavascriptUtils.compileTemplate(template, json);
      // values.add(s);
      // }
      // }
      // });
      // } else {
      // treat non-nested
      if (cells.get(solrColumnName) == null) {
        values.add("");
      } else {
        values.add(cells.get(solrColumnName).getValue());
      }
    }
    // }

    return values;
  }

  public List<ViewerRow> getNestedRowList() {
    return nestedRowList;
  }

  public void setNestedRowList(List<ViewerRow> nestedRowList) {
    this.nestedRowList = nestedRowList;
  }

  public void addNestedRow(ViewerRow nestedRowList) {
    if (this.nestedRowList == null) {
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

  public String getNestedTableId() {
    return nestedTableId;
  }

  public void setNestedTableId(String nestedTableId) {
    this.nestedTableId = nestedTableId;
  }

  public Map<String, ViewerMimeType> getColsMimeTypeList() {
    return colsMimeTypeList;
  }

  public Map<String, ViewerLobStoreType> getColsLobTypeList() {
    return colsLobTypeList;
  }

  public void setColsMimeTypeList(Map<String, ViewerMimeType> colsMimeTypeList) {
    this.colsMimeTypeList = colsMimeTypeList;
  }

  public void addMimeTypeListEntry(String colName, ViewerMimeType viewerMimeType) {
    this.colsMimeTypeList.put(colName, viewerMimeType);
  }

  public void addLobType(String colName, ViewerLobStoreType lobType) {
    this.colsLobTypeList.put(colName, lobType);
  }

}
