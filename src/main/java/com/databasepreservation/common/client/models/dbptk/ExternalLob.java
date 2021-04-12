/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.dbptk;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExternalLob implements Serializable {

  private String pathToColumnList;
  private String basePath;
  private String referenceType;

  public ExternalLob() {
  }

  public ExternalLob(String pathToColumnList, String basePath, String referenceType) {
    this.pathToColumnList = pathToColumnList;
    this.basePath = basePath;
    this.referenceType = referenceType;
  }

  public String getPathToColumnList() {
    return pathToColumnList;
  }

  public void setPathToColumnList(String pathToColumnList) {
    this.pathToColumnList = pathToColumnList;
  }

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public String getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(String referenceType) {
    this.referenceType = referenceType;
  }
}
