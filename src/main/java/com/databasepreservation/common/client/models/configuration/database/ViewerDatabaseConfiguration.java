package com.databasepreservation.common.client.models.configuration.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"version", "id", "siard", "validation", "collections"})
public class ViewerDatabaseConfiguration implements Serializable {

  private String version = ViewerConstants.DATABASE_STATUS_VERSION;
  private String id;
  private ViewerSiardConfiguration viewerSiardConfiguration;
  private ViewerValidationConfiguration viewerValidationConfiguration;
  private List<String> collections;

  public ViewerDatabaseConfiguration() {
    collections = new ArrayList<>();
  }

  public ViewerDatabaseConfiguration(String version, String id, ViewerSiardConfiguration viewerSiardConfiguration,
    ViewerValidationConfiguration viewerValidationConfiguration, List<String> collections) {
    this.version = version;
    this.id = id;
    this.viewerSiardConfiguration = viewerSiardConfiguration;
    this.viewerValidationConfiguration = viewerValidationConfiguration;
    this.collections = collections;
  }

  public ViewerDatabaseConfiguration(ViewerDatabaseConfiguration status) {
    this.version = status.getVersion();
    this.id = status.getId();
    this.viewerSiardConfiguration = status.getViewerSiardConfiguration();
    this.viewerValidationConfiguration = status.getViewerValidationConfiguration();
    this.collections = status.getCollections();
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

  @JsonProperty("siard")
  public ViewerSiardConfiguration getViewerSiardConfiguration() {
    return viewerSiardConfiguration;
  }

  public void setViewerSiardConfiguration(ViewerSiardConfiguration viewerSiardConfiguration) {
    this.viewerSiardConfiguration = viewerSiardConfiguration;
  }

  @JsonProperty("validation")
  public ViewerValidationConfiguration getViewerValidationConfiguration() {
    return viewerValidationConfiguration;
  }

  public void setViewerValidationConfiguration(ViewerValidationConfiguration viewerValidationConfiguration) {
    this.viewerValidationConfiguration = viewerValidationConfiguration;
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
}
