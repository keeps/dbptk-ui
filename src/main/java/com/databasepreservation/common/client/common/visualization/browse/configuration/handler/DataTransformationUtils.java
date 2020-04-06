package com.databasepreservation.common.client.common.visualization.browse.configuration.handler;

import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation.TableNode;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
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
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationUtils {
  public static ClientMessages messages = GWT.create(ClientMessages.class);
	private DataTransformationUtils() {}

  public static void includeRelatedTable(TableNode childNode, DenormalizeConfiguration denormalizeConfiguration) {
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

  public static void saveConfiguration(String databaseUUID, DenormalizeConfiguration denormalizeConfiguration,
    CollectionStatus collectionStatus) {
    if (denormalizeConfiguration != null && denormalizeConfiguration.getState().equals(ViewerJobStatus.NEW)) {
      CollectionService.Util.call((Boolean result) -> {
        final CollectionObserver collectionObserver = ObserverManager.getCollectionObserver();
        collectionObserver.setCollectionStatus(collectionStatus);
        CollectionService.Util.call((Void response) -> {
          Toast.showInfo(messages.advancedConfigurationLabelForDataTransformation(),
            "Created denormalization configuration file with success for " + denormalizeConfiguration.getTableID());
        }, errorMessage -> {
          Dialogs.showErrors(messages.advancedConfigurationLabelForDataTransformation(), errorMessage,
            messages.basicActionClose());
        }).run(databaseUUID,databaseUUID, denormalizeConfiguration.getTableUUID());
      }, errorMessage -> {
        Dialogs.showErrors(messages.advancedConfigurationLabelForDataTransformation(), errorMessage,
          messages.basicActionClose());
      }).createDenormalizeConfigurationFile(databaseUUID, databaseUUID, denormalizeConfiguration.getTableUUID(),
        denormalizeConfiguration);
    }
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
        fieldsToReturn.add(key + ":[subquery]");
        extraParameters.put(key + ".q", "+nestedUUID:" + nestedTableId + " AND {!terms f=_root_ v=$row.uuid}");
        Integer quantity = column.getNestedColumns().getQuantityInList();
        if(quantity <= column.getNestedColumns().getMaxQuantityInList()){
          extraParameters.put(key + ".rows", quantity.toString());
        } else {
          extraParameters.put(key + ".rows", column.getNestedColumns().getMaxQuantityInList().toString());
        }
        nestedCount++;
      }
    }
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_NESTED + ":" + "\"" + keys + "\"");
  }
}
