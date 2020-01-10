package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"show", "template"})
public class DetailsStatus implements Serializable {

  private boolean show;
  private TemplateStatus templateStatus;

  public DetailsStatus() {
  }

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  @JsonProperty("template")
  public TemplateStatus getTemplateStatus() {
    return templateStatus;
  }

  public void setTemplateStatus(TemplateStatus templateStatus) {
    this.templateStatus = templateStatus;
  }
}
