package com.databasepreservation.common.client.models.status.helpers;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableStatusHelper {

  private String label;
  private String description;

  public TableStatusHelper() {
  }

  public TableStatusHelper(String label, String description) {
    this.label = label;
    this.description = description;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
