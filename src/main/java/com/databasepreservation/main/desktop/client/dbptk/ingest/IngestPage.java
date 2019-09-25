package com.databasepreservation.main.desktop.client.dbptk.ingest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.progressBar.ProgressBarIngestPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.progressBar.ProgressBarPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

  private IngestPage(final String databaseUUID, final String databaseName) {
    initWidget(binder.createAndBindUi(this));

    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDIngesting(databaseUUID, databaseName);
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

    final ProgressBarPanel instance = ProgressBarIngestPanel.getInstance(databaseUUID);
    // final UploadPanel instance = UploadPanel.createInstance(databaseUUID);
    instance.setTitleText(messages.SIARDHomePageTextForIngestSIARDTitle());
    instance.setSubtitleText(messages.SIARDHomePageTextForIngestSIARDSubtitle());

    GWT.log(databaseUUID);

    panel.add(instance);
  }

}
