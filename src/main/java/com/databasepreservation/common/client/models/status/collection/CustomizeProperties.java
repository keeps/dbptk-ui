/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gwt.dom.client.Style;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"width", "unit"})
public class CustomizeProperties implements Serializable {

  private String width;
  private Style.Unit unit;

  public CustomizeProperties() {
    this.width = "10";
    this.unit = Style.Unit.EM;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public Style.Unit getUnit() {
    return unit;
  }

  public void setUnit(Style.Unit unit) {
    this.unit = unit;
  }

  @Override
  public String toString() {
    return "TableColumnProperties{" + "width='" + width + '\'' + ", unit=" + unit + '}';
  }
}
