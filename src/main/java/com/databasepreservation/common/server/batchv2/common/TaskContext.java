package com.databasepreservation.common.server.batchv2.common;

import java.util.Map;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TaskContext {
  CollectionStatus getCollectionStatus();

  <T> T getConfiguration(Class<T> clazz);

  void updateExecutionStatus(String partitionId, Map<String, Object> trackingInfo, String exitStatus);

  String getDatabaseUUID();
}
