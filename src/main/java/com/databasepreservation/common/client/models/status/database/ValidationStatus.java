/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.database;

import java.io.Serializable;

import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"validationStatus", "createdOn", "reportLocation", "validatorVersion", "indicators"})
public class ValidationStatus implements Serializable {

  private ViewerDatabaseValidationStatus validationStatus;
  private String createdOn;
  private String reportLocation;
  private String validatorVersion;
  private Indicators indicators;

  public ValidationStatus() {
  }

  public ValidationStatus(ViewerDatabaseValidationStatus validationStatus, String createdOn, String reportLocation, String validatorVersion, Indicators indicators) {
    this.validationStatus = validationStatus;
    this.createdOn = createdOn;
    this.reportLocation = reportLocation;
    this.validatorVersion = validatorVersion;
    this.indicators = indicators;
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

  public Indicators getIndicators() {
    return indicators;
  }

  public void setIndicators(Indicators indicators) {
    this.indicators = indicators;
  }
}
