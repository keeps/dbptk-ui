package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.Date;

import com.databasepreservation.common.client.models.status.IsProcessable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualTableStatus implements Serializable, IsProcessable {
  private ProcessingState processingState;
  private Date lastUpdatedDate;
  private Date lastExecutionDate;

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
