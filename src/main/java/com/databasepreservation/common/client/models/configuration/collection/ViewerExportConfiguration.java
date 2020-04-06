package com.databasepreservation.common.client.models.configuration.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@JsonPropertyOrder({"templateStatus"})
public class ViewerExportConfiguration implements Serializable {
  private ViewerTemplateConfiguration viewerTemplateConfiguration;

  public ViewerExportConfiguration() {
  }

  @JsonProperty("templateStatus")
  public ViewerTemplateConfiguration getViewerTemplateConfiguration() {
    return viewerTemplateConfiguration;
  }

  public void setViewerTemplateConfiguration(ViewerTemplateConfiguration viewerTemplateConfiguration) {
    this.viewerTemplateConfiguration = viewerTemplateConfiguration;
  }
}
