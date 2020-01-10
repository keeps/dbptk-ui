package com.databasepreservation.common.client.configuration.observer;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CollectionObserver {

  private CollectionStatus collection;
  private List<CollectionStatusObserver> observerList = new ArrayList<>();

  public void addObserver(CollectionStatusObserver observer) {
    if (!this.observerList.contains(observer)) {
      this.observerList.add(observer);
    }
  }

  public void removeObserver(CollectionStatusObserver observer) {
    this.observerList.remove(observer);
  }

  public void setCollectionStatus(CollectionStatus collection) {
    this.collection = collection;
    for (CollectionStatusObserver observer : this.observerList) {
      observer.updateCollection(this.collection);
    }
  }
}
