package com.databasepreservation.common.client.models.dbptk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.models.parameters.PreservationParameter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Module implements Serializable {

  private String moduleName;
  private List<PreservationParameter> parameters;

  public Module() {
    parameters = new ArrayList<>();
  }

  public Module(String moduleName) {
    this.moduleName = moduleName;
    this.parameters = new ArrayList<>();
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public List<PreservationParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<PreservationParameter> parameters) {
    this.parameters = parameters;
  }

  public void addPreservationParameter(PreservationParameter parameter) {
    parameters.add(parameter);
  }

  public List<PreservationParameter> getRequiredParameters() {
    return parameters.stream().filter(PreservationParameter::isRequired).collect(Collectors.toList());
  }
}
