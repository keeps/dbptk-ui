package com.databasepreservation.common.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomViewsParameters implements Serializable {

  private ArrayList<CustomViewsParameter> customViewsParameter;

  public CustomViewsParameters() {
  }

  public CustomViewsParameters(ArrayList<CustomViewsParameter> customViewsParameter) {
    this.customViewsParameter = customViewsParameter;
  }

  public ArrayList<CustomViewsParameter> getCustomViewsParameter() {
    return customViewsParameter;
  }

  public void setCustomViewsParameter(ArrayList<CustomViewsParameter> customViewsParameter) {
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
