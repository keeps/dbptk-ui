package com.databasepreservation.common.server.batch.steps.denormalize.listeners;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

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
public class DenormalizeLifecycleListener implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeLifecycleListener.class);

  private final DatabaseRowsSolrManager solrManager;
  private final DenormalizeConfiguration config;
  private final String databaseUUID;
  private final ViewerDatabase database;

  public DenormalizeLifecycleListener(DatabaseRowsSolrManager solrManager, DenormalizeConfiguration config,
    ViewerDatabase database, String databaseUUID) {
    this.solrManager = solrManager;
    this.config = config;
    this.database = database;
    this.databaseUUID = databaseUUID;
  }

  // --- SUBSTITUI O CLEANUP TASKLET ---
  @Override
  public void beforeStep(StepExecution stepExecution) {
    String tableID = config.getTableID();
    LOGGER.info(">>> [CLEANUP] Starting cleanup for table: {}", tableID);
    Filter filter = FilterUtils.filterByTable(new Filter(), tableID);

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

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    String tableUUID = config.getTableUUID();

    if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
      LOGGER.info(">>> [FINALIZE] Updating status for table: {}", tableUUID);

      try {
        updateCollectionStatus(tableUUID);

      } catch (Exception e) {
        LOGGER.error("Failed to finalize table {}", tableUUID, e);
        return ExitStatus.FAILED; // Marca o job como falhado se a finalização falhar
      }
    }
    return null;
  }

  private void updateCollectionStatus(String tableUUID) throws GenericException, IllegalAccessException {
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
        var tableMetadata = database.getMetadata().getTable(relatedTable.getTableUUID());
        var columnBySolrName = tableMetadata.getColumnBySolrName(column.getSolrName());

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

      String columnStatusName = String.join(">", path);
      viewerColumn.setDisplayName(columnStatusName + ">" + columnName.toString());
      nestedColumn.setPath(relatedTable.getPath());

      ViewerFactory.getConfigurationManager().addDenormalizationColumns(databaseUUID, config.getTableUUID(),
        viewerColumn, nestedColumn, template, removeBrackets(originalType), removeBrackets(typeName),
        removeBrackets(nullable));
    }

    for (RelatedTablesConfiguration innerRelatedTable : relatedTable.getRelatedTables()) {
      setAllColumnsToInclude(innerRelatedTable, new ArrayList<>(path));
    }
  }

  private String removeBrackets(List<String> list) {
    return list.toString().replace("[", "").replace("]", "");
  }

}
