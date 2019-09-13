package com.databasepreservation.main.common.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

import com.databasepreservation.model.reporters.ValidationReporter.Status;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ValidationProgressData implements Serializable {

  private String componentName;
  private String ID;
  private Status status;
  private String message;
  private String stepBeingValidated;
  private String pathBeingValidated;
  private boolean finished = false;

  private static HashMap<String, ValidationProgressData> instances = new HashMap<>();

  public static ValidationProgressData getInstance(String uuid) {
    if (instances.get(uuid) == null) {
      instances.put(uuid, new ValidationProgressData());
    }
    return instances.get(uuid);
  }

  private ValidationProgressData() {
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public String getID() {
    return ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getStepBeingValidated() {
    return stepBeingValidated;
  }

  public void setStepBeingValidated(String stepBeingValidated) {
    this.stepBeingValidated = stepBeingValidated;
  }

  public String getPathBeingValidated() {
    return pathBeingValidated;
  }

  public void setPathBeingValidated(String pathBeingValidated) {
    this.pathBeingValidated = pathBeingValidated;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }

  public static HashMap<String, ValidationProgressData> getInstances() {
    return instances;
  }

  public static void setInstances(HashMap<String, ValidationProgressData> instances) {
    ValidationProgressData.instances = instances;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ValidationProgressData that = (ValidationProgressData) o;
    return isFinished() == that.isFinished() &&
        Objects.equals(getComponentName(), that.getComponentName()) &&
        Objects.equals(getID(), that.getID()) &&
        getStatus() == that.getStatus() &&
        Objects.equals(getMessage(), that.getMessage()) &&
        Objects.equals(getStepBeingValidated(), that.getStepBeingValidated()) &&
        Objects.equals(getPathBeingValidated(), that.getPathBeingValidated());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getComponentName(), getID(), getStatus(), getMessage(), getStepBeingValidated(), getPathBeingValidated(), isFinished());
  }

  public void reset() {
    componentName = "";
    ID = "";
    this.status = null;
    this.message = "";
    this.stepBeingValidated = "";
    this.pathBeingValidated = "";
    this.finished = false;
  }
}
