package com.databasepreservation.common.client.models.wizard.table;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumnsParameters implements Serializable {

  private Map<String, List<String>> columns; // Key: schema, Values: columns
  private List<ExternalLOBsParameter> externalLOBsParameters;
  private List<String> selectedSchemas;

  public TableAndColumnsParameters() {
  }

  public TableAndColumnsParameters(Map<String, List<String>> columns,
    List<ExternalLOBsParameter> externalLOBsParameters, List<String> selectedSchemas) {
    this.columns = columns;
    this.externalLOBsParameters = externalLOBsParameters;
    this.selectedSchemas = selectedSchemas;
  }

  public Map<String, List<String>> getColumns() {
    return columns;
  }

  public void setColumns(Map<String, List<String>> columns) {
    this.columns = columns;
  }

  public List<ExternalLOBsParameter> getExternalLOBsParameters() {
    return externalLOBsParameters;
  }

  public void setExternalLOBsParameters(List<ExternalLOBsParameter> externalLOBsParameters) {
    this.externalLOBsParameters = externalLOBsParameters;
  }

  public List<String> getSelectedSchemas() {
    return selectedSchemas.stream().distinct().collect(Collectors.toList());
  }

  public void setSelectedSchemas(List<String> schemas) {
    this.selectedSchemas = schemas;
  }
}
