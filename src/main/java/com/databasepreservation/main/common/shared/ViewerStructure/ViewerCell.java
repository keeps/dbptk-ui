package com.databasepreservation.main.common.shared.ViewerStructure;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerCell implements IsSerializable {
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
}
