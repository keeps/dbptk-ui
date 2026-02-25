/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation.TableNode;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationUtils {
  private DataTransformationUtils() {
  }

  public static void includeRelatedTable(TableNode childNode, DenormalizeConfiguration denormalizeConfiguration,
    CollectionStatus collectionStatus) {
    TableNode parentNode = childNode.getParentNode();
    ViewerTable sourceTable = childNode.getTable();
    ViewerTable referencedTable = parentNode.getTable();
    ViewerForeignKey foreignKey = childNode.getForeignKey();
    ViewerColumn sourceColumn;
    ViewerColumn referencedColumn;

    RelatedTablesConfiguration relatedTable = new RelatedTablesConfiguration();
    relatedTable.setUuid(childNode.getUuid());
    relatedTable.setMultiValue(childNode.getMultiValue());
    relatedTable.setTableUUID(sourceTable.getUuid());
    relatedTable.setTableID(sourceTable.getId());
    relatedTable.setReferencedTableUUID(referencedTable.getUuid());
    relatedTable.setReferencedTableID(referencedTable.getId());

    List<ViewerColumn> allSourceColumns = getViewerColumnsWithVirtualColumns(sourceTable.getColumns(),
      collectionStatus.getTableStatusByTableId(sourceTable.getId()));
    List<ViewerColumn> allReferencedColumns = getViewerColumnsWithVirtualColumns(referencedTable.getColumns(),
      collectionStatus.getTableStatusByTableId(referencedTable.getId()));

    for (ViewerReference reference : foreignKey.getReferences()) {
      if (foreignKey.getReferencedTableUUID().equals(referencedTable.getUuid())) {
        sourceColumn = allSourceColumns.get(reference.getSourceColumnIndex());
        referencedColumn = allReferencedColumns.get(reference.getReferencedColumnIndex());
      } else {
        sourceColumn = allSourceColumns.get(reference.getReferencedColumnIndex());
        referencedColumn = allReferencedColumns.get(reference.getSourceColumnIndex());
      }
      relatedTable.getReferences().add(createReference(sourceColumn, referencedColumn));
    }

    RelatedTablesConfiguration returnedRelatedTable = denormalizeConfiguration
      .getRelatedTable(childNode.getParentNode().getUuid());
    if (returnedRelatedTable == null) {
      denormalizeConfiguration.addRelatedTable(relatedTable);
    } else {
      returnedRelatedTable.addRelatedTable(relatedTable);
    }
  }

  private static ReferencesConfiguration createReference(ViewerColumn sourceColumn, ViewerColumn referencedColumn) {
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

  public static void removeRelatedTable(TableNode childNode, DenormalizeConfiguration denormalizeConfiguration) {
    denormalizeConfiguration.setState(ViewerJobStatus.NEW);
    Map<ViewerForeignKey, TableNode> children = childNode.getChildren();
    denormalizeConfiguration.getRelatedTables().removeIf(t -> t.getUuid().equals(childNode.getUuid()));
    if (children == null || children.isEmpty()) {
      return;
    }

    for (Map.Entry<ViewerForeignKey, TableNode> entry : children.entrySet()) {
      removeRelatedTable(entry.getValue(), denormalizeConfiguration);
    }
  }

  public static void addColumnToInclude(String uuid, ViewerColumn columnToInclude,
    DenormalizeConfiguration denormalizeConfiguration) {
    RelatedTablesConfiguration relatedTable = denormalizeConfiguration.getRelatedTable(uuid);

    if (relatedTable != null) {
      for (RelatedColumnConfiguration checkColumn : relatedTable.getColumnsIncluded()) {
        if (checkColumn.getIndex() == columnToInclude.getColumnIndexInEnclosingTable()) {
          return;
        }
      }
    }
    relatedTable.addColumnToInclude(columnToInclude);
    denormalizeConfiguration.setState(ViewerJobStatus.NEW);
  }

  public static void removeColumnToInclude(String uuid, ViewerColumn columnToRemove,
    DenormalizeConfiguration denormalizeConfiguration) {
    RelatedTablesConfiguration relatedTable = denormalizeConfiguration.getRelatedTable(uuid);
    if (relatedTable != null) {
      List<RelatedColumnConfiguration> columnsIncluded = relatedTable.getColumnsIncluded();

      for (RelatedColumnConfiguration column : columnsIncluded) {
        if (column.getIndex() == columnToRemove.getColumnIndexInEnclosingTable()) {
          columnsIncluded.remove(column);
          denormalizeConfiguration.setState(ViewerJobStatus.NEW);
          break;
        }
      }
    }
  }

  public static void buildNestedFieldsToReturn(ViewerTable table, CollectionStatus status,
    Map<String, String> extraParameters, List<String> fieldsToReturn) {
    TableStatus tableStatus = status.getTableStatus(table.getUuid());
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    int nestedCount = 0;
    String keys = "";
    String separator = "";
    for (ColumnStatus column : tableStatus.getColumns()) {
      if (column.getNestedColumns() != null) {
        String nestedTableId = column.getId();
        String key = ViewerConstants.SOLR_ROWS_NESTED + "." + nestedCount;
        keys = keys + separator + key;
        separator = ",";
        addDefaultNestedFieldsToReturn(fieldsToReturn);
        fieldsToReturn.add(key + ":[subquery]");
        addNestedDefaultFieldList(extraParameters, key);
        extraParameters.put(key + ".q", "+nestedUUID:" + nestedTableId + " AND {!terms f=_root_ v=$row.uuid}");
        Integer quantity = column.getNestedColumns().getQuantityInList();
        if (quantity <= column.getNestedColumns().getMaxQuantityInList()) {
          extraParameters.put(key + ".rows", quantity.toString());
        } else {
          extraParameters.put(key + ".rows", column.getNestedColumns().getMaxQuantityInList().toString());
        }
        nestedCount++;
      }
    }
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_NESTED + ":" + "\"" + keys + "\"");
  }

  public static void addNestedDefaultFieldList(Map<String, String> extraParameters, String key) {
    extraParameters.put(key + ".fl", ViewerConstants.SOLR_ROWS_NESTED + "*," + ViewerConstants.INDEX_ID + ","
      + ViewerConstants.SOLR_ROWS_NESTED_COL + "*," + "originalRowUUID_t");
  }

  public static void addDefaultNestedFieldsToReturn(List<String> fieldsToReturn) {
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_NESTED + "*");
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_NESTED_COL + "*");
    fieldsToReturn.add("originalRowUUID_t");
  }

  // Virtual
  @NotNull
  public static List<ViewerColumn> getViewerColumnsWithVirtualColumns(List<ViewerColumn> originalColumns,
    TableStatus tableStatus) {
    List<ViewerColumn> allViewerColumns = new ArrayList<>(originalColumns);
    List<ColumnStatus> virtualColumnStatus = tableStatus.getColumns().stream()
      .filter(column -> column.getType().equals(ViewerType.dbTypes.VIRTUAL)).collect(Collectors.toList());

    ArrayList<ViewerColumn> virtualViewerColumns = virtualColumnStatus.stream()
      .map(DataTransformationUtils::convertToViewerColumn).collect(Collectors.toCollection(ArrayList::new));

    allViewerColumns.addAll(virtualViewerColumns);
    return allViewerColumns;
  }

  public static ViewerColumn convertToViewerColumn(ColumnStatus columnStatus) {
    ViewerColumn viewerColumn = new ViewerColumn();

    viewerColumn.setSolrName(columnStatus.getId());
    viewerColumn.setDisplayName(columnStatus.getCustomName());
    ViewerType viewerType = new ViewerType();
    viewerType.setDbType(columnStatus.getType());
    viewerType.setTypeName(columnStatus.getType().name());
    viewerColumn.setType(viewerType);
    viewerColumn.setDescription(columnStatus.getDescription());

    viewerColumn.setColumnIndexInEnclosingTable(0);
    viewerColumn.setNillable(true);

    return viewerColumn;
  }

  public static ViewerColumn getColumnBySolrName(List<ViewerColumn> columns, String solrName) {
    for (ViewerColumn column : columns) {
      if (column.getSolrName().equals(solrName))
        return column;
    }
    return null;
  }

  public static ViewerForeignKey convertToViewerForeignKey(ForeignKeysStatus foreignKeysStatus,
    CollectionStatus collectionStatus, String sourceTableUUID) {
    ViewerForeignKey foreignKey = new ViewerForeignKey();

    foreignKey.setName(foreignKeysStatus.getName());
    foreignKey.setReferencedTableUUID(foreignKeysStatus.getReferencedTableUUID());
    foreignKey.setReferencedTableId(foreignKeysStatus.getReferencedTableId());

    TableStatus sourceTableStatus = collectionStatus.getTableStatus(sourceTableUUID);
    TableStatus referencedTableStatus = collectionStatus.getTableStatus(foreignKeysStatus.getReferencedTableUUID());

    ArrayList<ViewerReference> viewerReferenceArrayList = new ArrayList<>();
    for (ForeignKeysStatus.ReferencedColumnStatus reference : foreignKeysStatus.getReferences()) {
      ViewerReference viewerReference = new ViewerReference();

      ColumnStatus sourceColumn = sourceTableStatus.getColumnById(reference.getSourceColumnId());
      ColumnStatus referencedColumn = referencedTableStatus.getColumnById(reference.getReferencedColumnId());

      viewerReference.setSourceColumnIndex(sourceColumn.getColumnIndex());
      viewerReference.setReferencedColumnIndex(referencedColumn.getColumnIndex());

      viewerReferenceArrayList.add(viewerReference);
    }

    if (foreignKey.getReferences() == null) {
      foreignKey.setReferences(new ArrayList<>());
    }

    foreignKey.getReferences().addAll(viewerReferenceArrayList);

    return foreignKey;
  }
}
