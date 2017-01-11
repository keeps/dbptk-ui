package com.databasepreservation.visualization.client.ViewerStructure;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRow implements Serializable, IsIndexed {
  private String UUID;
  private Map<String, ViewerCell> cells;

  public ViewerRow() {
    cells = new HashMap<>();
  }

  @Override
  public String getUUID() {
    return UUID;
  }

  public void setUUID(String UUID) {
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

  @Override
  public String getId() {
    return getUUID();
  }

  @Override
  public List<String> toCsvHeaders() {
    return Collections.emptyList();
  }

  @Override
  public List<Object> toCsvValues() {
    return Collections.emptyList();
  }

  @Override
  public List<String> liteFields() {
    return Collections.emptyList();
  }
}
