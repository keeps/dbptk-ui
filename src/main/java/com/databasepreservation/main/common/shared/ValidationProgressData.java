
package com.databasepreservation.main.common.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerValidator;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ValidationProgressData implements Serializable {

  private boolean finished = false;
  private int lastPosition = 0;
  private ViewerValidator component;
  private ViewerValidator.Requirement requirement;
  private boolean isComponent = false;
  private List<ViewerValidator> componentList = new ArrayList<>();

  private static HashMap<String, ValidationProgressData> instances = new HashMap<>();

  public static ValidationProgressData getInstance(String uuid) {
    if (instances.get(uuid) == null) {
      instances.put(uuid, new ValidationProgressData());
    }
    return instances.get(uuid);
  }

  private ValidationProgressData() {
  }

  public void setComponentName(String componentName) {
    component = ViewerValidator.getInstance(componentName);
    component.setComponentName(componentName);
    addComponent(component);
    System.out.println("componentName: " + componentName );
  }

  public void setID(String ID) {
    component.setComponentID(ID);
    System.out.println("ID: " + ID );
  }

  public void setStatus(String status) {
    component.setComponentStatus(status);
    System.out.println("STATUS: " + status );
  }

  public void setStepBeingValidated(String stepBeingValidated, String status) {
    System.out.println("step: " + stepBeingValidated );
    isComponent = false;
    requirement = ViewerValidator.Requirement.getInstance(stepBeingValidated);
    requirement.setRequirementID(stepBeingValidated);
    requirement.setRequirementStatus(status);
    component.addRequirement(requirement);
  }

  public void setMessage(String message) {
    isComponent = true;
    component.setComponentMessage(message);
  }

  public void setPathBeingValidated(String pathBeingValidated) {
    System.out.println("path: " + pathBeingValidated );
    if(isComponent){
      component.addPathBeingValidated(pathBeingValidated);
    } else {
      requirement.addPathBeingValidated(pathBeingValidated);
    }
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = !finished;
    System.out.println("finished: " + !finished );
  }

  public static HashMap<String, ValidationProgressData> getInstances() {
    return instances;
  }

  public static void setInstances(HashMap<String, ValidationProgressData> instances) {
    ValidationProgressData.instances = instances;
  }

  public void reset() {
    this.finished = false;
    this.componentList.clear();
    ViewerValidator.reset();
  }

  public List<ViewerValidator> getComponent() {
    List<ViewerValidator> sliceComponentList = componentList.subList(lastPosition, componentList.size());
    lastPosition = componentList.size();
    return sliceComponentList;
  }

  public void addComponent(ViewerValidator component) {
    if (!this.componentList.contains(component)) {
      this.componentList.add(component);
    }
  }
}
