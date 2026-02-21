package com.databasepreservation.common.server.batch.steps.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TablePartitionStrategy implements PartitionStrategy {
  private static final Logger LOGGER = LoggerFactory.getLogger(TablePartitionStrategy.class);
  private final DatabaseRowsSolrManager solrManager;
  private final Predicate<TableStatus> tableFilter;

  public TablePartitionStrategy(DatabaseRowsSolrManager solrManager, Predicate<TableStatus> tableFilter) {
    this.solrManager = solrManager;
    this.tableFilter = tableFilter;
  }

  @Override
  public Map<String, ExecutionContext> mapPartitions(JobContext context) {
    Map<String, ExecutionContext> partitions = new HashMap<>();

    if (context.getCollectionStatus().getTables() != null) {
      context.getCollectionStatus().getTables().stream().filter(tableFilter).forEach(table -> {
        ExecutionContext v = new ExecutionContext();
        v.putString("tableId", table.getId());

        v.put("databaseUUID", context.getDatabaseUUID());
        v.put("filter", FilterUtils.filterByTable(new Filter(), table.getId()));
        v.put("fields", new ArrayList<String>());

        partitions.put("partition-" + table.getId(), v);
      });
    }
    return partitions;
  }

  @Override
  public long calculateWorkload(JobContext context, ExecutionContext stepContext) {
    Filter filter = (Filter) stepContext.get("filter");
    try {
      return solrManager.findRows(context.getDatabaseUUID(), filter, null, new Sublist(0, 0), null).getTotalCount();
    } catch (RequestNotValidException | GenericException e) {
      LOGGER.error("Error calculating workload for table {}: {}", stepContext.getString("tableId"), e.getMessage());
      return 0L;
    }
  }
}
