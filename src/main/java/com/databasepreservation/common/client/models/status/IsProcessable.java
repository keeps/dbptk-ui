package com.databasepreservation.common.client.models.status;

import java.util.Date;

import com.databasepreservation.common.client.models.status.collection.ProcessingState;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface IsProcessable {
  ProcessingState getProcessingState();

  Date getLastUpdatedDate();

  Date getLastExecutionDate();

  default boolean shouldProcess() {
    if (getProcessingState() == ProcessingState.TO_REMOVE) {
      return true;
    }

    if (getLastExecutionDate() == null) {
      return true;
    }

    if (getLastUpdatedDate() == null) {
      return false;
    }
    return getLastUpdatedDate().after(getLastExecutionDate());
  }
}
