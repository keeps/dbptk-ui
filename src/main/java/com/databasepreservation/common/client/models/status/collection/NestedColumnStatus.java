package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class NestedColumnStatus implements Serializable {
  private String id;
  private String name;
  private String originalTable;
  private Boolean multiValue = false;
  private List<String> nestedFields = new ArrayList<>();
  private List<String> nestedSolrNames = new ArrayList<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOriginalTable() {
    return originalTable;
  }

  public void setOriginalTable(String originalTable) {
    this.originalTable = originalTable;
  }

  public List<String> getNestedFields() {
    return nestedFields;
  }

  public void setNestedFields(List<String> nestedFields) {
    this.nestedFields = nestedFields;
  }

  public Boolean getMultiValue() {
    return multiValue;
  }

  public void setMultiValue(Boolean multiValue) {
    this.multiValue = multiValue;
  }

  public List<String> getNestedSolrNames() {
    return nestedSolrNames;
  }

  public void setNestedSolrNames(List<String> nestedSolrNames) {
    this.nestedSolrNames = nestedSolrNames;
  }
}
