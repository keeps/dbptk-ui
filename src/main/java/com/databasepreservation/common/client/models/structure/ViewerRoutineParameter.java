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
