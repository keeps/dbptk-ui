/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.filter;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class CrossCollectionInnerJoinFilterParameter extends FilterParameter {
  private static final long serialVersionUID = 2618241901313423671L;
  private String rowUUID;
  private String nestedOriginalUUID;
  private String fromIndex;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public CrossCollectionInnerJoinFilterParameter() {
    // do nothing
  }

  public CrossCollectionInnerJoinFilterParameter(
    CrossCollectionInnerJoinFilterParameter crossCollectionInnerJoinFilterParameter) {
    this(crossCollectionInnerJoinFilterParameter.getRowUUID(),
      crossCollectionInnerJoinFilterParameter.getNestedOriginalUUID(),
      crossCollectionInnerJoinFilterParameter.getFromIndex());
  }

  public CrossCollectionInnerJoinFilterParameter(String rowUUID, String nestedTableId, String fromIndex) {
    setRowUUID(rowUUID);
    setNestedOriginalUUID(nestedTableId);
    setFromIndex(fromIndex);
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

  public String getFromIndex() {
    return fromIndex;
  }

  public void setFromIndex(String fromIndex) {
    this.fromIndex = fromIndex;
  }

  @Override
  public String toString() {
    return "CrossCollectionInnerJoinFilterParameter({!join method=crossCollection fromIndex=" + fromIndex
      + " from=nestedOriginalUUID to=uuid }_root_:" + getRowUUID() + " AND nestedUUID:" + getNestedOriginalUUID() + ")";
  }
}
