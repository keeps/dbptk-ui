/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@XmlRootElement(name = "metadata")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ViewerMetadata implements Serializable {
  @Serial
  private static final long serialVersionUID = -2637918454047613842L;
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

  private List<ViewerSchema> schemas;

  @JsonIgnore
  private Map<String, ViewerSchema> schemasMap;

  @JsonIgnore
  private Map<String, ViewerTable> tables;

  @JsonIgnore
  private Map<String, ViewerView> views;

  @JsonIgnore
  private Map<String, ViewerRoutine> routines;

  private List<ViewerUserStructure> users;

  private List<ViewerRoleStructure> roles;

  private List<ViewerPrivilegeStructure> privileges;

  // +++++++++++++++++++++++++++++++++++++++++++++++++++++
  // Getters and setters
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++

  public ViewerMetadata() {
    schemasMap = new LinkedHashMap<>();
    tables = new LinkedHashMap<>();
    views = new LinkedHashMap<>();
    routines = new LinkedHashMap<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ViewerSchema> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<ViewerSchema> schemaList) {
    this.schemas = schemaList;
    schemasMap = new LinkedHashMap<>();
    tables = new LinkedHashMap<>();
    views = new LinkedHashMap<>();
    routines = new LinkedHashMap<>();

    for (ViewerSchema schema : schemaList) {
      schemasMap.put(schema.getUuid(), schema);
      for (ViewerTable table : schema.getTables()) {
        tables.put(table.getUuid(), table);
      }
      for (ViewerView view : schema.getViews()) {
        views.put(view.getUuid(), view);
      }
      for (ViewerRoutine routine : schema.getRoutines()) {
        routines.put(routine.getUuid(), routine);
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

  public List<ViewerUserStructure> getUsers() {
    return users;
  }

  public void setUsers(List<ViewerUserStructure> users) {
    this.users = users;
  }

  public List<ViewerRoleStructure> getRoles() {
    return roles;
  }

  public void setRoles(List<ViewerRoleStructure> roles) {
    this.roles = roles;
  }

  public List<ViewerPrivilegeStructure> getPrivileges() {
    return privileges;
  }

  public void setPrivileges(List<ViewerPrivilegeStructure> privileges) {
    this.privileges = privileges;
  }

  // +++++++++++++++++++++++++++++++++++++++++++++++++++++
  // Behaviour
  // +++++++++++++++++++++++++++++++++++++++++++++++++++++

  public ViewerTable getTable(String tableUUID) {
    return tables.get(tableUUID);
  }

  public Map<String, ViewerTable> getTables() {
    return tables;
  }

  public ViewerTable getTableById(String tableId) {
    for (ViewerTable value : tables.values()) {
      if (value.getId().equals(tableId))
        return value;
    }
    return null;
  }

  public ViewerView getView(String viewUUID) {
    return views.get(viewUUID);
  }

  public Map<String, ViewerView> getViews() {
    return views;
  }

  public ViewerRoutine getRoutine(String routineUUID) {
    return routines.get(routineUUID);
  }

  public ViewerSchema getSchemaFromTableUUID(String tableUUID) {
    return getSchema(tables.get(tableUUID).getSchemaUUID());
  }

  public ViewerSchema getSchemaFromViewUUID(String viewUUID) {
    return getSchema(views.get(viewUUID).getSchemaUUID());
  }

  public ViewerSchema getSchema(String schemaUUID) {
    return schemasMap.get(schemaUUID);
  }

  @JsonIgnore
  public ViewerSchema getSchemaByName(String schemaName) {
    for (ViewerSchema schema : schemas) {
      if (schemaName.equals(schema.getName())) {
        return schema;
      }
    }

    return null;
  }

  @JsonIgnore
  public int getSchemaIndex(String schemaUUID) {
    int i = 1;

    for (Map.Entry<String, ViewerSchema> next : schemasMap.entrySet()) {
      if (next.getKey().equals(schemaUUID))
        return i;
      i++;
    }

    return -1;
  }

  @JsonIgnore
  public int getTableIndex(String tableUUID) {
    int i = 1;

    for (Map.Entry<String, ViewerTable> next : tables.entrySet()) {
      if (next.getKey().equals(tableUUID))
        return i;
      i++;
    }

    return -1;
  }

  public Map<String, ViewerRoutine> getRoutines() {
    return routines;
  }
}
