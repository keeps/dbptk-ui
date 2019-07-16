package com.databasepreservation.main.desktop.shared.models.wizardParameters;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumnsParameters implements Serializable {

  private HashMap<String, ArrayList<ViewerColumn>> columns; // Key: schema, Values: columns

  public TableAndColumnsParameters() {
  }

  public TableAndColumnsParameters(HashMap<String, ArrayList<ViewerColumn>> columns) {
    this.columns = columns;
  }

  public HashMap<String, ArrayList<ViewerColumn>> getColumns() {
    return columns;
  }

  public void setColumns(HashMap<String, ArrayList<ViewerColumn>> columns) {
    this.columns = columns;
  }
}
