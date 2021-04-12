/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.activity.logs;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class LogEntryParameter implements Serializable {

  private String name = null;
  private String value = null;

  public LogEntryParameter() {
    // do nothing
  }

  public LogEntryParameter(String name, String value) {
    setName(name);
    setValue(value);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    LogEntryParameter that = (LogEntryParameter) o;
    return Objects.equals(getName(), that.getName()) && Objects.equals(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getValue());
  }

  @Override
  public String toString() {
    return "LogEntryParameter (" + getName() + ", " + getValue() + ")";
  }
}
