package com.databasepreservation.common.client.models.configuration.collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"show", "template"})
public class ViewerListConfiguration implements Serializable {

  private boolean show;
  private ViewerTemplateConfiguration template;

  public ViewerListConfiguration() { }

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  public ViewerTemplateConfiguration getTemplate() {
    return template;
  }

  public void setTemplate(ViewerTemplateConfiguration template) {
    this.template = template;
  }

  @Override
  public String toString() {
    return "ListStatus{" +
        "show=" + show +
        ", template=" + template +
        '}';
  }
}
