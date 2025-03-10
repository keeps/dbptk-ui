/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrAlias implements Serializable {
  @Serial
  private static final long serialVersionUID = -5247919236401053565L;

  @JsonProperty("name")
  private String name;
  @JsonProperty("collections")
  private List<String> collections;

  public SolrAlias() {
  }

  public SolrAlias(String aliasName, List<String> collections) {
    this.name = aliasName;
    this.collections = collections;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getCollections() {
    return collections;
  }

  public void setCollections(List<String> collections) {
    this.collections = collections;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SolrAlias solrAlias = (SolrAlias) o;
    return Objects.equals(name, solrAlias.name) && Objects.equals(collections, solrAlias.collections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, collections);
  }
}
