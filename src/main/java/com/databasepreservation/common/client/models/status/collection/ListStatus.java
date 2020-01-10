package com.databasepreservation.common.client.models.status.collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"hide", "template"})
public class ListStatus implements Serializable {

  private boolean hide;
  private TemplateStatus template;

  public ListStatus() { }

  public boolean isHide() {
    return hide;
  }

  public void setHide(boolean hide) {
    this.hide = hide;
  }

  public TemplateStatus getTemplate() {
    return template;
  }

  public void setTemplate(TemplateStatus template) {
    this.template = template;
  }

  @Override
  public String toString() {
    return "ListStatus{" +
        "hide=" + hide +
        ", template=" + template +
        '}';
  }
}
