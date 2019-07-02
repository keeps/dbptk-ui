package com.databasepreservation.main.desktop.shared.models;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PreservationParameter implements Serializable {

  private String name = null;
  private String displayName = null;
  private String description = null;
  private boolean required = false;
  private boolean hasArgument = false;

  public PreservationParameter() {
  }

  public PreservationParameter(String name, String displayName, String description, boolean required, boolean hasArgument) {
    this.name = name;
    this.displayName = displayName;
    this.description = description;
    this.required = required;
    this.hasArgument = hasArgument;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public boolean hasArgument() {
    return hasArgument;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public void setHasArgument(boolean hasArgument) {
    this.hasArgument = hasArgument;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PreservationParameter that = (PreservationParameter) o;
    return isRequired() == that.isRequired() &&
        hasArgument() == that.hasArgument() &&
        Objects.equals(getName(), that.getName()) &&
        Objects.equals(getDescription(), that.getDescription());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getDescription(), isRequired(), hasArgument());
  }

  @Override
  public String toString() {
    return "PreservationParameter{" +
        "name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", required=" + required +
        ", hasArgument=" + hasArgument +
        '}';
  }
}
