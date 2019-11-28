/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.widgets.wcag;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

public class AccessibleFocusPanel extends FocusPanel {

  public AccessibleFocusPanel() {
    super();
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleFocusPanel(Widget w) {
    super(w);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }
}
