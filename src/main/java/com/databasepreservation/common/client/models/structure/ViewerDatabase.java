package com.databasepreservation.common.client.models.structure;

import com.databasepreservation.common.client.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerDatabase extends IsIndexed {

  private String uuid;
  private ViewerDatabaseStatus status;
  private ViewerMetadata metadata;

  private String path;
  private long size;
  private String version;

  private String validatedAt;
  private String validatedVersion;
  private String validatorReportPath;
  private ViewerDatabaseValidationStatus validationStatus;
  private String validationPassed;
  private String validationErrors;
  private String validationWarnings;
  private String validationSkipped;

  public ViewerDatabase() {
  }

  /**
   * @return the uuid used by solr to identify this database
   */
  @Override
  public String getUuid() {
    return uuid;
  }

  /**
   * Setter for the parameter uuid
   * 
   * @param uuid
   *          the uuid used by solr to identify this database
   */
  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * @return the database status
   */
  public ViewerDatabaseStatus getStatus() {
    return status;
  }

  /**
   * Setter for the parameter status
   *
   * @param status
   *          the database status
   */
  public void setStatus(String status) {
    this.status = ViewerDatabaseStatus.valueOf(status);
  }

  /**
   * Setter for the parameter status
   *
   * @param status
   *          the database status
   */
  public void setStatus(ViewerDatabaseStatus status) {
    this.status = status;
  }

  /**
   * @return the database metadata
   */
  public ViewerMetadata getMetadata() {
    return metadata;
  }

  /**
   * Setter for the parameter metadata
   *
   * @param metadata
   *          the database metadata
   */
  public void setMetadata(ViewerMetadata metadata) {
    this.metadata = metadata;
  }


  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return this.path;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  public String getValidatedAt() {
    return validatedAt;
  }

  public void setValidatedAt(String validatedAt) {
    this.validatedAt = validatedAt;
  }

  public String getValidatedVersion() {
    return validatedVersion;
  }

  public void setValidatedVersion(String validatedVersion) {
    this.validatedVersion = validatedVersion;
  }

  public ViewerDatabaseValidationStatus getValidationStatus() {
    return validationStatus;
  }

  public void setValidationPassed(String validationPassed) {
    this.validationPassed = validationPassed;
  }

  public String getValidationPassed() {
    return validationPassed;
  }

  public void setValidationErrors(String validationErrors) {
    this.validationErrors = validationErrors;
  }

  public String getValidationErrors() {
    return validationErrors;
  }

  public void setValidationWarnings(String validationWarnings) {
    this.validationWarnings = validationWarnings;
  }

  public String getValidationWarnings() {
    return validationWarnings;
  }

  public void setValidationSkipped(String validationSkipped) {
    this.validationSkipped = validationSkipped;
  }

  public String getValidationSkipped() {
    return validationSkipped;
  }

  public void setValidationStatus(ViewerDatabaseValidationStatus status) {
    this.validationStatus = status;
  }

  public String getValidatorReportPath() {
    return validatorReportPath;
  }

  public void setValidatorReportPath(String validatorReportPath) {
    this.validatorReportPath = validatorReportPath;
  }
}
