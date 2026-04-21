package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;

/**
 * Contract for column option panels that manage their own state. Enables
 * dirty-checking and localized persistence logic.
 */
public interface SavableOptionsPanel {

  /** @return true if the user modified any value in this panel. */
  boolean hasChanges();

  /**
   * Applies the panel's local UI state to the collection configuration models.
   */
  void applyChanges(ColumnStatus columnStatus, TableStatus tableStatus, CollectionStatus collectionStatus);

  default boolean requiresProcessing() {
    return false;
  }
}
