package com.databasepreservation.common.client;

import com.databasepreservation.common.client.configuration.observer.CollectionObserver;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ObserverManager {

  private static CollectionObserver collectionObserver;
  private static boolean instantiated = false;

  private static void instantiate() {
    if (!instantiated) {
      collectionObserver = new CollectionObserver();
      instantiated = true;
    }
  }

  public static CollectionObserver getCollectionObserver() {
    instantiate();
    return collectionObserver;
  }
}
