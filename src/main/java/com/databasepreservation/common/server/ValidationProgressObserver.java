package com.databasepreservation.common.server;

import com.databasepreservation.common.ValidationObserver;
import com.databasepreservation.common.client.models.ValidationRequirement;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.client.models.ValidationProgressData;
import com.databasepreservation.model.reporters.ValidationReporterStatus;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ValidationProgressObserver implements ValidationObserver {
  private ValidationProgressData progressData;
  private String databaseUUID;

  public ValidationProgressObserver(String UUID) {
    databaseUUID = UUID;
    progressData = ValidationProgressData.getInstance(UUID);
  }

  @Override
  public void notifyStartValidationModule(String componentName, String ID) {
    progressData.createRequirement(ValidationRequirement.Type.REQUIREMENT);
    progressData.setRequirementID(ID);
    progressData.setRequirementMessage(componentName);
  }

  @Override
  public void notifyFinishValidationModule(String componentName, ValidationReporterStatus status) {
    // Nothing to do
  }

  @Override
  public void notifyMessage(String componentName, String ID, String message, ValidationReporterStatus status) {
    progressData.createRequirement(ValidationRequirement.Type.MESSAGE);
    progressData.setRequirementID(ID);
    progressData.setRequirementMessage(message);
    progressData.setRequirementStatus(status.name());
  }

  @Override
  public void notifyValidationStep(String componentName, String step, ValidationReporterStatus status) {
    progressData.createRequirement(ValidationRequirement.Type.SUB_REQUIREMENT);
    progressData.setRequirementID(step);
    progressData.setRequirementStatus(status.name());
  }

  @Override
  public void notifyComponent(String ID, ValidationReporterStatus status) {
    progressData.createRequirement(ValidationRequirement.Type.ADDITIONAL);
    progressData.setRequirementID(ID);
    progressData.setRequirementStatus(status.name());
  }

  @Override
  public void notifyElementValidating(String ID, String path) {
    progressData.createRequirement(ValidationRequirement.Type.PATH);
    progressData.setRequirementID(ID);
    progressData.setRequirementMessage(path);
  }

  @Override
  public void notifyIndicators(int passed, int ok, int failed, int errors, int warnings, int skipped) {
    SIARDController.updateSIARDValidatorIndicators(databaseUUID, Integer.toString(passed), Integer.toString(ok), Integer.toString(failed), Integer.toString(errors),
      Integer.toString(warnings), Integer.toString(skipped));
    progressData.setIndicators(passed, ok, failed, errors, warnings, skipped);
  }

  @Override
  public void notifyValidationProcessFinish(boolean result) {
    progressData.setFinished(true);
  }

  @Override
  public void notifyValidationProgressSparse(int numberOfRows) {
    progressData.createRequirement(ValidationRequirement.Type.SPARSE_PROGRESS);
    progressData.setRequirementMessage(Integer.toString(numberOfRows));
  }

  @Override
  public void notifyElementValidationFinish(String ID, String path, ValidationReporterStatus status) {
    progressData.createRequirement(ValidationRequirement.Type.PATH_COMPLETE);
    progressData.setRequirementID(ID);
    progressData.setRequirementMessage(path);
    progressData.setRequirementStatus(status.name());
  }
}
