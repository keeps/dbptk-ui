package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TemplateStatus implements Serializable {

  private String template;

  public TemplateStatus() {
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }
}
