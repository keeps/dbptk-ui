package com.databasepreservation.common.client.models.status.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.databasepreservation.common.client.models.status.IsProcessable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualForeignKeysStatus implements Serializable, IsProcessable {
  @Serial
  private static final long serialVersionUID = 2221987881110464154L;

  private ProcessingState processingState;
  private TemplateStatus templateStatus;
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

  public TemplateStatus getTemplateStatus() {
    return templateStatus;
  }

  public void setTemplateStatus(TemplateStatus templateStatus) {
    this.templateStatus = templateStatus;
  }
}
