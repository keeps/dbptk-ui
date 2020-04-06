package com.databasepreservation.common.client.models.configuration.collection;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ViewerFacetsConfiguration implements Serializable {

  private String parameter;

  public ViewerFacetsConfiguration() {
  }

  public String getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }
}
