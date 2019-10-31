package com.databasepreservation.common.shared.client.common.visualization.metadata;

import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.google.gwt.user.client.ui.Composite;

public abstract class MetadataPanel extends Composite {
  public abstract void handleBreadcrumb(BreadcrumbPanel breadcrumb);
}
