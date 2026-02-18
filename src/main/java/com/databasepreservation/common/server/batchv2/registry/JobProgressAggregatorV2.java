package com.databasepreservation.common.server.batchv2.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class JobProgressAggregatorV2 {
  private final Map<String, Long> partitionWeights = new ConcurrentHashMap<>();
  private final Map<String, AtomicLong> partitionProgress = new ConcurrentHashMap<>();

  public void registerPartition(String tableId, long totalItems) {
    partitionWeights.put(tableId, totalItems);
    partitionProgress.put(tableId, new AtomicLong(0));
  }

  public void updateProgress(String tableId, long currentCount) {
    AtomicLong progress = partitionProgress.get(tableId);
    if (progress != null) {
      progress.set(currentCount);
    }
  }

  public double getGlobalProgress() {
    long totalWeight = partitionWeights.values().stream().mapToLong(Long::longValue).sum();
    if (totalWeight == 0)
      return 0.0;

    long currentTotal = partitionProgress.values().stream().mapToLong(AtomicLong::get).sum();

    return (double) currentTotal / totalWeight * 100.0;
  }

  public double getPartitionProgress(String tableId) {
    Long weight = partitionWeights.get(tableId);
    AtomicLong progress = partitionProgress.get(tableId);

    if (weight == null || weight == 0)
      return 0.0;
    return (double) progress.get() / weight * 100.0;
  }

}
