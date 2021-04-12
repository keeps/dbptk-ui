/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class NestedColumnStatus implements Serializable {
  private String originalTable;
  private String path;
  private Boolean multiValue = false;
  private List<String> nestedFields = new ArrayList<>();
  private List<String> nestedSolrNames = new ArrayList<>();
  private Integer quantityInList = 10;

  @JsonIgnore
  private final Integer MAX_QUANTITY_IN_LIST = 20;

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

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Boolean getMultiValue() {
    return multiValue;
  }

  public Integer getQuantityInList() {
    return quantityInList;
  }

  public void setQuantityInList(Integer quantityInList) {
    this.quantityInList = quantityInList;
  }

  @JsonIgnore
  public Integer getMaxQuantityInList() {
    return MAX_QUANTITY_IN_LIST;
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
