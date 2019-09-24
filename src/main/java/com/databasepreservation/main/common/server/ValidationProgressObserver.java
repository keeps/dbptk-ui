package com.databasepreservation.main.common.server;

import com.databasepreservation.common.ValidationObserver;
import com.databasepreservation.main.common.shared.ValidationProgressData;
import com.databasepreservation.model.reporters.ValidationReporterStatus;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ValidationProgressObserver implements ValidationObserver {
private ValidationProgressData progressData;

  public ValidationProgressObserver(String UUID) {
    progressData = ValidationProgressData.getInstance(UUID);
  }

  @Override
  public void notifyStartValidationModule(String componentName, String ID) {
    progressData.createRequirement(ValidationProgressData.Requirement.Type.REQUIREMENT);
    progressData.setRequirementID(ID);
    progressData.setRequirementMessage(componentName);
  }

  @Override
  public void notifyFinishValidationModule(String componentName, ValidationReporterStatus status) {
  }

  @Override
  public void notifyMessage(String componentName, String ID, String message, ValidationReporterStatus status) {
    progressData.createRequirement(ValidationProgressData.Requirement.Type.MESSAGE);
    progressData.setRequirementID(ID);
    progressData.setRequirementMessage(message);
    progressData.setRequirementStatus(status.name());
  }

  @Override
  public void notifyValidationStep(String componentName, String step, ValidationReporterStatus status) {
    progressData.createRequirement(ValidationProgressData.Requirement.Type.SUB_REQUIREMENT);
    progressData.setRequirementID(step);
    progressData.setRequirementStatus(status.name());
  }


  @Override
  public void notifyComponent(String ID, ValidationReporterStatus status) {
    progressData.createRequirement(ValidationProgressData.Requirement.Type.ADDITIONAL);
    progressData.setRequirementID(ID);
    progressData.setRequirementStatus(status.name());
  }

  @Override
  public void notifyElementValidating(String ID, String path) {
    progressData.createRequirement(ValidationProgressData.Requirement.Type.PATH);
    progressData.setRequirementID(ID);
    progressData.setRequirementMessage(path);
  }

  @Override
  public void notifyNumberOfWarnings(int warnings) {
    progressData.setNumberOfWarning(warnings);
  }

  @Override
  public void notifyValidationProcessFinish(boolean result) {
    progressData.setFinished(result);
  }
}
