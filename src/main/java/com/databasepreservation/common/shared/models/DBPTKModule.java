package com.databasepreservation.common.shared.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DBPTKModule implements Serializable {

  private Map<String, List<PreservationParameter>> parameters;

  public DBPTKModule() {
    this.parameters = new HashMap<>();
  }

  public void setParameters(Map<String, List<PreservationParameter>> parameters) {
    this.parameters = parameters;
  }

  public Map<String, List<PreservationParameter>> getParameters() {
    return this.parameters;
  }

  public List<PreservationParameter> getParameters(String key) {
    return getParameters().get(key);
  }

  public void addPreservationParameter(String moduleName, PreservationParameter parameter) {
    if (this.parameters.get(moduleName) != null) {
      this.parameters.get(moduleName).add(parameter);
    } else {
      List<PreservationParameter> preservationParameterList = new ArrayList<>();
      preservationParameterList.add(parameter);
      this.parameters.put(moduleName, preservationParameterList);
    }
  }

  public List<PreservationParameter> getRequiredParameters(String siardVersion) {
    List<PreservationParameter> requiredParameters = new ArrayList<>();
    for (Map.Entry<String, List<PreservationParameter>> entry : parameters.entrySet()) {
      if (entry.getKey().equals(siardVersion)) {
        for (PreservationParameter p : entry.getValue()) {
          if (p.isRequired()) {
            requiredParameters.add(p);
          }
        }
      }
    }
    return requiredParameters;
  }
}
