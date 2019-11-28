package com.databasepreservation.common.client.common.visualization.browse.technicalInformation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.widgets.wcag.MarkdownWidgetWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ReportPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, ReportPanel> instances = new HashMap<>();

  public static ReportPanel getInstance(ViewerDatabase database) {
    String code = database.getUuid();

    ReportPanel instance = instances.get(code);
    if (instance == null) {
      instance = new ReportPanel(database);
      instances.put(code, instance);
    }
    return instance;
  }

  interface ReportPanelUiBinder extends UiBinder<Widget, ReportPanel> {
  }

  private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  Label title;

  @UiField(provided = true)
  MarkdownWidgetWrapper contentItems;

  private ReportPanel(ViewerDatabase database) {
    this.database = database;
    this.contentItems = new MarkdownWidgetWrapper(database.getUuid());

    initWidget(uiBinder.createAndBindUi(this));

    title.setText(messages.titleReport());
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
          BreadcrumbManager.forDatabaseReport(database.getUuid(), database.getMetadata().getName()));
  }
}
