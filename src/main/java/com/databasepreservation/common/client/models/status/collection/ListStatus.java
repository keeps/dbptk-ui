package com.databasepreservation.common.client.models.status.collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"show", "template"})
public class ListStatus implements Serializable {

  private boolean show;
  private TemplateStatus template;

  public ListStatus() { }

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
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
        "show=" + show +
        ", template=" + template +
        '}';
  }
}
