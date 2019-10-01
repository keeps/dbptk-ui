package com.databasepreservation.main.common.shared.ViewerStructure;

import org.roda.core.data.v2.validation.ValidationReport;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerDatabase extends IsIndexed {

  public enum Status {
    INGESTING, AVAILABLE, REMOVING, ERROR, METADATA_ONLY
  }

  public enum ValidationStatus {
    NOT_VALIDATED, VALIDATION_SUCCESS, VALIDATION_FAILED, VALIDATION_RUNNING, ERROR
  }

  private String uuid;
  private Status status;
  private ViewerMetadata metadata;

  // fields used when status is ingesting
  private long ingestedRows;
  private long totalRows;

  private long ingestedTables;
  private long totalTables;

  private long ingestedSchemas;
  private long totalSchemas;

  private String currentTableName;
  private String currentSchemaName;

  private String SIARDPath;
  private long SIARDSize;

  private String validatedAt;
  private String validatedVersion;
  private String validatorReportPath;
  private ValidationStatus validationStatus;
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
  public String getUUID() {
    return uuid;
  }

  /**
   * Setter for the parameter uuid
   * 
   * @param uuid
   *          the uuid used by solr to identify this database
   */
  @Override
  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  /**
   * @return the database status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Setter for the parameter status
   *
   * @param status
   *          the database status
   */
  public void setStatus(String status) {
    this.status = Status.valueOf(status);
  }

  /**
   * Setter for the parameter status
   *
   * @param status
   *          the database status
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * @return the database metadata
   */
  public ViewerMetadata getMetadata() {
    return metadata;
  }

  public long getIngestedRows() {
    return ingestedRows;
  }

  public void setIngestedRows(long ingestedRows) {
    this.ingestedRows = ingestedRows;
  }

  public long getTotalRows() {
    return totalRows;
  }

  public void setTotalRows(long totalRows) {
    this.totalRows = totalRows;
  }

  public long getIngestedTables() {
    return ingestedTables;
  }

  public void setIngestedTables(long ingestedTables) {
    this.ingestedTables = ingestedTables;
  }

  public long getTotalTables() {
    return totalTables;
  }

  public void setTotalTables(long totalTables) {
    this.totalTables = totalTables;
  }

  public long getIngestedSchemas() {
    return ingestedSchemas;
  }

  public void setIngestedSchemas(long ingestedSchemas) {
    this.ingestedSchemas = ingestedSchemas;
  }

  public long getTotalSchemas() {
    return totalSchemas;
  }

  public void setTotalSchemas(long totalSchemas) {
    this.totalSchemas = totalSchemas;
  }

  public String getCurrentTableName() {
    return currentTableName;
  }

  public void setCurrentTableName(String currentTableName) {
    this.currentTableName = currentTableName;
  }

  public String getCurrentSchemaName() {
    return currentSchemaName;
  }

  public void setCurrentSchemaName(String currentSchemaName) {
    this.currentSchemaName = currentSchemaName;
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

  public void setSIARDPath(String SIARDPath) {
    this.SIARDPath = SIARDPath;
  }

  public String getSIARDPath() {
    return this.SIARDPath;
  }

  public long getSIARDSize() {
    return SIARDSize;
  }

  public void setSIARDSize(long SIARDSize) {
    this.SIARDSize = SIARDSize;
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

  public ValidationStatus getValidationStatus() {
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

  public void setValidationStatus(ValidationStatus status) {
    this.validationStatus = status;
  }

  public String getValidatorReportPath() {
    return validatorReportPath;
  }

  public void setValidatorReportPath(String validatorReportPath) {
    this.validatorReportPath = validatorReportPath;
  }
}
