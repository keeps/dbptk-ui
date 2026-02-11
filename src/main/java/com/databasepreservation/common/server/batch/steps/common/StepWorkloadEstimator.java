package com.databasepreservation.common.server.batch.steps.common;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface StepWorkloadEstimator {
  long estimate(String databaseUUID, CollectionStatus status);
}
