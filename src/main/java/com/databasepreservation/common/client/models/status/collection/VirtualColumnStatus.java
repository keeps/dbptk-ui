package com.databasepreservation.common.client.models.status.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStatus implements Serializable {
  @Serial
  private static final long serialVersionUID = -4361681277573813148L;

  private List<String> sourceColumnsIds;
  private TemplateStatus sourceTemplateStatus;

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
}
