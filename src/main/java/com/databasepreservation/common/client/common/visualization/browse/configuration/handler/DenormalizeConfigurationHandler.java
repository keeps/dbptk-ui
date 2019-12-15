package com.databasepreservation.common.client.common.visualization.browse.configuration.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.widgets.Toast;
import org.fusesource.restygwt.client.Method;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.common.visualization.browse.configuration.TransformationTable;
import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.ReferencesConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.*;
import com.databasepreservation.common.client.services.ConfigurationService;
import com.google.gwt.core.client.GWT;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeConfigurationHandler {
  private static Map<String, DenormalizeConfigurationHandler> instances = new HashMap<>();
  private final String VERSION = "1.0.0";
  private ViewerDatabase database;
  private ViewerTable table;
  private DenormalizeConfiguration configuration;
  private CollectionConfigurationHandler collectionConfiguration;

  private enum State {
    CURRENT, DESIRED
  }

  /**
   * @param database
   * @return
   */
  public static DenormalizeConfigurationHandler getInstance(ViewerDatabase database, ViewerTable table) {
    return instances.computeIfAbsent(database.getUuid() + table.getUUID(),
      k -> new DenormalizeConfigurationHandler(database, table));
  }

  private DenormalizeConfigurationHandler(ViewerDatabase database, ViewerTable table) {
    this.database = database;
    this.table = table;
    this.collectionConfiguration = CollectionConfigurationHandler.getInstance(database);
  }

  public void getCollectionConfiguration(DefaultMethodCallback<Boolean> callback) {
    collectionConfiguration.getConfigurationFile(new DefaultMethodCallback<Boolean>() {
      @Override
      public void onSuccess(Method method, Boolean response) {
        getConfigurationFile(callback);
      }
    });
  }

  private void getConfigurationFile(DefaultMethodCallback<Boolean> callback) {
    ConfigurationService.Util.call(new DefaultMethodCallback<DenormalizeConfiguration>() {
      @Override
      public void onSuccess(Method method, DenormalizeConfiguration response) {
        if (response == null) {
          createConfiguration(State.CURRENT);
        } else {
          configuration = response;
        }
        callback.onSuccess(method, true);
      }
    }).getDenormalizeConfigurationFile(database.getUuid(), table.getUUID());
  }

  private void createConfiguration(State state) {
    configuration = new DenormalizeConfiguration(database.getUuid(), table);
    configuration.setVersion(VERSION);
    configuration.setState(state.name());
    collectionConfiguration.addTable(table);
    collectionConfiguration.getTableByID(table.getUUID()).setRelatedTables(configuration.getUuid());
  }

  public void build() {
    ConfigurationService.Util.call((Boolean result) -> {
      GWT.log("Created denormalization configuration file with success");
      collectionConfiguration.build();
    }).createDenormalizeConfigurationFile(database.getUuid(), table.getUUID(), configuration);
  }

  public ViewerTable getTable(){
    return table;
  }

  public List<RelatedTablesConfiguration> getRelatedTableList(){
    return configuration.getRelatedTables();
  }

  public String includeRelatedTable(ViewerTable sourceTable, ViewerTable referencedTable, ViewerForeignKey foreignKey) {
    RelatedTablesConfiguration relatedTable = new RelatedTablesConfiguration();
    relatedTable.setTableUUID(sourceTable.getUUID());
    relatedTable.setTableID(sourceTable.getId());
    relatedTable.setReferencedTableUUID(referencedTable.getUUID());
    relatedTable.setReferencedTableID(referencedTable.getId());

    for (ViewerReference reference : foreignKey.getReferences()) {
      if (foreignKey.getReferencedTableUUID().equals(sourceTable.getUUID())) {
        ViewerColumn sourceColumn = sourceTable.getColumns().get(reference.getReferencedColumnIndex());
        ViewerColumn referencedColumn = referencedTable.getColumns().get(reference.getSourceColumnIndex());
        relatedTable.getReferences().add(createReference(sourceColumn, referencedColumn));
      } else {
        ViewerColumn sourceColumn = sourceTable.getColumns().get(reference.getSourceColumnIndex());
        ViewerColumn referencedColumn = referencedTable.getColumns().get(reference.getReferencedColumnIndex());
        relatedTable.getReferences().add(createReference(sourceColumn, referencedColumn));
      }
    }

    StringBuilder uuid = new StringBuilder(sourceTable.getUUID());
    uuid.append(".").append(referencedTable.getUUID());
    for (ViewerReference reference : foreignKey.getReferences()) {
      uuid.append(".").append(reference.getSourceColumnIndex());
    }

    relatedTable.setUuid(uuid.toString());
    if (getRelatedTableByUUID(relatedTable.getUuid()) != null) {
      removeRelatedTableByUUID(relatedTable.getUuid());
    }
    configuration.getRelatedTables().add(relatedTable);
    return uuid.toString();
  }

  private ReferencesConfiguration createReference(ViewerColumn sourceColumn, ViewerColumn referencedColumn) {
    ReferencesConfiguration references = new ReferencesConfiguration();
    RelatedColumnConfiguration sourceColumnTable = new RelatedColumnConfiguration();
    RelatedColumnConfiguration referencedColumnTable = new RelatedColumnConfiguration();

    sourceColumnTable.setSolrName(sourceColumn.getSolrName());
    sourceColumnTable.setColumnName(sourceColumn.getDisplayName());
    sourceColumnTable.setIndex(sourceColumn.getColumnIndexInEnclosingTable());

    referencedColumnTable.setSolrName(referencedColumn.getSolrName());
    referencedColumnTable.setColumnName(referencedColumn.getDisplayName());
    referencedColumnTable.setIndex(referencedColumn.getColumnIndexInEnclosingTable());

    references.setSourceTable(sourceColumnTable);
    references.setReferencedTable(referencedColumnTable);

    return references;
  }

  public RelatedTablesConfiguration getRelatedTableByRelationship(String sourceTableUUID, String referencedTableUUID,
    ViewerForeignKey foreignKey) {
    StringBuilder uuid = new StringBuilder(sourceTableUUID);
    uuid.append(".").append(referencedTableUUID);

    for (ViewerReference reference : foreignKey.getReferences()) {
      uuid.append(".").append(reference.getSourceColumnIndex());
    }

    GWT.log("UUID: " + uuid);
    return getRelatedTableByUUID(uuid.toString());
  }

  public RelatedTablesConfiguration getRelatedTableByUUID(String uuid) {
    for (RelatedTablesConfiguration relatedTable : configuration.getRelatedTables()) {
      if (relatedTable.getUuid().equals(uuid)) {
        return relatedTable;
      }
    }
    return null;
  }

  //TODO: Cascate delete, implement tree and delete all child
  public void removeRelatedTableByUUID(String uuid) {
    for (RelatedTablesConfiguration relatedTables : configuration.getRelatedTables()) {
      if (relatedTables.getUuid().equals(uuid)) {
        configuration.getRelatedTables().remove(relatedTables);
        break;
      }
    }
  }

  public void addColumnToInclude(String uuid, ViewerColumn object) {
    RelatedTablesConfiguration relatedTable = getRelatedTableByUUID(uuid);
    RelatedColumnConfiguration column = new RelatedColumnConfiguration();
    column.setIndex(object.getColumnIndexInEnclosingTable());
    column.setColumnName(object.getDisplayName());
    column.setSolrName(object.getSolrName());

    //avoid to include the same column twice
    for (RelatedColumnConfiguration columnToInclude : relatedTable.getColumnsIncluded()) {
      if (columnToInclude.getIndex() == column.getIndex()) {
        return;
      }
    }
    relatedTable.getColumnsIncluded().add(column);
    TransformationTable.getInstance(database, table, this).redrawTable();
    int nestedIndex = relatedTable.getNestedIndex();
    relatedTable.getDisplaySettings()
      .setNestedSolrName(ViewerConstants.SOLR_INDEX_ROW_NESTED_COLUMN_NAME_PREFIX + nestedIndex + "_txt");
    relatedTable.setNestedIndex(nestedIndex + 1);
  }

  public void removeColumnToInclude(String uuid, ViewerColumn object) {
    RelatedTablesConfiguration relatedTable = getRelatedTableByUUID(uuid);
    List<RelatedColumnConfiguration> columnsIncluded = relatedTable.getColumnsIncluded();

    for (RelatedColumnConfiguration column : columnsIncluded) {
      if (column.getIndex() == object.getColumnIndexInEnclosingTable()) {
        columnsIncluded.remove(column);
        break;
      }
    }
    TransformationTable.getInstance(database, table, this).redrawTable();
  }

  public List<ViewerColumn> getAllColumnsToInclude() {
    List<ViewerColumn> columnList = new ArrayList<>();

    for (RelatedTablesConfiguration relatedTable : configuration.getRelatedTables()) {
      ViewerTable referencedTable = database.getMetadata().getTable(relatedTable.getTableUUID());
      for (RelatedColumnConfiguration columnsIncluded : relatedTable.getColumnsIncluded()) {
        ViewerColumn column = referencedTable.getColumns().get(columnsIncluded.getIndex());
        column.setSolrName(relatedTable.getDisplaySettings().getNestedSolrName());
        columnList.add(column);
      }
    }

    return columnList;
  }
}
