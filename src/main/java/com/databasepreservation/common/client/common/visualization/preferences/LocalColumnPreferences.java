package com.databasepreservation.common.client.common.visualization.preferences;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LocalColumnPreferences {
  private Boolean show;
  private String width;
  private String unit;

  public LocalColumnPreferences() {
  }

  public Boolean getShow() {
    return show;
  }

  public void setShow(Boolean show) {
    this.show = show;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }
}
