package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerType implements Serializable {
  public enum dbTypes {
    BINARY, BOOLEAN, DATETIME, ENUMERATION, INTERVAL, NUMERIC_FLOATING_POINT, NUMERIC_INTEGER, STRING, COMPOSED_STRUCTURE,
    COMPOSED_ARRAY
  }

  private String originalTypeName;
  private String description;
  private String typeName;
  private dbTypes dbType;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getOriginalTypeName() {
    return originalTypeName;
  }

  public void setOriginalTypeName(String originalTypeName) {
    this.originalTypeName = originalTypeName;
  }

  public dbTypes getDbType() {
    return dbType;
  }

  public void setDbType(dbTypes dbType) {
    this.dbType = dbType;
  }

  @Override
  public String toString() {
    return "ViewerType{" + "dbType=" + dbType + ", originalTypeName='" + originalTypeName + '\'' + ", description='"
      + description + '\'' + ", typeName='" + typeName + '\'' + '}';
  }
}
