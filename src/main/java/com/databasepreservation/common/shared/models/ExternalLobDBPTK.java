package com.databasepreservation.common.shared.models;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExternalLobDBPTK implements Serializable {

  private String pathToColumnList;
  private String basePath;
  private String referenceType;

  public ExternalLobDBPTK() {
  }

  public ExternalLobDBPTK(String pathToColumnList, String basePath, String referenceType) {
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
