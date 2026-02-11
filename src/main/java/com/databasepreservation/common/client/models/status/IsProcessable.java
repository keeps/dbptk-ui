package com.databasepreservation.common.client.models.status;

import java.util.Date;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface IsProcessable {
  Date getLastUpdatedDate();
  Date getLastExecutionDate();

  default boolean shouldProcess() {
    if (getLastExecutionDate() == null) {
      return true;
    }
    if (getLastUpdatedDate() == null) {
      return false;
    }
    return getLastUpdatedDate().after(getLastExecutionDate());
  }
}
