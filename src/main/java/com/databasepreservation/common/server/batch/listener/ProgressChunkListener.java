package com.databasepreservation.common.server.batch.listener;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ProgressChunkListener implements ChunkListener, StepExecutionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProgressChunkListener.class);

  private final DatabaseRowsSolrManager solrManager;
  private String jobUUID;
  private String databaseUUID;
  private String tableID;
  private long totalRows = 0;
  private long startTime;

  public ProgressChunkListener(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    this.jobUUID = stepExecution.getJobParameters().getString(ViewerConstants.INDEX_ID);
    this.databaseUUID = stepExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);
    this.tableID = stepExecution.getJobParameters().getString(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);

    try {
      List<FilterParameter> filterParameters = new ArrayList<>();
      filterParameters.add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_UUID, tableID));
      IndexResult<ViewerRow> rows = solrManager.findRows(databaseUUID, new Filter(filterParameters), null,
        new Sublist(0, 0), null);
      this.totalRows = rows.getTotalCount();
      LOGGER.info("Total rows to process: {}", totalRows);

      solrManager.editBatchJob(jobUUID, totalRows, 0);
    } catch (Exception e) {
      LOGGER.warn("Could not calculate total rows for progress bar", e);
      this.totalRows = -1;
    }
  }

  @Override
  public void beforeChunk(ChunkContext context) {
    this.startTime = System.currentTimeMillis();
  }

  @Override
  public void afterChunk(ChunkContext context) {
    long duration = System.currentTimeMillis() - this.startTime;
    long readCount = context.getStepContext().getStepExecution().getReadCount();

    LOGGER.info("Progress: {}/{} | Chunk Time: {} ms ", readCount, totalRows, duration);

    if (jobUUID != null && totalRows > 0) {
      try {
        solrManager.editBatchJob(jobUUID, totalRows, readCount);
      } catch (Exception e) {
        LOGGER.warn("Failed to update Solr progress", e);
      }
    }
  }

  @Override
  public void afterChunkError(ChunkContext context) {
    LOGGER.error("Error occurred during chunk processing");
  }

  @Override
  public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
    return null;
  }
}
