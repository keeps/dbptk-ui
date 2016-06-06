package com.databasepreservation.dbviewer.client.ViewerStructure;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerMetadata implements Serializable {
  private String name;

  private String description;

  private String archiver;

  private String archiverContact;

  private String dataOwner;

  private String dataOriginTimespan;

  private String lobFolder;

  private String producerApplication;

  // this date is in UTC
  private String archivalDate;

  private String clientMachine;

  private String databaseProduct;

  private String databaseUser;

  private Map<String, ViewerSchema> schemas;

  private Map<String, ViewerTable> tables;

  // private List<DbvUser> users;

  // private List<DbvRole> roles;

  // private List<DbvPrivilege> privileges;

  // +++++++++++++++++++++++++++++++++++++++++++++++++++++
  // Getters and setters
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++

  public ViewerMetadata() {
    schemas = new HashMap<>();
    tables = new HashMap<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<ViewerSchema> getSchemas() {
    return schemas.values();
  }

  public void setSchemas(List<ViewerSchema> schemaList) {
    schemas = new HashMap<>();
    tables = new HashMap<>();
    for (ViewerSchema schema : schemaList) {
      schemas.put(schema.getUUID(), schema);
      for (ViewerTable table : schema.getTables()) {
        tables.put(table.getUUID(), table);
      }
    }
  }

  public String getArchivalDate() {
    return archivalDate;
  }

  public void setArchivalDate(String archivalDate) {
    this.archivalDate = archivalDate;
  }

  public String getArchiver() {
    return archiver;
  }

  public void setArchiver(String archiver) {
    this.archiver = archiver;
  }

  public String getArchiverContact() {
    return archiverContact;
  }

  public void setArchiverContact(String archiverContact) {
    this.archiverContact = archiverContact;
  }

  public String getClientMachine() {
    return clientMachine;
  }

  public void setClientMachine(String clientMachine) {
    this.clientMachine = clientMachine;
  }

  public String getDatabaseProduct() {
    return databaseProduct;
  }

  public void setDatabaseProduct(String databaseProduct) {
    this.databaseProduct = databaseProduct;
  }

  public String getDatabaseUser() {
    return databaseUser;
  }

  public void setDatabaseUser(String databaseUser) {
    this.databaseUser = databaseUser;
  }

  public String getDataOriginTimespan() {
    return dataOriginTimespan;
  }

  public void setDataOriginTimespan(String dataOriginTimespan) {
    this.dataOriginTimespan = dataOriginTimespan;
  }

  public String getDataOwner() {
    return dataOwner;
  }

  public void setDataOwner(String dataOwner) {
    this.dataOwner = dataOwner;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getProducerApplication() {
    return producerApplication;
  }

  public void setProducerApplication(String producerApplication) {
    this.producerApplication = producerApplication;
  }

  // +++++++++++++++++++++++++++++++++++++++++++++++++++++
  // Behaviour
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++

  public ViewerTable getTable(String tableUUID) {
    return tables.get(tableUUID);
  }

  public ViewerSchema getSchemaFromTableUUID(String tableUUID) {
    return getSchema(tables.get(tableUUID).getSchemaUUID());
  }

  public ViewerSchema getSchema(String schemaUUID) {
    return schemas.get(schemaUUID);
  }
}
