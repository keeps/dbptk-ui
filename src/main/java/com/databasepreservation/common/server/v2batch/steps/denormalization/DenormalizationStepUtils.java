package com.databasepreservation.common.server.v2batch.steps.denormalization;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.utils.StatusUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepUtils {
  public static void updateCollectionStatusInMemory(CollectionStatus collectionStatus, DenormalizeConfiguration config,
    ViewerDatabase database) {

    String tableUUID = config.getTableUUID();
    TableStatus targetTableStatus = collectionStatus.getTableStatus(tableUUID);

    if (targetTableStatus != null) {
      removeDenormalizationColumns(targetTableStatus);

      List<RelatedTablesConfiguration> relatedTables = config.getRelatedTables();
      for (RelatedTablesConfiguration relatedTable : relatedTables) {
        List<String> path = new ArrayList<>();
        ViewerTable rootTable = database.getMetadata().getTable(tableUUID);
        path.add(rootTable.getName());

        processRelatedTable(collectionStatus, database, targetTableStatus, relatedTable, path);
      }

      collectionStatus.setNeedsToBeProcessed(false);
    }
  }

  private static void processRelatedTable(CollectionStatus collectionStatus, ViewerDatabase database,
    TableStatus targetTableStatus, RelatedTablesConfiguration relatedTable, List<String> path) {

    ViewerTable currentTableMeta = database.getMetadata().getTable(relatedTable.getTableUUID());
    path.add(currentTableMeta.getName());

    List<RelatedColumnConfiguration> columnsIncluded = relatedTable.getColumnsIncluded();
    if (!columnsIncluded.isEmpty()) {
      createAndAddColumn(collectionStatus, database, targetTableStatus, relatedTable, path, currentTableMeta);
    }

    List<RelatedTablesConfiguration> innerTables = relatedTable.getRelatedTables();
    for (RelatedTablesConfiguration inner : innerTables) {
      processRelatedTable(collectionStatus, database, targetTableStatus, inner, new ArrayList<>(path));
    }
  }

  private static void createAndAddColumn(CollectionStatus collectionStatus, ViewerDatabase database,
    TableStatus targetTableStatus, RelatedTablesConfiguration relatedTable, List<String> path,
    ViewerTable currentTableMeta) {

    TableStatus currentTableStatus = collectionStatus.getTableStatusByTableId(currentTableMeta.getId());
    List<ViewerColumn> allColumns = DataTransformationUtils
      .getViewerColumnsWithVirtualColumns(currentTableMeta.getColumns(), currentTableStatus);

    List<String> names = new ArrayList<>();
    List<String> origTypes = new ArrayList<>();
    List<String> typeNames = new ArrayList<>();
    List<String> nullables = new ArrayList<>();

    for (RelatedColumnConfiguration col : relatedTable.getColumnsIncluded()) {
      ViewerColumn meta = DataTransformationUtils.getColumnBySolrName(allColumns, col.getSolrName());
      if (meta != null) {
        names.add(col.getColumnName());
        origTypes.add(col.getColumnName() + ":" + meta.getType().getOriginalTypeName());
        typeNames.add(col.getColumnName() + ":" + meta.getType().getTypeName());
        nullables.add(col.getColumnName() + ":" + meta.getNillable());
      }
    }

    ViewerColumn viewerColumn = new ViewerColumn();
    viewerColumn.setDescription("Please EDIT");
    viewerColumn.setSolrName(relatedTable.getUuid());
    viewerColumn.setDisplayName(String.join(">", path) + ">" + names.toString());

    int nextOrder = targetTableStatus.getLastColumnOrder() + 1;
    ColumnStatus columnStatus = StatusUtils.getColumnStatus(viewerColumn, true, nextOrder);

    columnStatus.setNestedColumns(buildNestedStatus(relatedTable, names));
    columnStatus.setOriginalType(removeBrackets(origTypes));
    columnStatus.setTypeName(removeBrackets(typeNames));
    columnStatus.setNullable(removeBrackets(nullables));
    columnStatus.setType(ViewerType.dbTypes.NESTED);

    applyTemplates(columnStatus, names);

    targetTableStatus.addColumnStatus(columnStatus);
  }

  private static NestedColumnStatus buildNestedStatus(RelatedTablesConfiguration related, List<String> names) {
    NestedColumnStatus nested = new NestedColumnStatus();
    nested.setMultiValue(related.getMultiValue());
    nested.setOriginalTable(related.getTableID());
    nested.setPath(related.getPath());
    nested.setNestedFields(names);
    return nested;
  }

  private static void applyTemplates(ColumnStatus status, List<String> names) {
    if (status == null || names == null || names.isEmpty()) return;

    StringBuilder sb = new StringBuilder();
    for (String name : names) {
      sb.append(ViewerConstants.OPEN_TEMPLATE_ENGINE).append(name).append(ViewerConstants.CLOSE_TEMPLATE_ENGINE);
    }
    String template = sb.toString();

    status.getExportStatus().getTemplateStatus().setTemplate(template);
    status.getDetailsStatus().getTemplateStatus().setTemplate(template);
    status.getSearchStatus().getList().getTemplate().setTemplate(template);
  }

  private static void removeDenormalizationColumns(TableStatus table) {
    List<ColumnStatus> columns = table.getColumns();
    if (columns != null) {
      columns.removeIf(c -> ViewerType.dbTypes.NESTED.equals(c.getType()) || c.getNestedColumns() != null);
    }
  }

  private static String removeBrackets(List<String> list) {
    return list.toString().replace("[", "").replace("]", "");
  }
}
