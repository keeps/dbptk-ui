/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serializable;
import java.util.Date;

import com.databasepreservation.common.client.index.IsIndexed;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ViewerJob extends IsIndexed implements Serializable {

  private String uuid;
  private Long jobId;
  private String databaseUuid;
  private String collectionUuid;
  private String databaseName;
  private String tableUuid;
  private String schemaName;
  private String tableName;
  private String name;
  private ViewerJobStatus status;
  private Date createTime;
  private Date startTime;
  private Date endTime;
  private String exitCode;
  private String exitDescription;
  private Long rowsToProcess;
  private Long processRows;

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  public String getCollectionUuid() {
    return collectionUuid;
  }

  public void setCollectionUuid(String collectionUuid) {
    this.collectionUuid = collectionUuid;
  }

  public Long getJobId() {
    return jobId;
  }

  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ViewerJobStatus getStatus() {
    return status;
  }

  public void setStatus(ViewerJobStatus status) {
    this.status = status;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public void setDatabaseUuid(String databaseUuid) {
    this.databaseUuid = databaseUuid;
  }

  public String getDatabaseUuid() {
    return databaseUuid;
  }

  public void setTableUuid(String tableUuid) {
    this.tableUuid = tableUuid;
  }

  public String getTableUuid() {
    return tableUuid;
  }

  public String getExitCode() {
    return exitCode;
  }

  public void setExitCode(String exitCode) {
    this.exitCode = exitCode;
  }

  public String getExitDescription() {
    return exitDescription;
  }

  public void setExitDescription(String exitDescription) {
    this.exitDescription = exitDescription;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public String getTableName() {
    return tableName;
  }

  @JsonIgnore
  public String getTableId() {
    return schemaName + "." + tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Long getRowsToProcess() {
    return rowsToProcess;
  }

  public void setRowsToProcess(Long rowsToProcess) {
    this.rowsToProcess = rowsToProcess;
  }

  public Long getProcessRows() {
    return processRows;
  }

  public void setProcessRows(Long processRows) {
    this.processRows = processRows;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }
}
