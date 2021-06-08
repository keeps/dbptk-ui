/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"show", "showContent", "template", "customizeProperties"})
public class ListStatus implements Serializable {

  private boolean show;
  private boolean showContent;
  private TemplateStatus template;
  private CustomizeProperties customizeProperties;

  public ListStatus() {
    showContent = true;
    customizeProperties = new CustomizeProperties();
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

  public CustomizeProperties getCustomizeProperties() {
    return customizeProperties;
  }

  public void setCustomizeProperties(CustomizeProperties customizeProperties) {
    this.customizeProperties = customizeProperties;
  }

  @Override
  public String toString() {
    return "ListStatus{" + "show=" + show + ", showContent=" + showContent + ", template=" + template
      + ", customizeProperties=" + customizeProperties + '}';
  }
}
