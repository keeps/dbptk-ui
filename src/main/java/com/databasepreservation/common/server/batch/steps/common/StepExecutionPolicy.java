package com.databasepreservation.common.server.batch.steps.common;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface StepExecutionPolicy {
  boolean shouldExecute(String databaseUUID, CollectionStatus status);
}
