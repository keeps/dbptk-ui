package com.databasepreservation.common.client.models.configuration.database;

import java.io.Serializable;

import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"validationStatus", "createdOn", "reportLocation", "validatorVersion", "indicators"})
public class ViewerValidationConfiguration implements Serializable {

  private ViewerDatabaseValidationStatus validationStatus;
  private String createdOn;
  private String reportLocation;
  private String validatorVersion;
  private ViewerValidationIndicators viewerValidationIndicators;

  public ViewerValidationConfiguration() {
  }

  public ViewerValidationConfiguration(ViewerDatabaseValidationStatus validationStatus, String createdOn, String reportLocation, String validatorVersion, ViewerValidationIndicators viewerValidationIndicators) {
    this.validationStatus = validationStatus;
    this.createdOn = createdOn;
    this.reportLocation = reportLocation;
    this.validatorVersion = validatorVersion;
    this.viewerValidationIndicators = viewerValidationIndicators;
  }

  public ViewerDatabaseValidationStatus getValidationStatus() {
    return validationStatus;
  }

  public void setValidationStatus(ViewerDatabaseValidationStatus validationStatus) {
    this.validationStatus = validationStatus;
  }

  public String getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(String createdOn) {
    this.createdOn = createdOn;
  }

  public String getReportLocation() {
    return reportLocation;
  }

  public void setReportLocation(String reportLocation) {
    this.reportLocation = reportLocation;
  }

  public String getValidatorVersion() {
    return validatorVersion;
  }

  public void setValidatorVersion(String validatorVersion) {
    this.validatorVersion = validatorVersion;
  }

  public ViewerValidationIndicators getViewerValidationIndicators() {
    return viewerValidationIndicators;
  }

  public void setViewerValidationIndicators(ViewerValidationIndicators viewerValidationIndicators) {
    this.viewerValidationIndicators = viewerValidationIndicators;
  }
}
