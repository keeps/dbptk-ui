package com.databasepreservation.common.client.models.status;

import java.util.Date;

import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface IsProcessable {
  ProcessingState getProcessingState();

  Date getLastUpdatedDate();

  void setProcessingState(ProcessingState state);

  Date getLastExecutionDate();

  void setLastExecutionDate(Date date);

  default boolean shouldProcess() {
    if (getProcessingState() == ProcessingState.TO_REMOVE) {
      return true;
    }

    if (getProcessingState() == ProcessingState.PENDING_METADATA || getProcessingState() == ProcessingState.PROCESSED
      || getProcessingState() == ProcessingState.PROCESSING || getProcessingState() == ProcessingState.FAILED) {
      return false;
    }

    if (getLastExecutionDate() == null) {
      return true;
    }

    if (getLastUpdatedDate() == null) {
      return false;
    }
    return getLastUpdatedDate().after(getLastExecutionDate());
  }

  @JsonIgnore
  default boolean isMarkedForRemoval() {
    return getProcessingState() == ProcessingState.TO_REMOVE;
  }

  @JsonIgnore
  default void markAsPendingMetadata() {
    setProcessingState(ProcessingState.PENDING_METADATA);
  }

  @JsonIgnore
  default boolean isPendingMetadata() {
    return getProcessingState() == ProcessingState.PENDING_METADATA;
  }

  @JsonIgnore
  default void markAsProcessed() {
    setProcessingState(ProcessingState.PROCESSED);
    setLastExecutionDate(new Date());
  }
}
