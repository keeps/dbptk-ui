package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class ColumnOptionsPanel extends Composite {

  public abstract TemplateStatus getSearchTemplate();

  public abstract TemplateStatus getDetailsTemplate();

  public abstract TemplateStatus getExportTemplate();
}
