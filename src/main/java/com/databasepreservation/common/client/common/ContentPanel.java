package com.databasepreservation.common.client.common;

import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class ContentPanel extends Composite {

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   * 
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  public abstract void handleBreadcrumb(BreadcrumbPanel breadcrumb);
}
