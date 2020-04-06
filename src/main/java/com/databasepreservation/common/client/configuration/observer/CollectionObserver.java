package com.databasepreservation.common.client.configuration.observer;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CollectionObserver {
  private List<ICollectionStatusObserver> observerList = new ArrayList<>();

  public void addObserver(ICollectionStatusObserver observer) {
    if (!this.observerList.contains(observer)) {
      this.observerList.add(observer);
    }
  }

  public void removeObserver(ICollectionStatusObserver observer) {
    this.observerList.remove(observer);
  }

  public void setCollectionStatus(ViewerCollectionConfiguration collection) {
    for (ICollectionStatusObserver observer : this.observerList) {
      observer.updateCollection(collection);
    }
  }
}
