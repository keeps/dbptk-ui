package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"hide", "template"})
public class DetailsStatus implements Serializable {

  private boolean hide;
  private TemplateStatus templateStatus;

  public DetailsStatus() {
  }

  public boolean isHide() {
    return hide;
  }

  public void setHide(boolean hide) {
    this.hide = hide;
  }

  @JsonProperty("template")
  public TemplateStatus getTemplateStatus() {
    return templateStatus;
  }

  public void setTemplateStatus(TemplateStatus templateStatus) {
    this.templateStatus = templateStatus;
  }
}
