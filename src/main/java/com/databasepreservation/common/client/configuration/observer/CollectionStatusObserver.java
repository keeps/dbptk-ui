package com.databasepreservation.common.client.configuration.observer;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface CollectionStatusObserver {
  void updateCollection(CollectionStatus collectionStatus);
}
