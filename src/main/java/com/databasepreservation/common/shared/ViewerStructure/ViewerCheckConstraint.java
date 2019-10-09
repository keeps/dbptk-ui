package com.databasepreservation.common.shared.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerCheckConstraint implements Serializable {
  // mandatory in SIARD2
  private String name;
  private String condition;

  // optional in SIARD2
  private String description;

  public ViewerCheckConstraint() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
