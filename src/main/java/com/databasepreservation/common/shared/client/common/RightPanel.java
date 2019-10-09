package com.databasepreservation.common.shared.client.common;

import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class RightPanel extends Composite {

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   * 
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  public abstract void handleBreadcrumb(BreadcrumbPanel breadcrumb);
}
