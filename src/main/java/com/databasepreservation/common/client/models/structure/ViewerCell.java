/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerCell implements Serializable {
  @Serial
  private static final long serialVersionUID = -5345114836590659380L;
  private String value;
  private List<String> listValue;
  private String mimeType;
  private String fileExtension;
  private ViewerLobStoreType storeType;

  public ViewerCell() {
  }

  public ViewerCell(String value) {
    this.value = value;
  }

  public ViewerCell(String value, ViewerLobStoreType storeType) {
    this.value = value;
    this.storeType = storeType;
  }

  public ViewerCell(String value, String mimeType, String fileExtension, ViewerLobStoreType storeType) {
    this.value = value;
    this.mimeType = mimeType;
    this.fileExtension = fileExtension;
    this.storeType = storeType;
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

  /**
   * Gets the value of this cell as list of strings
   *
   * @return the value or null if the database cell value was NULL
   */
  public List<String> getListValue() {
    return listValue;
  }

  public void setListValue(List<String> listValue) {
    this.listValue = listValue;
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

  public ViewerLobStoreType getStoreType() {
    return storeType;
  }

  public void setStoreType(ViewerLobStoreType storeType) {
    this.storeType = storeType;
  }

  @Override
  public String toString() {
    return "ViewerCell{" + "value='" + value + '\'' + ", mimeType='" + mimeType + '\'' + ", fileExtension='"
      + fileExtension + '\'' + ", lobType=" + storeType + '}';
  }
}
