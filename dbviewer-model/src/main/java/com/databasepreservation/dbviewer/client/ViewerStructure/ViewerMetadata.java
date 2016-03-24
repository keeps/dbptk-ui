package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerMetadata implements Serializable {
  private String name;

  // private String description;

  // private String archiver;

  // private String archiverContact;

  // private String dataOwner;

  // private String dataOriginTimespan;

  // private String lobFolder;

  // private String producerApplication;

  // private String archivalDate;

  // private String clientMachine;

  // private String databaseProduct;

  // private String databaseUser;

  private List<ViewerSchema> schemas;

  // private List<DbvUser> users;

  // private List<DbvRole> roles;

  // private List<DbvPrivilege> privileges;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ViewerSchema> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<ViewerSchema> schemas) {
    this.schemas = schemas;
  }
}
