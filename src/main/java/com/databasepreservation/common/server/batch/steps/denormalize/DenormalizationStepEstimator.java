package com.databasepreservation.common.server.batch.steps.denormalize;

import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.steps.common.StepWorkloadEstimator;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DenormalizationStepEstimator implements StepWorkloadEstimator {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizationStepEstimator.class);

  private final DatabaseRowsSolrManager solrManager;

  public DenormalizationStepEstimator(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Override
  public long estimate(String databaseUUID, CollectionStatus status) {
    long total = 0;

    if (status.getDenormalizations() == null)
      return 0;

    for (String entryID : status.getDenormalizations()) {
      try {
        DenormalizeConfiguration config = ViewerFactory.getConfigurationManager()
          .getDenormalizeConfigurationFromCollectionStatusEntry(databaseUUID, entryID);

        if (config != null && config.shouldProcess()) {
          Filter filter = FilterUtils.filterByTable(new Filter(), config.getTableID());
          IndexResult<ViewerRow> result = solrManager.findRows(databaseUUID, filter, null, new Sublist(0, 0), null);
          total += result.getTotalCount();
        }
      } catch (Exception e) {
        LOGGER.warn("Error calculating total for denormalization entry ID: " + entryID, e);
      }
    }

    return total;
  }
}
