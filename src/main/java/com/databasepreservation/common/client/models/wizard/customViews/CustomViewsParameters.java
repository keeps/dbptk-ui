/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.customViews;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomViewsParameters implements Serializable {

  private Map<String, CustomViewsParameter> customViewsParameterMap;

  public CustomViewsParameters() {
    this.customViewsParameterMap = new HashMap<>();
  }

  public CustomViewsParameters(Map<String, CustomViewsParameter> customViewsParameter) {
    this.customViewsParameterMap = customViewsParameter;
  }

  public Map<String, CustomViewsParameter> getCustomViewsParameter() {
    return customViewsParameterMap;
  }

  public void setCustomViewsParameter(Map<String, CustomViewsParameter> customViewsParameter) {
    this.customViewsParameterMap = customViewsParameter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CustomViewsParameters that = (CustomViewsParameters) o;
    return Objects.equals(getCustomViewsParameter(), that.getCustomViewsParameter());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCustomViewsParameter());
  }
}
