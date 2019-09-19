package com.databasepreservation.main.common.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumnsParameters implements Serializable {

  private HashMap<String, ArrayList<ViewerColumn>> columns; // Key: schema, Values: columns
  private ArrayList<ExternalLOBsParameter> externalLOBsParameters;
  private List<String> selectedSchemas;

  public TableAndColumnsParameters() {
  }

  public TableAndColumnsParameters(HashMap<String, ArrayList<ViewerColumn>> columns, ArrayList<ExternalLOBsParameter> externalLOBsParameters, ArrayList<String> selectedSchemas) {
    this.columns = columns;
    this.externalLOBsParameters = externalLOBsParameters;
    this.selectedSchemas = selectedSchemas;
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

  public List<String> getSelectedSchemas() {
    return selectedSchemas.stream().distinct().collect(Collectors.toList());
  }

  public void setSelectedSchemas(List<String> schemas) {
    this.selectedSchemas = schemas;
  }
}
