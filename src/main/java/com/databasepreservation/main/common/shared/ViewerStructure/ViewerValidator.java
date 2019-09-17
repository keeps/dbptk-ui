package com.databasepreservation.main.common.shared.ViewerStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ViewerValidator implements IsSerializable {
  private String componentID;
  private String componentName;
  private String componentMessage;
  private String componentStatus;
  private Map<String, List<String>> pathListBeingValidated = new HashMap<>();
  private List<Requirement> requirementList;
  private static Map<String, ViewerValidator> instances = new HashMap<>();

  public static ViewerValidator getInstance(String componentName) {
    if (instances.get(componentName) == null) {
      instances.put(componentName, new ViewerValidator());
    }
    return instances.get(componentName);
  }

  public ViewerValidator() {
    requirementList = new ArrayList<>();
  }

  public String getComponentID() {
    return componentID;
  }

  public void setComponentID(String componentID) {
    this.componentID = componentID;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public String getComponentStatus() {
    return componentStatus;
  }

  public void setComponentStatus(String componentStatus) {
    this.componentStatus = componentStatus;
  }

  public List<Requirement> getRequirementList() {
    return requirementList;
  }

  public void addRequirement(Requirement requirementList) {
    if (!this.requirementList.contains(requirementList)) {
      this.requirementList.add(requirementList);
    }
  }

  public void setComponentMessage(String componentMessage) {
    this.componentMessage = componentMessage;
    if(pathListBeingValidated.get(componentMessage) == null){
        pathListBeingValidated.put(componentMessage, new ArrayList<>());
    }
  }

  public void addPathBeingValidated(String pathBeingValidated) {
    pathListBeingValidated.get(this.componentMessage).add(pathBeingValidated);
  }

  public Map<String, List<String>> getPathListBeingValidated() {
    return pathListBeingValidated;
  }

  public static class Requirement implements IsSerializable {
    private String requirementID;
    private String requirementStatus;
    private static Map<String, Requirement> instances = new HashMap<>();
    private Map<String, List<String>> pathListBeingValidated = new HashMap<>();

    public static Requirement getInstance(String requirementID) {
      if (instances.get(requirementID) == null) {
        instances.put(requirementID, new Requirement());
      }
      return instances.get(requirementID);
    }

    private Requirement() {

    }

    public String getRequirementID() {
      return requirementID;
    }

    public void setRequirementID(String requirementID) {
      this.requirementID = requirementID;
      if(pathListBeingValidated.get(requirementID) == null){
        pathListBeingValidated.put(requirementID, new ArrayList<>());
      }
    }

    public String getRequirementStatus() {
      return requirementStatus;
    }

    public void setRequirementStatus(String requirementStatus) {
      this.requirementStatus = requirementStatus;
    }

    public void addPathBeingValidated(String pathBeingValidated) {
      pathListBeingValidated.get(this.requirementID).add(pathBeingValidated);
    }

    public Map<String, List<String>> getPathListBeingValidated() {
      return pathListBeingValidated;
    }
  }

  public static void reset() {
    instances.clear();
    Requirement.instances.clear();
  }
}
