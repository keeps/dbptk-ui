package com.databasepreservation.common.client.common.lists.cells.helper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CheckboxData {

  private boolean checked;
  private boolean disable;

  public CheckboxData() {  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }

  public boolean isDisable() {
    return disable;
  }

  public void setDisable(boolean disable) {
    this.disable = disable;
  }
}
