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
import java.util.Objects;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class ViewerMimeType implements Serializable {

  @Serial
  private static final long serialVersionUID = -4141694879328097290L;
  private String mimeType;
  private String fileExtension;

  public ViewerMimeType(String mimeType, String fileExtension) {
    this.mimeType = mimeType;
    this.fileExtension = fileExtension;
  }

  public ViewerMimeType() {
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
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ViewerMimeType that = (ViewerMimeType) o;
    return Objects.equals(mimeType, that.mimeType) && Objects.equals(fileExtension, that.fileExtension);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mimeType, fileExtension);
  }

  @Override
  public String toString() {
    return "ViewerMimeType{" + "mimeType='" + mimeType + '\'' + ", fileExtension='" + fileExtension + '\'' + '}';
  }
}
