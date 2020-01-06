package com.databasepreservation.common.client.models.structure;

import com.databasepreservation.common.client.index.IsIndexed;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ViewerJob extends IsIndexed implements Serializable {

  private String uuid;
  private String databaseUuid;
  private String tableUuid;
  private String name;
  private ViewerJobStatus status;
  private String startTime;
  private String endTime;

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public String getUuid() {
    return uuid;
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

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
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
}
