package com.databasepreservation.common.client;

import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.configuration.observer.SaveButtonObserver;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ObserverManager {

  private static CollectionObserver collectionObserver;
  private static SaveButtonObserver saveObserver;
  private static boolean instantiated = false;

  private static void instantiate() {
    if (!instantiated) {
      collectionObserver = new CollectionObserver();
      saveObserver = new SaveButtonObserver();
      instantiated = true;
    }
  }

  public static CollectionObserver getCollectionObserver() {
    instantiate();
    return collectionObserver;
  }

  public static SaveButtonObserver getSaveObserver() {
    instantiate();
    return saveObserver;
  }
}
