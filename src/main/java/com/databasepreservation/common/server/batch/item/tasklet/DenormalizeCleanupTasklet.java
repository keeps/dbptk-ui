package com.databasepreservation.common.server.batch.item.tasklet;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeCleanupTasklet implements Tasklet {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeCleanupTasklet.class);

  private final DatabaseRowsSolrManager solrManager;
  private final DenormalizeConfiguration config;
  private final ViewerDatabase database;
  private final String databaseUUID;
  private final String tableUUID;

  public DenormalizeCleanupTasklet(DatabaseRowsSolrManager solrManager, DenormalizeConfiguration config,
    ViewerDatabase database, String databaseUUID, String tableUUID) {
    this.solrManager = solrManager;
    this.config = config;
    this.database = database;
    this.databaseUUID = databaseUUID;
    this.tableUUID = tableUUID;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    LOGGER.info("Starting cleanup for table: {}", config.getTableID());

    cleanNestedDocuments();
    updateCollectionStatus();

    return RepeatStatus.FINISHED;
  }

  private void cleanNestedDocuments() {
    Filter filter = FilterUtils.filterByTable(new Filter(), config.getTableID());

    IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, filter, null, new ArrayList<>());

    for (ViewerRow row : allRows) {
      solrManager.deleteNestedDocuments(databaseUUID, row.getUuid());
    }

    try {
      allRows.close();
    } catch (Exception e) {
      LOGGER.warn("Error closing cursor during cleanup", e);
    }

    for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
      solrManager.deleteNestedDocuments(databaseUUID, relatedTable.getUuid());
    }
  }

  private void updateCollectionStatus() throws GenericException, IllegalAccessException {
    ViewerFactory.getConfigurationManager().removeDenormalizationColumns(databaseUUID, config.getTableUUID());
    for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
      List<String> path = new ArrayList<>();
      path.add(database.getMetadata().getTable(tableUUID).getName());
      setAllColumnsToInclude(relatedTable, path);
    }
  }

  private void setAllColumnsToInclude(RelatedTablesConfiguration relatedTable, List<String> path)
    throws GenericException, IllegalAccessException {
    List<RelatedColumnConfiguration> columnsIncluded = relatedTable.getColumnsIncluded();
    path.add(database.getMetadata().getTable(relatedTable.getTableUUID()).getName());

    if (!columnsIncluded.isEmpty()) {
      ViewerColumn viewerColumn = new ViewerColumn();
      viewerColumn.setDescription("Please EDIT");
      viewerColumn.setSolrName(relatedTable.getUuid());
      NestedColumnStatus nestedColumn = new NestedColumnStatus();
      nestedColumn.setMultiValue(relatedTable.getMultiValue());
      nestedColumn.setOriginalTable(relatedTable.getTableID());
      List<String> columnName = new ArrayList<>();
      List<String> originalType = new ArrayList<>();
      List<String> typeName = new ArrayList<>();
      List<String> nullable = new ArrayList<>();

      for (RelatedColumnConfiguration column : columnsIncluded) {
        ViewerColumn columnBySolrName = database.getMetadata().getTable(relatedTable.getTableUUID())
          .getColumnBySolrName(column.getSolrName());
        nestedColumn.getNestedFields().add(column.getColumnName());
        nestedColumn.getNestedSolrNames().add(column.getSolrName());
        columnName.add(column.getColumnName());
        originalType.add(column.getColumnName() + ":" + columnBySolrName.getType().getOriginalTypeName());
        typeName.add(column.getColumnName() + ":" + columnBySolrName.getType().getTypeName());
        nullable.add(column.getColumnName() + ":" + columnBySolrName.getNillable());
      }

      String template = "";
      for (String templateName : columnName) {
        template = template + ViewerConstants.OPEN_TEMPLATE_ENGINE + templateName
          + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
      }

      String columnStatusName = "";
      String separator = "";
      for (String tableName : path) {
        columnStatusName = columnStatusName + separator + tableName;
        separator = ">";
      }

      viewerColumn.setDisplayName(columnStatusName + separator + columnName.toString());
      nestedColumn.setPath(relatedTable.getPath());
      ViewerFactory.getConfigurationManager().addDenormalizationColumns(databaseUUID, config.getTableUUID(),
        viewerColumn, nestedColumn, template, removeBrackets(originalType), removeBrackets(typeName),
        removeBrackets(nullable));
    }
    for (RelatedTablesConfiguration innerRelatedTable : relatedTable.getRelatedTables()) {
      setAllColumnsToInclude(innerRelatedTable, path);
    }
  }

  private String removeBrackets(List<String> list) {
    return list.toString().replace("[", "").replace("]", "");
  }
}
