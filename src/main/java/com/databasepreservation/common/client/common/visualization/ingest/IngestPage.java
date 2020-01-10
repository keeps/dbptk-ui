package com.databasepreservation.common.client.common.visualization.ingest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.common.visualization.progressBar.ProgressBarPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IngestPage extends ContentPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface IngestPageUiBinder extends UiBinder<Widget, IngestPage> {
  }

  private static IngestPageUiBinder binder = GWT.create(IngestPageUiBinder.class);
  private static Map<String, IngestPage> instances = new HashMap<>();
  private String databaseUUID;
  private String databaseName;

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDIngesting(databaseUUID, databaseName);
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  public static IngestPage getInstance(ViewerDatabase database) {
    String databaseUUID = database.getUuid();
    String databaseName = database.getMetadata().getName();
    instances.computeIfAbsent(databaseUUID, k -> new IngestPage(databaseUUID, databaseName));
    return instances.get(databaseUUID);
  }

  @UiField
  FlowPanel container;

  @UiField
  FlowPanel panel;

  @UiField
  Button btnBack;

  private IngestPage(final String databaseUUID, final String databaseName) {
    initWidget(binder.createAndBindUi(this));
    this.databaseUUID = databaseUUID;
    this.databaseName = databaseName;

    final ProgressBarPanel instance = ProgressBarPanel.getInstance(databaseUUID);
    instance.setTitleText(messages.SIARDHomePageTextForIngestSIARDTitle());
    instance.setSubtitleText(messages.SIARDHomePageTextForIngestSIARDSubtitle());
    
    panel.add(instance);

    configureBtnBack(databaseUUID);
  }

  private void configureBtnBack(final String databaseUUID) {
    btnBack.setText(messages.basicActionBack());
    btnBack.addClickHandler(event -> HistoryManager.gotoSIARDInfo(databaseUUID));
  }
}
