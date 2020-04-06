package com.databasepreservation.common.client.models.configuration.database;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ViewerSiardConfiguration implements Serializable {

  private String location;

  public ViewerSiardConfiguration() {}

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }
}
