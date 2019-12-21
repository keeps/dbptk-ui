package com.databasepreservation.common.client.common.visualization.browse.configuration.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.models.configuration.collection.TableConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.Method;

import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.ConfigurationService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CollectionConfigurationHandler {
  private static Map<String, CollectionConfigurationHandler> instances = new HashMap<>();
  private final String VERSION = "1.0.0";
  private ViewerDatabase database;
  private CollectionConfiguration configuration;

  /**
   *
   * @param database
   * @return
   */
  public static CollectionConfigurationHandler getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new CollectionConfigurationHandler(database));
  }

  /**
   *
   * @param database
   */
  private CollectionConfigurationHandler(ViewerDatabase database) {
    this.database = database;
  }

  /**
   *
   * @param callback
   */
  public void getConfigurationFile(DefaultMethodCallback<Boolean> callback) {
    ConfigurationService.Util.call(new DefaultMethodCallback<CollectionConfiguration>() {
      @Override
      public void onSuccess(Method method, CollectionConfiguration response) {
        if (response == null) {
          createConfiguration();
        } else {
          configuration = response;
        }
        callback.onSuccess(method, true);
      }
    }).getConfigurationFile(database.getUuid());
  }

  /**
   *
   */
  private void createConfiguration() {
    configuration = new CollectionConfiguration(database);
    configuration.setVersion(VERSION);
    build();
  }

  public void build(){
    ConfigurationService.Util.call((Boolean result) -> {
      GWT.log("Created collection configuration file with success");
      Dialogs.showInformationDialog("Configuration file", "Created denormalization configuration file with success", "OK");
    }).createConfigurationFile(database.getUuid(), configuration);
  }

  public List<TableConfiguration> getTables(){
    return configuration.getTables();
  }
  /**
   *
   * @param tableUUID
   * @return
   */
  public TableConfiguration getTableByID(String tableUUID){
    for (TableConfiguration tableConfiguration: configuration.getTables()) {
      if(tableConfiguration.getUuid().equals(tableUUID)){
        GWT.log("Found table in configuration file: " + tableConfiguration.getName());
        return tableConfiguration;
      }
    }
    return null;
  }

  /**
   *
   * @param table
   */
  public void addTable(ViewerTable table){
    if(getTableByID(table.getUuid()) == null){
      configuration.getTables().add(new TableConfiguration(table));
    } else {
      GWT.log("Table already exist: " + table.getName());
    }
  }

  /**
   *
   * @param tableUUID
   */
  public void removeTable(String tableUUID){
    if(getTableByID(tableUUID) != null){
      configuration.getTables().removeIf(t -> t.getUuid().equals(tableUUID));
    }
  }
}
