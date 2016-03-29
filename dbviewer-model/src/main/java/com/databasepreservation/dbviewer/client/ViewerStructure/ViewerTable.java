package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTable implements Serializable, IsIndexed {
  // used to identify the collection containing data from this table
  private String uuid;

  private String name;

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
