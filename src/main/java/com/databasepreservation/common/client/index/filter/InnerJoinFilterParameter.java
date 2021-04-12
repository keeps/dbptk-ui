/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.filter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class InnerJoinFilterParameter extends FilterParameter {
  private static final long serialVersionUID = 2618241901314423671L;
  private String rowUUID;
  private String nestedOriginalUUID;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public InnerJoinFilterParameter() {
    // do nothing
  }

  public InnerJoinFilterParameter(InnerJoinFilterParameter innerJoinFilterParameter) {
    this(innerJoinFilterParameter.getRowUUID(), innerJoinFilterParameter.getNestedOriginalUUID());
  }

  public InnerJoinFilterParameter(String rowUUID, String nestedTableId) {
    setRowUUID(rowUUID);
    setNestedOriginalUUID(nestedTableId);
  }

  public String getRowUUID() {
    return rowUUID;
  }

  public void setRowUUID(String rowUUID) {
    this.rowUUID = rowUUID;
  }

  public String getNestedOriginalUUID() {
    return nestedOriginalUUID;
  }

  public void setNestedOriginalUUID(String nestedOriginalUUID) {
    this.nestedOriginalUUID = nestedOriginalUUID;
  }


  @Override
  public String toString() {
    return "InnerJoinFilterParameter({!join from=nestedOriginalUUID to=uuid }_root_:" + getRowUUID() + " AND nestedUUID:" + getNestedOriginalUUID() +")";
  }
}
