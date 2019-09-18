package com.databasepreservation.main.desktop.client.dbptk.ingest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.UploadPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
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

  public static IngestPage getInstance(String databaseUUID) {

    if (instances.get(databaseUUID) == null) {
      IngestPage instance = new IngestPage(databaseUUID);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  @UiField
  FlowPanel container, panel;

  @UiField
  BreadcrumbPanel breadcrumb;

  private IngestPage(final String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          ViewerDatabase database = (ViewerDatabase) result;

          List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDMainPage(databaseUUID,
            database.getMetadata().getName());
          BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

          final UploadPanel instance = UploadPanel.createInstance(database);
          instance.setTitleText(messages.SIARDHomePageTextForIngestSIARDTitle());
          instance.setSubtitleText(messages.SIARDHomePageTextForIngestSIARDSubtitle());

          GWT.log(database.getUUID());

          panel.add(instance);
        }
      });
  }
}