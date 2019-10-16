package com.databasepreservation.common.shared.client.common.visualization.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.widgets.wcag.MarkdownWidgetWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseReportPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DatabaseReportPanel> instances = new HashMap<>();

  public static DatabaseReportPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseReportPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseReportPanel(database);
      instances.put(code, instance);
    }
    return instance;
  }

  interface ReportPanelUiBinder extends UiBinder<Widget, DatabaseReportPanel> {
  }

  private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  Label title;

  @UiField(provided = true)
  MarkdownWidgetWrapper contentItems;

  private DatabaseReportPanel(ViewerDatabase database) {
    this.database = database;
    this.contentItems = new MarkdownWidgetWrapper(database.getUUID());

    initWidget(uiBinder.createAndBindUi(this));

    title.setText(messages.titleReport());
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
          BreadcrumbManager.forDatabaseReport(database.getUUID(), database.getMetadata().getName()));
  }
}
