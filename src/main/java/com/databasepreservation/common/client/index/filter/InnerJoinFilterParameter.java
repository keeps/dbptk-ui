package com.databasepreservation.common.client.index.filter;

import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class InnerJoinFilterParameter extends FilterParameter {
  private static final long serialVersionUID = 2618241901314423671L;
  private String rowUUID;
  private String nestedTableId;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public InnerJoinFilterParameter() {
    // do nothing
  }

  public InnerJoinFilterParameter(InnerJoinFilterParameter innerJoinFilterParameter) {
    this(innerJoinFilterParameter.getRowUUID(), innerJoinFilterParameter.getNestedTableId());
  }

  public InnerJoinFilterParameter(String rowUUID, String nestedTableId) {
    setRowUUID(rowUUID);
    setNestedTableId(nestedTableId);
  }

  public String getRowUUID() {
    return rowUUID;
  }

  public void setRowUUID(String rowUUID) {
    this.rowUUID = rowUUID;
  }

  public String getNestedTableId() {
    return nestedTableId;
  }

  public void setNestedTableId(String nestedTableId) {
    this.nestedTableId = nestedTableId;
  }


  @Override
  public String toString() {
    return "InnerJoinFilterParameter(tableId:" + getNestedTableId() +" AND {!join from=nestedOriginalUUID to=uuid }_root_:" + getRowUUID() + ")";
  }
}
