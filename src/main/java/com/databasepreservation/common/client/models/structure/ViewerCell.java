/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerCell implements Serializable {
  private String value;
  private String mimeType;
  private String fileExtension;

  public ViewerCell() {
  }

  public ViewerCell(String value) {
    this.value = value;
  }

  public ViewerCell(String value, String mimeType, String fileExtension) {
    this.value = value;
    this.mimeType = mimeType;
    this.fileExtension = fileExtension;
  }

  /**
   * Gets the value of this cell as string
   * 
   * @return the value or null if the database cell value was NULL
   */
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }

  @Override
  public String toString() {
    return "ViewerCell{" + "value='" + value + '\'' + ", mimeType='" + mimeType + '\'' + ", fileExtension='"
      + fileExtension + '\'' + '}';
  }
}
