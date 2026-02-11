package com.databasepreservation.common.server.batch.steps.virtualColumn;

import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.steps.common.StepWorkloadEstimator;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualColumnStepEstimator implements StepWorkloadEstimator {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualColumnStepEstimator.class);

  private final DatabaseRowsSolrManager solrManager;

  public VirtualColumnStepEstimator(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Override
  public long estimate(String databaseUUID, CollectionStatus status) {
    if (status.getTables() == null)
      return 0;

    return status.getTables().stream().filter(table -> table.getColumns() != null)
      .filter(VirtualColumnStepUtils::hasProcessableVirtualColumns).mapToLong(table -> countRowsForTable(databaseUUID, table)).sum();

  }

  private long countRowsForTable(String databaseUUID, TableStatus table) {
    try {
      Filter filter = FilterUtils.filterByTable(new Filter(), table.getId());
      IndexResult<ViewerRow> result = solrManager.findRows(databaseUUID, filter, null, new Sublist(0, 0), null);
      return result.getTotalCount();
    } catch (Exception e) {
      LOGGER.warn("Error estimating rows for table: " + table.getId(), e);
      return 0;
    }
  }
}
