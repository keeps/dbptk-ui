/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.filter;

import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BlockJoinChildFilterParameter extends FilterParameter {

  private String rowUUID;
  private String parentTableId;
  private String nestedTableId;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public BlockJoinChildFilterParameter() {
    // do nothing
  }

  public BlockJoinChildFilterParameter(BlockJoinChildFilterParameter termsFilterParameter) {
    this(termsFilterParameter.getRowUUID(), termsFilterParameter.getParentTableId(), termsFilterParameter.getNestedTableId());
  }

  public BlockJoinChildFilterParameter(String rowUUID, String parentTableId, String nestedTableId) {
    setRowUUID(rowUUID);
    setParentTableId(parentTableId);
    setNestedTableId(nestedTableId);
  }

  public String getRowUUID() {
    return rowUUID;
  }

  public void setRowUUID(String rowUUID) {
    this.rowUUID = rowUUID;
  }

  public String getParentTableId() {
    return parentTableId;
  }

  public void setParentTableId(String parentTableId) {
    this.parentTableId = parentTableId;
  }

  public String getNestedTableId() {
    return nestedTableId;
  }

  public void setNestedTableId(String nestedTableId) {
    this.nestedTableId = nestedTableId;
  }

  @Override
  public String toString() { return "+nestedTableId:" + nestedTableId +" +{!child of='tableId:" + parentTableId +"' }+uuid:" + rowUUID;
  }
}
