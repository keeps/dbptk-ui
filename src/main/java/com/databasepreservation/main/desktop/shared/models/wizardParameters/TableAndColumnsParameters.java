package com.databasepreservation.main.desktop.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumnsParameters implements Serializable {

  private HashMap<String, ArrayList<String>> columns;

  public TableAndColumnsParameters() {
  }

  public TableAndColumnsParameters(HashMap<String, ArrayList<String>> columns) {
    this.columns = columns;
  }

  public HashMap<String, ArrayList<String>> getColumns() {
    return columns;
  }

  public void setColumns(HashMap<String, ArrayList<String>> columns) {
    this.columns = columns;
  }
}
