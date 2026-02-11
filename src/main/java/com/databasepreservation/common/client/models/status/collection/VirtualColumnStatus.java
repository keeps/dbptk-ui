package com.databasepreservation.common.client.models.status.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.databasepreservation.common.client.models.status.IsProcessable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStatus implements Serializable, IsProcessable {
  @Serial
  private static final long serialVersionUID = -4361681277573813148L;

  private List<String> sourceColumnsIds;
  private TemplateStatus sourceTemplateStatus;
  private Date lastUpdatedDate;
  private Date lastExecutionDate;

  public VirtualColumnStatus() {
  }

  public TemplateStatus getSourceTemplateStatus() {
    return sourceTemplateStatus;
  }

  public void setSourceTemplateStatus(TemplateStatus sourceTemplateStatus) {
    this.sourceTemplateStatus = sourceTemplateStatus;
  }

  public List<String> getSourceColumnsIds() {
    return sourceColumnsIds;
  }

  public void setSourceColumnsIds(List<String> sourceColumnsIds) {
    this.sourceColumnsIds = sourceColumnsIds;
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
