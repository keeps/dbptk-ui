package com.databasepreservation.common.server.batch.steps.denormalize.listeners;

import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.steps.common.StepProgressAggregator;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeListener implements StepExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeListener.class);

  private final DatabaseRowsSolrManager solrManager;
  private final CollectionStatus collectionStatus;
  private final StepProgressAggregator aggregator;
  private final String databaseUUID;

  public DenormalizeListener(DatabaseRowsSolrManager solrManager, CollectionStatus collectionStatus,
    StepProgressAggregator aggregator, String databaseUUID) {
    this.solrManager = solrManager;
    this.collectionStatus = collectionStatus;
    this.aggregator = aggregator;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    long grandTotal = 0;

    for (String entryID : collectionStatus.getDenormalizations()) {
      try {
        DenormalizeConfiguration config = ViewerFactory.getConfigurationManager()
          .getDenormalizeConfigurationFromCollectionStatusEntry(databaseUUID, entryID);

        if (config != null && config.shouldProcess()) {
          Filter filter = FilterUtils.filterByTable(new Filter(), config.getTableID());
          IndexResult<ViewerRow> result = solrManager.findRows(databaseUUID, filter, null, new Sublist(0, 0), null);
          grandTotal += result.getTotalCount();
        }
      } catch (Exception e) {
        LOGGER.warn("Error calculating total for denormalization entry ID: " + entryID, e);
      }
    }

    aggregator.reset(grandTotal);

    String jobUUID = stepExecution.getJobParameters().getString(ViewerConstants.INDEX_ID);
    try {
      solrManager.editBatchJob(jobUUID, grandTotal, 0);
    } catch (Exception e) {
      LOGGER.warn("Error updating batch job with total count for denormalization", e);
    }
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    return null;
  }
}
