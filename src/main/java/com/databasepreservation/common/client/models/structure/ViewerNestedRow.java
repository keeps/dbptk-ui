/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerNestedRow extends ViewerRow {
  @Serial
  private static final long serialVersionUID = -60735036813821807L;
  private String nestedUUID;
  private String nestedTableId;
  private String nestedOriginalUUID;

  public ViewerNestedRow() {
    super();
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
}
