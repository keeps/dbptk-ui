package com.databasepreservation.common.client.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValidationProgressData implements Serializable {
  private boolean finished = false;
  private List<ValidationRequirement> requirementsList = new ArrayList<>();
  private ValidationRequirement requirement;
  private int numberOfPassed;
  private int numberOfOks;
  private int numberOfFailed;
  private int numberOfErrors;
  private int numberOfWarnings;
  private int numberOfSkipped;

  private static HashMap<String, ValidationProgressData> instances = new HashMap<>();

  public static ValidationProgressData getInstance(String uuid) {
    return instances.computeIfAbsent(uuid, k -> new ValidationProgressData());
  }

  public ValidationProgressData() {
  }

  public static void clear(String uuid) {
    instances.remove(uuid);
  }

  public void createRequirement(ValidationRequirement.Type type) {
    requirement = new ValidationRequirement(type);
    requirementsList.add(requirement);
  }

  public void setRequirementsList(List<ValidationRequirement> requirementsList) {
    this.requirementsList = requirementsList;
  }

  public void setRequirement(ValidationRequirement requirement) {
    this.requirement = requirement;
  }

  public void setRequirementID(String ID) {
    requirement.setId(ID);
  }

  public void setRequirementStatus(String status) {
    requirement.setStatus(status);
  }

  public void setRequirementMessage(String message) {
    requirement.setMessage(message);
  }

  public List<ValidationRequirement> getRequirementsList(int lastPosition) {
    return requirementsList.subList(lastPosition, requirementsList.size());
  }

  public List<ValidationRequirement> getRequirementsList() {
    return requirementsList;
  }

  public boolean getFinished() {
    return finished;
  }

  public void setFinished(boolean status) {
    finished = status;
  }

  public void setIndicators(int passed, int ok, int failed, int errors, int warnings, int skipped) {
    numberOfPassed = passed;
    numberOfOks = ok;
    numberOfFailed = failed;
    numberOfErrors = errors;
    numberOfWarnings = warnings;
    numberOfSkipped = skipped;
  }

  public int getNumberOfPassed() {
    return numberOfPassed;
  }

  public int getNumberOfErrors() {
    return numberOfErrors;
  }

  public int getNumberOfWarnings() {
    return numberOfWarnings;
  }

  public int getNumberOfSkipped() {
    return numberOfSkipped;
  }

  public int getNumberOfOks() {
    return numberOfOks;
  }

  public int getNumberOfFailed() {
    return numberOfFailed;
  }

  public void setNumberOfPassed(int numberOfPassed) {
    this.numberOfPassed = numberOfPassed;
  }

  public void setNumberOfOks(int numberOfOks) {
    this.numberOfOks = numberOfOks;
  }

  public void setNumberOfFailed(int numberOfFailed) {
    this.numberOfFailed = numberOfFailed;
  }

  public void setNumberOfErrors(int numberOfErrors) {
    this.numberOfErrors = numberOfErrors;
  }

  public void setNumberOfWarnings(int numberOfWarnings) {
    this.numberOfWarnings = numberOfWarnings;
  }

  public void setNumberOfSkipped(int numberOfSkipped) {
    this.numberOfSkipped = numberOfSkipped;
  }
}
