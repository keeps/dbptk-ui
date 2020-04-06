package com.databasepreservation.common.client.models.status.database;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SiardStatus implements Serializable {

  private String location;

  public SiardStatus() {}

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }
}
