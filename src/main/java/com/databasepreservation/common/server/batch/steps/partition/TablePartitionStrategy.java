package com.databasepreservation.common.server.batch.steps.partition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TablePartitionStrategy implements PartitionStrategy {
  private static final Logger LOGGER = LoggerFactory.getLogger(TablePartitionStrategy.class);
  private final DatabaseRowsSolrManager solrManager;
  private final Predicate<TableStatus> tableFilter;
  private final PartitionContextEnricher<TableStatus> contextEnricher;

  public TablePartitionStrategy(DatabaseRowsSolrManager solrManager, Predicate<TableStatus> tableFilter,
    PartitionContextEnricher<TableStatus> contextEnricher) {
    this.solrManager = solrManager;
    this.tableFilter = tableFilter;
    this.contextEnricher = contextEnricher;
  }

  @Override
  public Map<String, ExecutionContext> mapPartitions(JobContext context) {
    Map<String, ExecutionContext> partitions = new HashMap<>();

    if (context.getCollectionStatus().getTables() != null) {
      ViewerDatabase database = context.getViewerDatabase();

      if (database == null) {
        throw new UnexpectedJobExecutionException(
          "Cannot partition workload: Database " + context.getDatabaseUUID() + " returned null from Solr.");
      }
      final String dbVersion = database.getVersion();
      final String dbPath = database.getPath();

      context.getCollectionStatus().getTables().stream().filter(tableFilter).forEach(table -> {
        ExecutionContext partitionContext = new ExecutionContext();

        partitionContext.putString(BatchConstants.TABLE_ID_KEY, table.getId());
        partitionContext.put(BatchConstants.DATABASE_UUID_KEY, context.getDatabaseUUID());
        partitionContext.putString(BatchConstants.DB_VERSION_KEY, dbVersion);
        partitionContext.putString(BatchConstants.DB_PATH_KEY, dbPath);

        contextEnricher.enrich(partitionContext, table);

        partitions.put(BatchConstants.PARTITION_PREFIX + table.getId(), partitionContext);
      });
    }
    return partitions;
  }

  @Override
  public long calculateWorkload(JobContext context, ExecutionContext partitionContext) {
    Filter filter = (Filter) partitionContext.get(BatchConstants.FILTER_KEY);
    try {
      return solrManager.findRows(context.getDatabaseUUID(), filter, null, new Sublist(0, 0), null).getTotalCount();
    } catch (RequestNotValidException | GenericException e) {
      LOGGER.error("Error calculating workload for table {}: {}",
        partitionContext.getString(BatchConstants.TABLE_ID_KEY), e.getMessage());
      return 0L;
    }
  }
}
