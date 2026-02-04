package com.databasepreservation.common.server.batch.steps.denormalize;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
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
}
