/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.shared.client.widgets.wcag;

import com.google.gwt.user.cellview.client.SimplePager;

public class AccessibleSimplePager extends SimplePager {

  public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, boolean showLastPageButton) {
    super(location, showFastForwardButton, showLastPageButton);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager() {
    super();
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location) {
    super(location);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, int fastForwardRows,
    boolean showLastPageButton) {
    super(location, showFastForwardButton, fastForwardRows, showLastPageButton);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location, SimplePager.Resources resources, boolean showFastForwardButton,
    int fastForwardRows, boolean showLastPageButton, boolean showFirstPageButton,
    SimplePager.ImageButtonsConstants imageButtonConstants) {
    super(location, resources, showFastForwardButton, fastForwardRows, showLastPageButton, showFirstPageButton,
      imageButtonConstants);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

}
