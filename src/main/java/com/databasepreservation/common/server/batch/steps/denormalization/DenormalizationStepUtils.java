package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        processRelatedTable(database, targetTableStatus, relatedTable, path);
      }

      collectionStatus.setNeedsToBeProcessed(false);
    }
  }

  private static void processRelatedTable(ViewerDatabase database, TableStatus targetTableStatus,
    RelatedTablesConfiguration relatedTable, List<String> path) {

    ViewerTable currentTableMeta = database.getMetadata().getTable(relatedTable.getTableUUID());
    path.add(currentTableMeta.getName());

    List<RelatedColumnConfiguration> columnsIncluded = relatedTable.getColumnsIncluded();
    if (!columnsIncluded.isEmpty()) {
      Map<Integer, List<RelatedColumnConfiguration>> groupedColumns = columnsIncluded.stream()
        .collect(Collectors.groupingBy(c -> c.getGroupId() != null ? c.getGroupId() : 1));

      for (Map.Entry<Integer, List<RelatedColumnConfiguration>> entry : groupedColumns.entrySet()) {
        createAndAddColumnForGroup(targetTableStatus, relatedTable, path, currentTableMeta, entry.getKey(),
          entry.getValue());
      }
    }

    List<RelatedTablesConfiguration> innerTables = relatedTable.getRelatedTables();
    for (RelatedTablesConfiguration inner : innerTables) {
      processRelatedTable(database, targetTableStatus, inner, new ArrayList<>(path));
    }
  }

  private static void createAndAddColumnForGroup(TableStatus targetTableStatus, RelatedTablesConfiguration relatedTable,
    List<String> path, ViewerTable currentTableMeta, Integer groupId, List<RelatedColumnConfiguration> groupColumns) {

    List<String> names = new ArrayList<>();
    List<String> solrNames = new ArrayList<>();
    List<String> origTypes = new ArrayList<>();
    List<String> typeNames = new ArrayList<>();
    List<String> nullables = new ArrayList<>();

    for (RelatedColumnConfiguration col : groupColumns) {
      ViewerColumn meta = DataTransformationUtils.getColumnBySolrName(currentTableMeta.getColumns(), col.getSolrName());
      if (meta != null) {
        names.add(col.getColumnName());
        solrNames.add(col.getSolrName());
        origTypes.add(col.getColumnName() + ":" + meta.getType().getOriginalTypeName());
        typeNames.add(col.getColumnName() + ":" + meta.getType().getTypeName());
        nullables.add(col.getColumnName() + ":" + meta.getNillable());
      }
    }

    ViewerColumn viewerColumn = new ViewerColumn();
    viewerColumn.setDescription("Please EDIT");

    viewerColumn.setSolrName(relatedTable.getUuid() + "_" + groupId);
    viewerColumn.setDisplayName(String.join(">", path) + ">" + names.toString());

    int nextOrder = targetTableStatus.getLastColumnOrder() + 1;
    ColumnStatus columnStatus = StatusUtils.getColumnStatus(viewerColumn, true, nextOrder);

    columnStatus.setNestedColumns(buildNestedStatus(relatedTable, names, solrNames));
    columnStatus.setOriginalType(removeBrackets(origTypes));
    columnStatus.setTypeName(removeBrackets(typeNames));
    columnStatus.setNullable(removeBrackets(nullables));
    columnStatus.setType(ViewerType.dbTypes.NESTED);

    applyTemplates(columnStatus, names);

    targetTableStatus.addColumnStatus(columnStatus);
  }

  private static NestedColumnStatus buildNestedStatus(RelatedTablesConfiguration related, List<String> names,
    List<String> solrNames) {
    NestedColumnStatus nested = new NestedColumnStatus();
    nested.setMultiValue(related.getMultiValue());
    nested.setOriginalTable(related.getTableID());
    nested.setPath(related.getPath());
    nested.setNestedFields(names);
    nested.setNestedSolrNames(solrNames);
    nested.setReferenceUuid(related.getUuid());
    return nested;
  }

  private static void applyTemplates(ColumnStatus status, List<String> names) {
    if (status == null || names == null || names.isEmpty())
      return;

    StringBuilder sb = new StringBuilder();
    for (String name : names) {
      sb.append(ViewerConstants.OPEN_TEMPLATE_ENGINE).append(name).append(ViewerConstants.CLOSE_TEMPLATE_ENGINE);
    }
    String template = sb.toString();

    status.getExportStatus().getTemplateStatus().setTemplate(template);
    status.getDetailsStatus().getTemplateStatus().setTemplate(template);
    status.getSearchStatus().getList().getTemplate().setTemplate(template);
  }

  public static void removeDenormalizationColumns(TableStatus table) {
    List<ColumnStatus> columns = table.getColumns();
    if (columns != null) {
      columns.removeIf(c -> ViewerType.dbTypes.NESTED.equals(c.getType()) || c.getNestedColumns() != null);
    }
  }

  private static String removeBrackets(List<String> list) {
    return list.toString().replace("[", "").replace("]", "");
  }

  public static List<String> getGlobalNestedMetadataFields() {
    return List.of(ViewerConstants.SOLR_ROWS_NESTED, "token" + ViewerConstants.SOLR_DYN_NEST_MULTI,
      "type" + ViewerConstants.SOLR_DYN_TEXT_GENERAL);
  }

  public static List<String> getRelatedTablePrefixes(List<RelatedTablesConfiguration> relatedTables) {
    List<String> prefixes = new ArrayList<>();
    if (relatedTables != null) {
      for (RelatedTablesConfiguration rt : relatedTables) {
        prefixes.add(rt.getTableID() + "_" + ViewerConstants.SOLR_ROWS_NESTED_COL);
        prefixes.addAll(getRelatedTablePrefixes(rt.getRelatedTables()));
      }
    }
    return prefixes;
  }

  public static List<String> getAllNestedUUIDs(List<RelatedTablesConfiguration> relatedTables) {
    List<String> uuids = new ArrayList<>();
    if (relatedTables != null) {
      for (RelatedTablesConfiguration rt : relatedTables) {
        uuids.add(rt.getUuid());
        uuids.addAll(getAllNestedUUIDs(rt.getRelatedTables()));
      }
    }
    return uuids;
  }
}
