package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.Date;

import com.databasepreservation.common.client.models.status.IsProcessable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualTableStatus implements Serializable, IsProcessable {
  private String sourceTableUUID;
  private ProcessingState processingState;
  private Date lastUpdatedDate;
  private Date lastExecutionDate;
  private boolean useSourceTableForeignKeys;

  public String getSourceTableUUID() {
    return sourceTableUUID;
  }

  public void setSourceTableUUID(String sourceTableUUID) {
    this.sourceTableUUID = sourceTableUUID;
  }

  public boolean getUseSourceTableForeignKeys() {
    return useSourceTableForeignKeys;
  }

  public void setUseSourceTableForeignKeys(boolean useSourceTableForeignKeys) {
    this.useSourceTableForeignKeys = useSourceTableForeignKeys;
  }

  @Override
  public ProcessingState getProcessingState() {
    return processingState;
  }

  public void setProcessingState(ProcessingState processingState) {
    this.processingState = processingState;
  }

  @Override
  public Date getLastUpdatedDate() {
    return lastUpdatedDate;
  }

  public void setLastUpdatedDate(Date lastUpdatedDate) {
    this.lastUpdatedDate = lastUpdatedDate;
  }

  @Override
  public Date getLastExecutionDate() {
    return lastExecutionDate;
  }

  public void setLastExecutionDate(Date lastExecutionDate) {
    this.lastExecutionDate = lastExecutionDate;
  }
}
