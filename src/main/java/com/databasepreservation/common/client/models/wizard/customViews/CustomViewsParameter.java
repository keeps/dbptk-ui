/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.customViews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.models.wizard.table.ColumnParameter;
import com.databasepreservation.common.client.models.wizard.table.ExternalLobParameter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomViewsParameter implements Serializable {
  private String schemaName;
  private Integer customViewID;
  private String customViewName;
  private String customViewDescription;
  private String customViewQuery;
  private List<ColumnParameter> columnParameters;

  public CustomViewsParameter() {
    this.columnParameters = new ArrayList<>();
  }

  public CustomViewsParameter(String schemaName, Integer customViewID, String customViewName,
    String customViewDescription, String customViewQuery) {
    this.schemaName = schemaName;
    this.customViewID = customViewID;
    this.customViewName = customViewName;
    this.customViewDescription = customViewDescription;
    this.customViewQuery = customViewQuery;
    this.columnParameters = new ArrayList<>();
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schema) {
    this.schemaName = schema;
  }

  public Integer getCustomViewID() {
    return customViewID;
  }

  public void setCustomViewID(Integer customViewID) {
    this.customViewID = customViewID;
  }

  public String getCustomViewName() {
    return customViewName;
  }

  public void setCustomViewName(String customViewName) {
    this.customViewName = customViewName;
  }

  public String getCustomViewDescription() {
    return customViewDescription;
  }

  public void setCustomViewDescription(String customViewDescription) {
    this.customViewDescription = customViewDescription;
  }

  public String getCustomViewQuery() {
    return customViewQuery;
  }

  public void setCustomViewQuery(String customViewQuery) {
    this.customViewQuery = customViewQuery;
  }

  public List<ColumnParameter> getColumnParameters() {
    return this.columnParameters;
  }

  public void setColumnParametersFromExternalLobs(Map<String, ExternalLobParameter> externalLobsParameters) {
    this.columnParameters = externalLobsParameters.entrySet().stream()
      .map(entry -> new ColumnParameter(entry.getValue(), entry.getKey(), false)).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CustomViewsParameter parameter = (CustomViewsParameter) o;
    return Objects.equals(schemaName, parameter.schemaName)
      && Objects.equals(getCustomViewName(), parameter.getCustomViewName())
      && Objects.equals(getCustomViewDescription(), parameter.getCustomViewDescription())
      && Objects.equals(getCustomViewQuery(), parameter.getCustomViewQuery())
      && Objects.equals(getColumnParameters(), parameter.getColumnParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaName, getCustomViewName(), getCustomViewDescription(), getCustomViewQuery(),
      getColumnParameters());
  }
}
