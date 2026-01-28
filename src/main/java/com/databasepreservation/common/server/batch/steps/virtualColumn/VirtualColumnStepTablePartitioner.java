package com.databasepreservation.common.server.batch.steps.virtualColumn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.server.batch.steps.denormalize.DenormalizeStepTablePartitioner;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStepTablePartitioner implements Partitioner {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeStepTablePartitioner.class);
  private final CollectionStatus collectionStatus;

  public VirtualColumnStepTablePartitioner(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> partitions = new HashMap<>();

    if (collectionStatus.getTables() == null || collectionStatus.getTables().isEmpty()) {
      LOGGER.info("No tables found to check for virtual columns.");
      return partitions;
    }

    LOGGER.info("Checking {} tables for virtual columns...", collectionStatus.getTables().size());

    for (TableStatus table : collectionStatus.getTables()) {
      if (VirtualColumnStepUtils.hasProcessableVirtualColumns(table)) {
        LOGGER.info("Adding table {} to execution queue for virtual column processing.", table.getId());

        ExecutionContext value = new ExecutionContext();
        value.putString("tableId", table.getId());
        value.putString("name", "Partition-" + table.getId());
        partitions.put("partition-" + table.getId(), value);

      } else {
        LOGGER.info("Skipping table {} (No processable virtual columns).", table.getId());
      }
    }

    return partitions;
  }
}
