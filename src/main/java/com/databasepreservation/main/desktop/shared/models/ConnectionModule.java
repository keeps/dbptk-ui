package com.databasepreservation.main.desktop.shared.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConnectionModule implements Serializable {

  private HashMap<String, ArrayList<PreservationParameter>> parameters;

  public ConnectionModule() {
    this.parameters = new HashMap<>();
  }

  public void setParameters(HashMap<String, ArrayList<PreservationParameter>> parameters) {
    this.parameters = parameters;
  }

  public HashMap<String, ArrayList<PreservationParameter>> getParameters() {
    return this.parameters;
  }

  public ArrayList<PreservationParameter> getParameters(String connection) {
   return getParameters().get(connection);
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

  public ConnectionModule getSIARDConnections() {
    ConnectionModule siard = new ConnectionModule();
    for (String key : this.parameters.keySet()) {
      if (key.toLowerCase().contains("siard")) {
        siard.addParameters(key, this.getPreservationParameter(key));
      }
    }

    return siard;
  }

  public ConnectionModule getDBMSConnections() {
    ConnectionModule dbms = new ConnectionModule();
    for (String key : this.parameters.keySet()) {
      if (!key.toLowerCase().contains("siard")) {
        dbms.addParameters(key, this.getPreservationParameter(key));
      }
    }

    return dbms;
  }

}
