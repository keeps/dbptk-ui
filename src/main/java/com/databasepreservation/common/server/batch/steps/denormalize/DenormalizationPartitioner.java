package com.databasepreservation.common.server.batch.steps.denormalize;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationPartitioner implements Partitioner {
  private final CollectionStatus collectionStatus;

  public DenormalizationPartitioner(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> partitions = new HashMap<>();

    int i = 0;
    for (String denormalizeEntryID : collectionStatus.getDenormalizations()) {

      ExecutionContext context = new ExecutionContext();
      context.put("denormalizeEntryID", denormalizeEntryID);
      context.putString("partitionName", "partition_" + denormalizeEntryID);

      partitions.put("partition_" + i++, context);
    }

    return partitions;
  }
}
