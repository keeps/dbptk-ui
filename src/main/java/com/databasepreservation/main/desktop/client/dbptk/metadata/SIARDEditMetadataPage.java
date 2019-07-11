package com.databasepreservation.main.desktop.client.dbptk.metadata;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.sidebar.MetadataEditSidebar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import com.google.gwt.core.client.GWT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @autor Gabriel Barros <gbarros@keep.pt>
 */
public class SIARDEditMetadataPage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDEditMetadataPageUiBinder extends UiBinder<Widget, SIARDEditMetadataPage> {
  }

  private static SIARDEditMetadataPageUiBinder binder = GWT.create(SIARDEditMetadataPageUiBinder.class);
  private static Map<String, SIARDEditMetadataPage> instances = new HashMap<>();
  private String databaseUUID;
  private ViewerDatabase database = null;
  private Map<String, String> SIARDbundle = new HashMap<>();

  public static SIARDEditMetadataPage getInstance(String databaseUUID) {

    SIARDEditMetadataPage instance = instances.get(databaseUUID);
    if (instance == null) {
      GWT.log("SIARDEditMetadataPage, getInstance:::" + databaseUUID);
      instance = new SIARDEditMetadataPage(databaseUUID);
      instances.put(databaseUUID, instance);
    }
    return instance;
  }

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  MetadataEditSidebar sidebar;

  @UiField
  SimplePanel rightPanelContainer;

  @UiField
  Button buttonCommit;

  @UiField
  LoadingDiv loading;


  private SIARDEditMetadataPage(final String databaseUUID) {
    this.databaseUUID = databaseUUID;
    this.sidebar = MetadataEditSidebar.getInstance(databaseUUID);

    initWidget(binder.createAndBindUi(this));

    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDEditMetadataPage(databaseUUID);
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  public void load(MetadataPanelLoad rightPanelLoader){
    GWT.log("load. uuid: " + databaseUUID + ", database: " + database);

    if (databaseUUID != null && (database == null || !ViewerDatabase.Status.METADATA_ONLY.equals(database.getStatus()))) {
      GWT.log("getting db");
      loadPanelWithDatabase(rightPanelLoader);
    } else {
      loadPanel(rightPanelLoader);
    }
  }

  private void loadPanelWithDatabase(MetadataPanelLoad rightPanelLoader) {
    GWT.log("loadPanelWithDatabase");
    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught){}

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          loadPanel(rightPanelLoader);
        }
      });
  }

  private void loadPanel(MetadataPanelLoad rightPanelLoader){
    GWT.log("loadPanel");
    GWT.log("have db: " + database + "sb.init: " + sidebar.isInitialized());
    MetadataPanel rightPanel = rightPanelLoader.load(database, SIARDbundle);

    if (database != null && !sidebar.isInitialized()) {
      sidebar.init(database);
    }

    if (rightPanel != null) {
      rightPanel.handleBreadcrumb(breadcrumb);
      rightPanelContainer.setWidget(rightPanel);
      rightPanel.setVisible(true);
    }

  }

  
  @UiHandler("buttonCommit")
  void buttonSaveHandler(ClickEvent e) {
    ViewerMetadata metadata = database.getMetadata();

    loading.setVisible(true);

    BrowserService.Util.getInstance().updateMetadataInformation( metadata, SIARDbundle, database.getUUID(),
      database.getSIARDPath(), new DefaultAsyncCallback<ViewerMetadata>() {
        @Override
        public void onFailure(Throwable caught) {
          // TODO: error handling
          Toast.showError("Metadata Update", database.getMetadata().getName());
          loading.setVisible(false);
        }

        @Override
        public void onSuccess(ViewerMetadata result) {
          loading.setVisible(false);
          Toast.showInfo("Metadata Update", "Success");
        }
      });

  }
}
