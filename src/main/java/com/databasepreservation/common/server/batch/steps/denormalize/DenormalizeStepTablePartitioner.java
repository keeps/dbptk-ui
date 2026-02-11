package com.databasepreservation.common.server.batch.steps.denormalize;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeStepTablePartitioner implements Partitioner {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeStepTablePartitioner.class);
  private final CollectionStatus collectionStatus;

  public DenormalizeStepTablePartitioner(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> partitions = new HashMap<>();

    if (collectionStatus.getDenormalizations() == null || collectionStatus.getDenormalizations().isEmpty()) {
      LOGGER.info("No denormalizations found to process.");
      return partitions;
    }

    LOGGER.info("Checking {} potential tables for denormalization...", collectionStatus.getDenormalizations().size());

    for (String denormalizeEntryID : collectionStatus.getDenormalizations()) {
      try {
        DenormalizeConfiguration config = ViewerFactory.getConfigurationManager()
          .getDenormalizeConfigurationFromCollectionStatusEntry(collectionStatus.getDatabaseUUID(), denormalizeEntryID);

        if (config.shouldProcess()) {
          LOGGER.info("Adding table {} to execution queue.", config.getTableID());

          ExecutionContext value = new ExecutionContext();
          value.putString("denormalizeEntryID", denormalizeEntryID);
          value.putString("name", "Partition-" + denormalizeEntryID);
          partitions.put("partition-" + denormalizeEntryID, value);

        } else {
          LOGGER.info("Skipping table {} (Already up-to-date).", config.getTableID());
        }
      } catch (GenericException e) {
        throw new RuntimeException(e);
      }
    }

    return partitions;
  }
}
