package com.databasepreservation.common.server.v2batch.steps.denormalization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.v2batch.job.JobContext;
import com.databasepreservation.common.server.v2batch.steps.partition.PartitionStrategy;

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
      for (String entryID : entries) {
        DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

        if (config != null && config.shouldProcess()) {
          ExecutionContext execContext = new ExecutionContext();

          execContext.putString("denormalizeEntryID", entryID);
          execContext.putString("name", "Partition-" + config.getTableID());
          partitions.put("partition-" + entryID, execContext);
        } else {
          LOGGER.warn("Skipping denormalization entry {} as it is not configured for processing.", entryID);
        }
      }
    }
    return partitions;
  }

  @Override
  public long calculateWorkload(JobContext context, ExecutionContext stepContext) {
    String entryID = stepContext.getString("denormalizeEntryID");
    DenormalizeConfiguration config = context.getDenormalizeConfig(entryID);

    if (config == null) {
      return 0;
    }

    Filter filter = FilterUtils.filterByTable(new Filter(), config.getTableID());
    try {
      return solrManager.countRows(context.getDatabaseUUID(), filter);
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Failed to calculate workload for denormalization entry {}: {}", entryID, e.getMessage(), e);
      return 0L;
    }
  }
}
