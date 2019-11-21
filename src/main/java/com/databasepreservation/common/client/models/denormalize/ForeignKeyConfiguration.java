package com.databasepreservation.common.client.models.denormalize;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ForeignKeyConfiguration implements Serializable {
  private String tableUUID;
  private ReferencedConfiguration referenced;
  private ReferenceConfiguration reference;

  public String getTableUUID() {
    return tableUUID;
  }

  public void setTableUUID(String tableUUID) {
    this.tableUUID = tableUUID;
  }

  public ReferencedConfiguration getReferenced() {
    return referenced;
  }

  public void setReferenced(ReferencedConfiguration referenced) {
    this.referenced = referenced;
  }

  public ReferenceConfiguration getReference() {
    return reference;
  }

  public void setReference(ReferenceConfiguration reference) {
    this.reference = reference;
  }
}
