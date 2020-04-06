package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.databasepreservation.common.client.models.configuration.collection.ViewerTemplateConfiguration;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class ColumnOptionsPanel extends Composite {

  public abstract ViewerTemplateConfiguration getSearchTemplate();

  public abstract ViewerTemplateConfiguration getDetailsTemplate();

  public abstract ViewerTemplateConfiguration getExportTemplate();
}
