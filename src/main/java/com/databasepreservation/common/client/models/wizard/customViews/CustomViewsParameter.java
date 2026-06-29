/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.customViews;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
  private Map<String, ExternalLobParameter> externalLobsParameters;

  public CustomViewsParameter() {
    this.externalLobsParameters = new HashMap<>();
  }

  public CustomViewsParameter(String schemaName, Integer customViewID, String customViewName,
    String customViewDescription, String customViewQuery) {
    this.schemaName = schemaName;
    this.customViewID = customViewID;
    this.customViewName = customViewName;
    this.customViewDescription = customViewDescription;
    this.customViewQuery = customViewQuery;
    this.externalLobsParameters = new HashMap<>();
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

  public Map<String, ExternalLobParameter> getExternalLobsParameters() {
    return externalLobsParameters;
  }

  public void setExternalLobsParameters(Map<String, ExternalLobParameter> externalLobsParameters) {
    this.externalLobsParameters = externalLobsParameters;
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
      && Objects.equals(getExternalLobsParameters(), parameter.getExternalLobsParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaName, getCustomViewName(), getCustomViewDescription(), getCustomViewQuery(),
      getExternalLobsParameters());
  }
}
