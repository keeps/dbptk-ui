package com.databasepreservation.common.client.models.wizard.customViews;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomViewsParameters implements Serializable {

  private List<CustomViewsParameter> customViewsParameter;

  public CustomViewsParameters() {
  }

  public CustomViewsParameters(List<CustomViewsParameter> customViewsParameter) {
    this.customViewsParameter = customViewsParameter;
  }

  public List<CustomViewsParameter> getCustomViewsParameter() {
    return customViewsParameter;
  }

  public void setCustomViewsParameter(List<CustomViewsParameter> customViewsParameter) {
    this.customViewsParameter = customViewsParameter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CustomViewsParameters that = (CustomViewsParameters) o;
    return Objects.equals(getCustomViewsParameter(), that.getCustomViewsParameter());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCustomViewsParameter());
  }
}
