/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common;

import com.databasepreservation.common.client.common.visualization.browse.configuration.ConfigurationStatusPanel;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class StatusAwareRightPanel extends RightPanel {
  private ConfigurationStatusPanel statusPanel;

  protected void updateStatusPanel(ViewerDatabase database) {
    if (database == null)
      return;

    if (this.statusPanel == null) {
      Widget root = getWidget();
      if (root instanceof InsertPanel) {
        this.statusPanel = new ConfigurationStatusPanel();
        ((InsertPanel) root).insert(this.statusPanel, 0);
      }
    }

    if (this.statusPanel != null) {
      this.statusPanel.setDatabase(database);
    }
  }
}
