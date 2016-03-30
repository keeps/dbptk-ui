package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerCell implements Serializable {
  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
