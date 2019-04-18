package com.databasepreservation.visualization.shared.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRoutineParameter implements Serializable {
  // mandatory in SIARD2
  private String name;
  private String mode;

  // optional in SIARD2
  private ViewerType type;
  private String description;

  public ViewerRoutineParameter() {

  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ViewerType getType() {
    return type;
  }

  public void setType(ViewerType type) {
    this.type = type;
  }
}
