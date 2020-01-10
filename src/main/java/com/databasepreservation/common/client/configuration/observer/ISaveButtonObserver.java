package com.databasepreservation.common.client.configuration.observer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface ISaveButtonObserver {
  void update(String databaseUUID, boolean enabled);
}
