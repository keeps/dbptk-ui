package com.databasepreservation.common.client.common.visualization.browse.configuration.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.browse.configuration.TableNode;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.TableConfiguration;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.ConfigurationService;
import com.google.gwt.core.client.GWT;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationHandler {
  private static Map<String, ConfigurationHandler> instances = new HashMap<>();
  private final String VERSION = "1.0.0";
  private ViewerDatabase database;
  private CollectionConfiguration collectionConfiguration;
  private DenormalizeConfiguration denormalizeConfiguration;
  private Map<String, Integer> nestedIndexMap = new HashMap<>();

  /**
   *
   * @param database
   * @return
   */
  public static ConfigurationHandler getInstance(ViewerDatabase database,
    CollectionConfiguration collectionConfiguration) {
    return instances.computeIfAbsent(database.getUuid(),
      k -> new ConfigurationHandler(database, collectionConfiguration));
  }

  public static ConfigurationHandler getInstance(ViewerDatabase database, DenormalizeConfiguration denormalizeConfiguration){
    return instances.computeIfAbsent(database.getUuid(),
        k -> new ConfigurationHandler(database, denormalizeConfiguration));
  }

  /**
   *
   * @param database
   */
  private ConfigurationHandler(ViewerDatabase database, CollectionConfiguration collectionConfiguration) {
    this.database = database;
    if (collectionConfiguration == null) {
      createConfiguration();
    } else {
      this.collectionConfiguration = collectionConfiguration;
    }
  }

  private ConfigurationHandler(ViewerDatabase database, DenormalizeConfiguration denormalizeConfiguration) {
    this.database = database;
    this.denormalizeConfiguration = denormalizeConfiguration;
  }

  /**
   * 
   */
  private void createConfiguration() {
    collectionConfiguration = new CollectionConfiguration(database);
    collectionConfiguration.setVersion(VERSION);
  }

  /**
   * 
   */
  public void buildAll() {
    for (TableConfiguration table : collectionConfiguration.getTables()) {
      addDenormalizationConfiguration(table.getDenormalizeConfiguration());
    }
  }

  public void addDenormalizationConfiguration(DenormalizeConfiguration denormalizeConfiguration){
    ConfigurationService.Util.call((Boolean result) -> {
      Dialogs.showInformationDialog("Configuration file", "Created denormalization configuration file with success",
          "OK");
    }).createDenormalizeConfigurationFile(database.getUuid(), denormalizeConfiguration.getTableUUID(), denormalizeConfiguration);
  }

  /**
   * 
   * @return
   */
  public List<TableConfiguration> getTables() {
    return collectionConfiguration.getTables();
  }

  /**
   *
   * @return
   */
  public TableConfiguration getTableConfiguration(ViewerTable table) {
    for (TableConfiguration tableJSONConfiguration : collectionConfiguration.getTables()) {
      if (tableJSONConfiguration.getUuid().equals(table.getUuid())) {
        GWT.log("Found table in configuration file: " + tableJSONConfiguration.getName());
        return tableJSONConfiguration;
      }
    }
    TableConfiguration tableConfiguration = new TableConfiguration(table);
    tableConfiguration.setDenormalizeConfiguration(createDenormalizeConfiguration(table));
    collectionConfiguration.getTables().add(tableConfiguration);
    return tableConfiguration;
  }

  /**
   *
   * @param table
   * @return
   */
  private DenormalizeConfiguration createDenormalizeConfiguration(ViewerTable table) {
    DenormalizeConfiguration denormalizeConfiguration = new DenormalizeConfiguration(database.getUuid(), table);
    update(denormalizeConfiguration);
    denormalizeConfiguration.setVersion(VERSION);
    return denormalizeConfiguration;
  }

  /**
   *
   * @param tableUUID
   */
  public void removeTable(String tableUUID) {
    collectionConfiguration.getTables().removeIf(t -> t.getUuid().equals(tableUUID));
  }

  /**
   *
   * @param table
   * @return
   */
  public DenormalizeConfiguration getDenormalizeConfiguration(ViewerTable table) {
    TableConfiguration targetTable = getTableConfiguration(table);
    DenormalizeConfiguration denormalizeConfiguration = targetTable.getDenormalizeConfiguration();
    return denormalizeConfiguration;
  }

  public void removeRelatedTable(TableNode childNode, ViewerTable table) {
    Map<ViewerForeignKey, TableNode> children = childNode.getChildren();

    TableConfiguration tableConfiguration = getTableConfiguration(table);
    tableConfiguration.getDenormalizeConfiguration().getRelatedTables()
      .removeIf(t -> t.getUuid().equals(childNode.getUuid()));

    if (children == null || children.isEmpty()) {
      return;
    }

    for (Map.Entry<ViewerForeignKey, TableNode> entry : children.entrySet()) {
      removeRelatedTable(entry.getValue(), table);
    }
    update(tableConfiguration.getDenormalizeConfiguration());
  }

  /**
   *
   */
  public void includeRelatedTable(TableNode childNode, ViewerTable targetTable) {
    TableNode parentNode = childNode.getParentNode();
    ViewerTable sourceTable = childNode.getTable();
    ViewerTable referencedTable = parentNode.getTable();
    ViewerForeignKey foreignKey = childNode.getForeignKey();
    ViewerColumn sourceColumn;
    ViewerColumn referencedColumn;

    RelatedTablesConfiguration relatedTable = new RelatedTablesConfiguration();
    relatedTable.setUuid(childNode.getUuid());
    relatedTable.setTableUUID(sourceTable.getUuid());
    relatedTable.setTableID(sourceTable.getId());
    relatedTable.setReferencedTableUUID(referencedTable.getUuid());
    relatedTable.setReferencedTableID(referencedTable.getId());

    for (ViewerReference reference : foreignKey.getReferences()) {
      if (foreignKey.getReferencedTableUUID().equals(referencedTable.getUuid())) {
        sourceColumn = sourceTable.getColumns().get(reference.getSourceColumnIndex());
        referencedColumn = referencedTable.getColumns().get(reference.getReferencedColumnIndex());
      } else {
        sourceColumn = sourceTable.getColumns().get(reference.getReferencedColumnIndex());
        referencedColumn = referencedTable.getColumns().get(reference.getSourceColumnIndex());
      }
      relatedTable.getReferences().add(createReference(sourceColumn, referencedColumn));
    }

    DenormalizeConfiguration denormalizeConfiguration = getDenormalizeConfiguration(targetTable);
    List<RelatedTablesConfiguration> relatedTables = denormalizeConfiguration.getRelatedTables();

    if (relatedTables != null && !relatedTables.isEmpty()) {
      for (RelatedTablesConfiguration table : denormalizeConfiguration.getRelatedTables()) {
        if (table.getUuid().equals(relatedTable.getUuid())) {
          return;
        }
      }
    }

    denormalizeConfiguration.getRelatedTables().add(relatedTable);
    update(denormalizeConfiguration);
    if (nestedIndexMap.get(targetTable.getUuid()) == null) {
      nestedIndexMap.put(targetTable.getUuid(), 0);
    } else {
      nestedIndexMap.put(targetTable.getUuid(), nestedIndexMap.get(targetTable.getUuid()) + 1);
    }

    relatedTable.getDisplaySettings().setNestedSolrName(ViewerConstants.SOLR_INDEX_ROW_NESTED_COLUMN_NAME_PREFIX
      + nestedIndexMap.get(targetTable.getUuid()) + ViewerConstants.SOLR_DYN_NEST_MULTI);
  }

  /**
   * 
   * @param sourceColumn
   * @param referencedColumn
   * @return
   */
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

  /**
   * 
   * @param table
   * @return
   */
  public List<RelatedTablesConfiguration> getRelatedTableList(ViewerTable table) {
    TableConfiguration tableConfiguration = getTableConfiguration(table);
    return tableConfiguration.getDenormalizeConfiguration().getRelatedTables();
  }

  /**
   * 
   * @param uuid
   * @return
   */
  public RelatedTablesConfiguration getRelatedTable(String uuid) {
    for (TableConfiguration table : collectionConfiguration.getTables()) {
      for (RelatedTablesConfiguration relatedTable : table.getDenormalizeConfiguration().getRelatedTables()) {
        if (relatedTable.getUuid().equals(uuid)) {
          return relatedTable;
        }
      }
    }
    return null;
  }

  /**
   * 
   * @param uuid
   * @param object
   */
  public void addColumnToInclude(String uuid, ViewerColumn object) {
    RelatedTablesConfiguration relatedTable = getRelatedTable(uuid);
    RelatedColumnConfiguration column = new RelatedColumnConfiguration();
    column.setIndex(object.getColumnIndexInEnclosingTable());
    column.setColumnName(object.getDisplayName());
    column.setSolrName(object.getSolrName());

    // avoid to include the same column twice
    for (RelatedColumnConfiguration columnToInclude : relatedTable.getColumnsIncluded()) {
      if (columnToInclude.getIndex() == column.getIndex()) {
        return;
      }
    }
    relatedTable.getColumnsIncluded().add(column);
  }

  /**
   * 
   * @param uuid
   * @param object
   */
  public void removeColumnToInclude(String uuid, ViewerColumn object) {
    RelatedTablesConfiguration relatedTable = getRelatedTable(uuid);
    if (relatedTable != null) {
      List<RelatedColumnConfiguration> columnsIncluded = relatedTable.getColumnsIncluded();

      for (RelatedColumnConfiguration column : columnsIncluded) {
        if (column.getIndex() == object.getColumnIndexInEnclosingTable()) {
          columnsIncluded.remove(column);
          break;
        }
      }
    }
  }

  public Map<String, List<ViewerColumn>> getAllColumnsToInclude(ViewerTable table) {
    Map<String, List<ViewerColumn>> columnsToIncludeMap = new HashMap<>();
    TableConfiguration tableConfiguration = getTableConfiguration(table);
    List<RelatedTablesConfiguration> relatedList = tableConfiguration.getDenormalizeConfiguration().getRelatedTables();
    for (RelatedTablesConfiguration relatedTable : relatedList) {
      ViewerTable referencedTable = database.getMetadata().getTable(relatedTable.getTableUUID());
      columnsToIncludeMap.computeIfAbsent(referencedTable.getId(), k -> new ArrayList<>());
      for (RelatedColumnConfiguration columnsIncluded : relatedTable.getColumnsIncluded()) {
        ViewerColumn column = referencedTable.getColumns().get(columnsIncluded.getIndex());
        columnsToIncludeMap.get(referencedTable.getId()).add(column);
      }
    }
    return columnsToIncludeMap;
  }

  public void update(DenormalizeConfiguration denormalizeConfiguration) {
    denormalizeConfiguration.setState(ViewerJobStatus.NEW);
  }
}
