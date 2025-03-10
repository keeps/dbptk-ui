/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists.pagination;

import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;

import java.io.Serializable;

public class ListSelectionState<T extends IsIndexed> implements Serializable {
  private static final long serialVersionUID = 1L;

  private T selected;
  private Filter filter;
  private Boolean justActive;
  private Facets facets;
  private Sorter sorter;
  private Integer index;
  private Long total;

  public ListSelectionState() {
    super();
  }

  public ListSelectionState(T selected, Filter filter, Boolean justActive, Facets facets, Sorter sorter, Integer index,
                            Long total) {
    super();
    this.selected = selected;
    this.filter = filter;
    this.justActive = justActive;
    this.facets = facets;
    this.sorter = sorter;
    this.index = index;
    this.total = total;
  }

  public T getSelected() {
    return selected;
  }

  public void setSelected(T selected) {
    this.selected = selected;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public Boolean getJustActive() {
    return justActive;
  }

  public void setJustActive(Boolean justActive) {
    this.justActive = justActive;
  }

  public Facets getFacets() {
    return facets;
  }

  public void setFacets(Facets facets) {
    this.facets = facets;
  }

  public Sorter getSorter() {
    return sorter;
  }

  public void setSorter(Sorter sorter) {
    this.sorter = sorter;
  }

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ListSelectionState [");
    if (selected != null) {
      builder.append("selected=");
      builder.append(selected);
      builder.append(", ");
    }
    if (filter != null) {
      builder.append("filter=");
      builder.append(filter);
      builder.append(", ");
    }
    if (justActive != null) {
      builder.append("justActive=");
      builder.append(justActive);
      builder.append(", ");
    }
    if (facets != null) {
      builder.append("facets=");
      builder.append(facets);
      builder.append(", ");
    }
    if (sorter != null) {
      builder.append("sorter=");
      builder.append(sorter);
      builder.append(", ");
    }
    if (index != null) {
      builder.append("index=");
      builder.append(index);
    }
    if (total != null) {
      builder.append("total=");
      builder.append(total);
    }
    builder.append("]");
    return builder.toString();
  }

}
