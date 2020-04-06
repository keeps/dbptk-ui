package com.databasepreservation.common.client.models.configuration.collection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"show", "template"})
public class ViewerDetailsConfiguration implements Serializable {

  private boolean show;
  private ViewerTemplateConfiguration viewerTemplateConfiguration;

  public ViewerDetailsConfiguration() {
  }

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  @JsonProperty("template")
  public ViewerTemplateConfiguration getViewerTemplateConfiguration() {
    return viewerTemplateConfiguration;
  }

  public void setViewerTemplateConfiguration(ViewerTemplateConfiguration viewerTemplateConfiguration) {
    this.viewerTemplateConfiguration = viewerTemplateConfiguration;
  }
}
