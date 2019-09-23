package com.databasepreservation.main.common.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValidationProgressData implements Serializable {
  private boolean finished = false;
  private List<Requirement> requirementsList = new ArrayList<>();
  private Requirement requirement;
  private int numberOfWarnings;

  private static HashMap<String, ValidationProgressData> instances = new HashMap<>();

  public static ValidationProgressData getInstance(String uuid) {
    if (instances.get(uuid) == null) {
      instances.put(uuid, new ValidationProgressData());
    }
    return instances.get(uuid);
  }

  public ValidationProgressData() {
  }

  public static void clear(String uuid) {
    instances.remove(uuid);
  }

  public void createRequirement(Requirement.Type type) {
    requirement = new Requirement(type);
    requirementsList.add(requirement);
  }

  public void setRequirementID(String ID) {
    requirement.setID(ID);
  }

  public void setRequirementStatus(String status) {
    requirement.setStatus(status);
  }

  public void setRequirementMessage(String message) {
    requirement.setMessage(message);
  }

  public List<Requirement> getRequirementsList(int lastPosition) {
    return requirementsList.subList(lastPosition, requirementsList.size());
  }

  public List<Requirement> getRequirementsList() {
    return requirementsList;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean hasErrors) {
    finished = true;
  }

  public void setNumberOfWarning(int warnings) {
    numberOfWarnings = warnings;
  }

  public int getNumberOfWarnings() {
    return numberOfWarnings;
  }

  public static class Requirement implements Serializable {
    private String ID;
    private String message;
    private String status;
    private Type type;

    public Requirement(Type type) {
      this.type = type;
    }

    public Requirement() {
    }

    public Type getType() {
      return type;
    }

    public enum Type {
      REQUIREMENT, REQUIREMENT_INIT, MESSAGE, SUB_REQUIREMENT, ADDITIONAL, PATH
    }

    public void setID(String ID) {
      this.ID = ID;
    }

    public String getID() {
      return ID;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
