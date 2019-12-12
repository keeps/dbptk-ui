package com.databasepreservation.common.client.common.sidebar;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface Sidebar extends IsWidget {
  void init(ViewerDatabase vb);
  boolean isInitialized();
  void select(String value);
}
