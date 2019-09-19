package com.databasepreservation.main.common.shared.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DBPTKModule implements Serializable {

  private HashMap<String, ArrayList<PreservationParameter>> parameters;

  public DBPTKModule() {
    this.parameters = new HashMap<>();
  }

  public void setParameters(HashMap<String, ArrayList<PreservationParameter>> parameters) {
    this.parameters = parameters;
  }

  public HashMap<String, ArrayList<PreservationParameter>> getParameters() {
    return this.parameters;
  }

  public ArrayList<PreservationParameter> getParameters(String key) {
    return getParameters().get(key);
  }

  public void addPreservationParameter(String moduleName, PreservationParameter parameter) {
    if (this.parameters.get(moduleName) != null) {
      this.parameters.get(moduleName).add(parameter);
    } else {
      ArrayList<PreservationParameter> parameters = new ArrayList<>();
      parameters.add(parameter);
      this.parameters.put(moduleName, parameters);
    }
  }

  private void addParameters(String key, ArrayList<PreservationParameter> value) {
    this.parameters.put(key, value);
  }

  private ArrayList<PreservationParameter> getPreservationParameter(String moduleName) {
    return this.getParameters().get(moduleName);
  }

  public ArrayList<PreservationParameter> getRequiredParameters() {
    ArrayList<PreservationParameter> requiredParameters = new ArrayList<>();
    for (Map.Entry<String, ArrayList<PreservationParameter>> entry : parameters.entrySet()) {
      for (PreservationParameter p : entry.getValue()) {
        if (p.isRequired()) {
          requiredParameters.add(p);
        }
      }
    }
    return requiredParameters;
  }

  public ArrayList<PreservationParameter> getRequiredParameters(String siardVersion) {
    ArrayList<PreservationParameter> requiredParameters = new ArrayList<>();
    for (Map.Entry<String, ArrayList<PreservationParameter>> entry : parameters.entrySet()) {
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

  public DBPTKModule getSIARDConnections() {
    DBPTKModule siard = new DBPTKModule();
    for (String key : this.parameters.keySet()) {
      if (key.toLowerCase().contains("siard")) {
        siard.addParameters(key, this.getPreservationParameter(key));
      }
    }

    return siard;
  }

  public DBPTKModule getDBMSConnections() {
    DBPTKModule dbms = new DBPTKModule();
    for (String key : this.parameters.keySet()) {
      if (!key.toLowerCase().contains("siard")) {
        dbms.addParameters(key, this.getPreservationParameter(key));
      }
    }

    return dbms;
  }

}
