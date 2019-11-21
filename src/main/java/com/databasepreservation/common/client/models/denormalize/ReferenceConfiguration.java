package com.databasepreservation.common.client.models.denormalize;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ReferenceConfiguration implements Serializable {
  private Long columnIndexInEnclosingTable;
  private String solrName;

  public Long getColumnIndexInEnclosingTable() {
    return columnIndexInEnclosingTable;
  }

  public void setColumnIndexInEnclosingTable(Long columnIndexInEnclosingTable) {
    this.columnIndexInEnclosingTable = columnIndexInEnclosingTable;
  }

  public String getSolrName() {
    return solrName;
  }

  public void setSolrName(String solrName) {
    this.solrName = solrName;
  }
}
