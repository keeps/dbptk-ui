package com.databasepreservation.common.client.models.configuration.collection;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ColumnConfiguration implements Serializable {
  private String id;
  private String name;
  private String description;
  private Boolean hide;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getHide() {
    return hide;
  }

  public void setHide(Boolean hide) {
    this.hide = hide;
  }
}
