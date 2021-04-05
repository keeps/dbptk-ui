package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"show", "showContent", "template"})
public class ListStatus implements Serializable {

  private boolean show;
  private boolean showContent;
  private TemplateStatus template;

  public ListStatus() {
    showContent = true;
  }

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  public boolean isShowContent() {
    return showContent;
  }

  public void setShowContent(boolean showContent) {
    this.showContent = showContent;
  }

  public TemplateStatus getTemplate() {
    return template;
  }

  public void setTemplate(TemplateStatus template) {
    this.template = template;
  }

  @Override
  public String toString() {
    return "ListStatus{" + "show=" + show + ", showContent=" + showContent + ", template=" + template + '}';
  }
}
