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
  private ArrayList<ExternalLOBsParameter> externalLOBsParameters;

  public TableAndColumnsParameters() {
  }

  public TableAndColumnsParameters(HashMap<String, ArrayList<ViewerColumn>> columns, ArrayList<ExternalLOBsParameter> externalLOBsParameters) {
    this.columns = columns;
    this.externalLOBsParameters = externalLOBsParameters;
  }

  public HashMap<String, ArrayList<ViewerColumn>> getColumns() {
    return columns;
  }

  public void setColumns(HashMap<String, ArrayList<ViewerColumn>> columns) {
    this.columns = columns;
  }

  public ArrayList<ExternalLOBsParameter> getExternalLOBsParameters() {
    return externalLOBsParameters;
  }

  public void setExternalLOBsParameters(ArrayList<ExternalLOBsParameter> externalLOBsParameters) {
    this.externalLOBsParameters = externalLOBsParameters;
  }
}
