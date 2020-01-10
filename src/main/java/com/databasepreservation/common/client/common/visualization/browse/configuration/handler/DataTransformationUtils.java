package com.databasepreservation.common.client.common.visualization.browse.configuration.handler;

import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.browse.configuration.TableNode;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.ConfigurationService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationUtils {
  public static void includeRelatedTable(TableNode childNode, DenormalizeConfiguration denormalizeConfiguration) {
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

    denormalizeConfiguration.addRelatedTable(relatedTable);
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

  public static void saveConfiguration(String databaseUUID, DenormalizeConfiguration denormalizeConfiguration) {
    if (denormalizeConfiguration != null && denormalizeConfiguration.getState().equals(ViewerJobStatus.NEW)) {
      ConfigurationService.Util.call((Boolean result) -> {
        Dialogs.showInformationDialog("Configuration file", "Created denormalization configuration file with success",
          "OK");
      }).createDenormalizeConfigurationFile(databaseUUID, denormalizeConfiguration.getTableUUID(),
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
}
