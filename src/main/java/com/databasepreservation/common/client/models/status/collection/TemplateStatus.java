package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TemplateStatus implements Serializable {

  private String template;
  private String separator;

  public TemplateStatus() {
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }
}
