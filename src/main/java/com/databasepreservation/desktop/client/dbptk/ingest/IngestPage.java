package com.databasepreservation.desktop.client.dbptk.ingest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.desktop.client.dbptk.wizard.common.progressBar.ProgressBarPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IngestPage extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface IngestPageUiBinder extends UiBinder<Widget, IngestPage> {
  }

  private static IngestPageUiBinder binder = GWT.create(IngestPageUiBinder.class);
  private static Map<String, IngestPage> instances = new HashMap<>();

  public static IngestPage getInstance(String databaseUUID, String databaseName) {

    if (instances.get(databaseUUID) == null) {
      IngestPage instance = new IngestPage(databaseUUID, databaseName);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  @UiField
  FlowPanel container, panel;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  Button btnBack;

  private IngestPage(final String databaseUUID, final String databaseName) {
    initWidget(binder.createAndBindUi(this));

    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDIngesting(databaseUUID, databaseName);
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

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
