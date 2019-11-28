package com.databasepreservation.common.client.common.visualization.metadata;

import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.google.gwt.user.client.ui.Composite;

public abstract class MetadataPanel extends Composite {
  public abstract void handleBreadcrumb(BreadcrumbPanel breadcrumb);
}
