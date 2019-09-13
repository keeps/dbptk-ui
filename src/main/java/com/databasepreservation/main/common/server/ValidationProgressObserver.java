package com.databasepreservation.main.common.server;

import com.databasepreservation.common.ValidationObserver;
import com.databasepreservation.main.common.shared.ValidationProgressData;
import com.databasepreservation.model.reporters.ValidationReporter;

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
    progressData.setComponentName(componentName);
    progressData.setID(ID);
  }

  @Override
  public void notifyValidationStep(String componentName, String step, ValidationReporter.Status status) {
    progressData.setComponentName(componentName);
    progressData.setStepBeingValidated(step);
    progressData.setStatus(status);
  }

  @Override
  public void notifyMessage(String componentName, String message, ValidationReporter.Status status) {
    progressData.setComponentName(componentName);
    progressData.setMessage(message);
    progressData.setStatus(status);
  }

  @Override
  public void notifyFinishValidationModule(String componentName, ValidationReporter.Status status) {
    progressData.setComponentName(componentName);
    progressData.setStatus(status);
  }

  @Override
  public void notifyComponent(String ID, ValidationReporter.Status status) {
    progressData.setID(ID);
    progressData.setStatus(status);
  }

  @Override
  public void notifyElementValidating(String path) {
    progressData.setPathBeingValidated(path);
  }
}
