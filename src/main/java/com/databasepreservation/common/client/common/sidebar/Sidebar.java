package com.databasepreservation.common.client.common.sidebar;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface Sidebar extends IsWidget {
  void init(ViewerDatabase vb, CollectionStatus status);

  void reset(ViewerDatabase database, CollectionStatus status);

  boolean isInitialized();

  void select(String value);

  void selectFirst();

  void updateSidebarItem(String key, boolean value);
}
