package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.BrowseNavigationPanel;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.MetadataNavigationPanel;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.SIARDNavigationPanel;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.ValidationNavigationPanel;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.services.DatabaseService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SIARDManagerPage extends ContentPanel {
  interface SIARDManagerPageUiBinder extends UiBinder<Widget, SIARDManagerPage> {
  }

  private static SIARDManagerPageUiBinder binder = GWT.create(SIARDManagerPageUiBinder.class);
  private static Map<String, SIARDManagerPage> instances = new HashMap<>();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private BreadcrumbPanel breadcrumb;
  private ViewerDatabase database;
  private MetadataNavigationPanel metadataNavigationPanel = null;
  private SIARDNavigationPanel siardNavigationPanel = null;
  private ValidationNavigationPanel validationNavigationPanel = null;
  private BrowseNavigationPanel browseNavigationPanel = null;
  private Boolean populationFieldsCompleted = false;

  @UiField
  LoadingDiv loading;

  @UiField
  FlowPanel metadataInformation;

  @UiField
  FlowPanel navigationPanels;

  @UiField
  Button btnBack;

  @UiField
  Button btnExclude;

  public static SIARDManagerPage getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new SIARDManagerPage(database));
  }

  private SIARDManagerPage(ViewerDatabase database) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    loading.setVisible(true);
    populateMetadataInfo();
    populateNavigationPanels();
    setupFooterButtons();
    populationFieldsCompleted = true;
    loading.setVisible(false);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    this.breadcrumb = breadcrumb;
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDMainPage(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  private void populateMetadataInfo() {
    metadataNavigationPanel = MetadataNavigationPanel.getInstance(database);
    metadataInformation.add(metadataNavigationPanel.build());
    metadataInformation.add(metadataNavigationPanel.buildDescription());
  }

  private void populateNavigationPanels() {
    siardNavigationPanel = SIARDNavigationPanel.getInstance(database);
    navigationPanels.add(siardNavigationPanel.build());

    validationNavigationPanel = ValidationNavigationPanel.getInstance(database);
    navigationPanels.add(validationNavigationPanel.build());

    browseNavigationPanel = BrowseNavigationPanel.getInstance(database);
    navigationPanels.add(browseNavigationPanel.build());
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (database != null && populationFieldsCompleted) {
      refreshInstance(database.getUuid());
    }
  }

  public void refreshInstance(String databaseUUID) {
    GWT.log("refreshInstance");
    loading.setVisible(true);

    DatabaseService.Util.call((IsIndexed result) -> {
      database = (ViewerDatabase) result;
      metadataNavigationPanel.update(database);
      siardNavigationPanel.update(database);
      browseNavigationPanel.update(database);
      validationNavigationPanel.update(database);

      List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDMainPage(databaseUUID,
          database.getMetadata().getName());
      BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

      loading.setVisible(false);
    }).retrieve(databaseUUID, databaseUUID);
  }

  private void setupFooterButtons() {
    btnBack.setText(messages.basicActionBack());
    btnExclude.setText(messages.basicActionDelete());

    btnBack.addClickHandler(event -> HistoryManager.gotoDatabase());

    btnExclude.addClickHandler(event -> {
      if (ViewerDatabaseStatus.AVAILABLE.equals(database.getStatus())
        || ViewerDatabaseStatus.ERROR.equals(database.getStatus())
        || ViewerDatabaseStatus.METADATA_ONLY.equals(database.getStatus())) {
        SafeHtml message = messages.SIARDHomePageTextForDeleteAllFromServer();
        if(ApplicationType.getType().equals(ViewerConstants.DESKTOP)){
          message = messages.SIARDHomePageTextForDeleteAllFromDesktop();
        }
        CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDelete(),
          message, messages.basicActionCancel(), messages.basicActionConfirm(),
          CommonDialogs.Level.DANGER, "500px", new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
              if (Boolean.TRUE.equals(result)) {
                deleteAll();
              }
            }
          });
      } else if (ViewerDatabaseStatus.INGESTING.equals(database.getStatus())) {
        Dialogs.showInformationDialog(messages.SIARDManagerPageInformationDialogTitle(), messages.SIARDManagerPageTextForWaitForFinishing(), messages.basicActionClose(), "btn btn-link");
      }
    });
  }

  private void deleteAll() {
    DatabaseService.Util.call((Boolean success) -> HistoryManager.gotoDatabase()).deleteDatabase(database.getUuid());
  }
}