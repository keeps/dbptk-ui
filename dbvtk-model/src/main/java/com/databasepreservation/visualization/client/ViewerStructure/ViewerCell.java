package com.databasepreservation.visualization.client.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerCell implements Serializable {
  private String value;

  public ViewerCell() {
  }

  /**
   * Gets the value of this cell as string
   * 
   * @return the value or null if the database cell value was NULL
   */
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
