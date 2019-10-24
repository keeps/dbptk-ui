package com.databasepreservation.common.shared.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerCell implements Serializable {
  private String value;

  public ViewerCell() {
  }

  public ViewerCell(String value) {
    this.value = value;
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


  @Override
  public String toString() {
    return value;
  }
}
