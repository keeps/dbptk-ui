/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.technicalInformation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.widgets.wcag.MarkdownWidgetWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ReportPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ReportPanelUiBinder extends UiBinder<Widget, ReportPanel> {
  }

  private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);

  @UiField
  FlowPanel header;

  @UiField
  SimplePanel description;

  @UiField
  FlowPanel content;

  private static Map<String, ReportPanel> instances = new HashMap<>();
  private ViewerDatabase database;

  public static ReportPanel getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new ReportPanel(database));
  }

  private ReportPanel(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));
    this.database = database;

    content.add(new MarkdownWidgetWrapper(database.getUuid()));
    configureHeader();
  }

  private void configureHeader() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_REPORT),
      messages.titleReport(), "h1"));

    HTML html = new HTML();
    html.addStyleName("font-size-description");

    description.setWidget(html);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
          BreadcrumbManager.forDatabaseReport(database.getUuid(), database.getMetadata().getName()));
  }
}
