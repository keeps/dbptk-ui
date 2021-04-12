/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.metadata;

import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.google.gwt.user.client.ui.Composite;

public abstract class MetadataPanel extends Composite {
  public abstract void handleBreadcrumb(BreadcrumbPanel breadcrumb);
}
