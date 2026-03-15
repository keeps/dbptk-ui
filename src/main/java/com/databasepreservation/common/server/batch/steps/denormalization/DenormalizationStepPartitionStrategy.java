package com.databasepreservation.common.server.batch.steps.denormalization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.BatchConstants;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepPartitionStrategy implements PartitionStrategy {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizationStepPartitionStrategy.class);
  private final DatabaseRowsSolrManager solrManager;

  public DenormalizationStepPartitionStrategy(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Override
  public Map<String, ExecutionContext> mapPartitions(JobContext context) {
    Map<String, ExecutionContext> partitions = new HashMap<>();
    Set<String> entries = context.getCollectionStatus().getDenormalizations();

    if (entries != null) {
      int partitionIndex = 0;
      for (String entryID : entries) {
        DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);
        if (config != null && config.shouldProcess() && !config.isMarkedForRemoval()) {
          List<String> fieldsToReturn = new ArrayList<>();
          fieldsToReturn.add(ViewerConstants.INDEX_ID);

          for (RelatedTablesConfiguration relatedTable : config.getRelatedTables()) {
            for (ReferencesConfiguration reference : relatedTable.getReferences()) {
              fieldsToReturn.add(reference.getReferencedTable().getSolrName());
            }
          }

          ExecutionContext partitionContext = new ExecutionContext();
          partitionContext.putString(BatchConstants.DENORMALIZATION_ENTRY_ID_KEY, entryID);

          partitionContext.put(BatchConstants.DATABASE_UUID_KEY, context.getDatabaseUUID());
          partitionContext.put(BatchConstants.FILTER_KEY, FilterUtils.filterByTable(new Filter(), config.getTableID()));
          partitionContext.put(BatchConstants.FIELDS_KEY, new ArrayList<>(fieldsToReturn));

          String partitionName = (partitionIndex++) + "-" + config.getTableID();
          partitions.put(BatchConstants.PARTITION_PREFIX + partitionName, partitionContext);
        }
      }
    }
    return partitions;
  }

  @Override
  public long calculateWorkload(JobContext context, ExecutionContext partitionContext) {
    Filter filter = (Filter) partitionContext.get(BatchConstants.FILTER_KEY);

    if (filter == null) {
      return 0L;
    }

    try {
      return solrManager.countRows(context.getDatabaseUUID(), filter);
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Failed to calculate workload: {}", e.getMessage());
      return 0L;
    }
  }
}
