package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerColumn implements Serializable {
  private String name;

  private ViewerType type;

  private String defaultValue;

  private Boolean nillable;

  private String description;

  private Boolean isAutoIncrement;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ViewerType getType() {
    return type;
  }

  public void setType(ViewerType type) {
    this.type = type;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public Boolean getNillable() {
    return nillable;
  }

  public void setNillable(Boolean nillable) {
    this.nillable = nillable;
  }

  public Boolean getAutoIncrement() {
    return isAutoIncrement;
  }

  public void setAutoIncrement(Boolean autoIncrement) {
    isAutoIncrement = autoIncrement;
  }

  @Override
  public String toString() {
    return "ViewerColumn{" + "defaultValue='" + defaultValue + '\'' + ", name='" + name + '\'' + ", type=" + type
      + ", nillable=" + nillable + ", description='" + description + '\'' + ", isAutoIncrement=" + isAutoIncrement
      + '}';
  }
}
