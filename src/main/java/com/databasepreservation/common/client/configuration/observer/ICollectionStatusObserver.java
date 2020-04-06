package com.databasepreservation.common.client.configuration.observer;

import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface ICollectionStatusObserver {
  void updateCollection(ViewerCollectionConfiguration viewerCollectionConfiguration);
}
