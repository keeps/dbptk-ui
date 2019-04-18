package com.databasepreservation.visualization.shared.ViewerStructure;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerDatabase extends IsIndexed {

  public enum Status {
    INGESTING, AVAILABLE, REMOVING, ERROR
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
}
