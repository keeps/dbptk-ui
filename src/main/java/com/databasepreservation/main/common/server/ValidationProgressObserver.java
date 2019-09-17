package com.databasepreservation.main.common.server;

import com.databasepreservation.common.ValidationObserver;
import com.databasepreservation.main.common.shared.ValidationProgressData;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerValidator;
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
    progressData.setComponentName(componentName);
    progressData.setID(ID);
  }

  @Override
  public void notifyValidationStep(String componentName, String step, ValidationReporterStatus status) {
    progressData.setStepBeingValidated(step, status.name());
  }

  @Override
  public void notifyMessage(String componentName, String message, ValidationReporterStatus status) {
    progressData.setMessage(message);
  }

  @Override
  public void notifyFinishValidationModule(String componentName, ValidationReporterStatus status) {
    progressData.setStatus(status.name());
  }

  @Override
  public void notifyComponent(String ID, ValidationReporterStatus status) {
    progressData.setStepBeingValidated(ID, status.name());
  }

  @Override
  public void notifyElementValidating(String path) {
    progressData.setPathBeingValidated(path);
  }

  @Override
  public void notifyValidationProcessFinish(boolean result) {
    progressData.setFinished(result);
  }
}
