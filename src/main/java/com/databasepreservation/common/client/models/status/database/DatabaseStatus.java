/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"version", "id", "siard", "validation", "collections", "availableToSearchAll"})
public class DatabaseStatus implements Serializable {

  private String version = ViewerConstants.DATABASE_STATUS_VERSION;
  private String id;
  private SiardStatus siardStatus;
  private ValidationStatus validationStatus;
  private List<String> collections;
  private Map<String, AuthorizationDetails> permissions;
  private boolean availableToSearchAll;

  public DatabaseStatus() {
    collections = new ArrayList<>();
    permissions = new HashMap<>();
    availableToSearchAll = true;
  }

  public DatabaseStatus(String version, String id, SiardStatus siardStatus, ValidationStatus validationStatus,
    List<String> collections, Map<String, AuthorizationDetails> permissions, boolean availableToSearchAll) {
    this.version = version;
    this.id = id;
    this.siardStatus = siardStatus;
    this.validationStatus = validationStatus;
    this.collections = collections;
    this.permissions = permissions;
    this.availableToSearchAll = availableToSearchAll;
  }

  public DatabaseStatus(DatabaseStatus status, Map<String, AuthorizationDetails> permissions) {
    this.version = status.getVersion();
    this.id = status.getId();
    this.siardStatus = status.getSiardStatus();
    this.validationStatus = status.getValidationStatus();
    this.collections = status.getCollections();
    this.permissions = status.getPermissions();
    this.availableToSearchAll = status.isAvailableToSearchAll();
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isAvailableToSearchAll() {
    return availableToSearchAll;
  }

  public void setAvailableToSearchAll(boolean availableToSearchAll) {
    this.availableToSearchAll = availableToSearchAll;
  }

  @JsonProperty("siard")
  public SiardStatus getSiardStatus() {
    return siardStatus;
  }

  public void setSiardStatus(SiardStatus siardStatus) {
    this.siardStatus = siardStatus;
  }

  @JsonProperty("validation")
  public ValidationStatus getValidationStatus() {
    return validationStatus;
  }

  public void setValidationStatus(ValidationStatus validationStatus) {
    this.validationStatus = validationStatus;
  }

  public List<String> getCollections() {
    return collections;
  }

  public void setCollections(List<String> collections) {
    this.collections = collections;
  }

  public void addBrowseCollection(String collection) {
    this.collections.add(collection);
  }

  public Map<String, AuthorizationDetails> getPermissions() {
    return permissions;
  }

  public void setPermissions(Map<String, AuthorizationDetails> permissions) {
    this.permissions = permissions;
  }
}
