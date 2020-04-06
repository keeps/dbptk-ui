package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FacetsStatus implements Serializable {

  private String parameter;

  public FacetsStatus() {
  }

  public String getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }
}
