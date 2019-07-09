package com.databasepreservation.main.desktop.client.dbptk.metadata;

import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.google.gwt.user.client.ui.Composite;

public abstract class MetadataRightPanel extends Composite {
    public abstract void handleBreadcrumb(BreadcrumbPanel breadcrumb);
}
