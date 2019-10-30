package com.databasepreservation.common.shared.client.common.visualization.browse.manager.SIARDPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.ContentPanel;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.LoadingDiv;
import com.databasepreservation.common.shared.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.visualization.browse.manager.SIARDPanel.navigation.BrowseNavigationPanel;
import com.databasepreservation.common.shared.client.common.visualization.browse.manager.SIARDPanel.navigation.MetadataNavigationPanel;
import com.databasepreservation.common.shared.client.common.visualization.browse.manager.SIARDPanel.navigation.SIARDNavigationPanel;
import com.databasepreservation.common.shared.client.common.visualization.browse.manager.SIARDPanel.navigation.ValidationNavigationPanel;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class SIARDManagerPage extends ContentPanel {
  interface SIARDManagerPageUiBinder extends UiBinder<Widget, SIARDManagerPage> {
  }

  private static SIARDManagerPageUiBinder binder = GWT.create(SIARDManagerPageUiBinder.class);
  private static Map<String, SIARDManagerPage> instances = new HashMap<>();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private BreadcrumbPanel breadcrumb;
  private ViewerDatabase database;
  private String validateAtHumanized = null;
  private String archivalDateHumanized = null;
  private MetadataNavigationPanel metadataNavigationPanel = null;
  private SIARDNavigationPanel siardNavigationPanel = null;
  private ValidationNavigationPanel validationNavigationPanel = null;
  private BrowseNavigationPanel browseNavigationPanel = null;
  private Boolean populationFieldsCompleted = false;

  @UiField
  LoadingDiv loading;

  @UiField
  FlowPanel metadataInformation, navigationPanels;

  @UiField
  Button btnBack, btnExclude;

  public static SIARDManagerPage getInstance(ViewerDatabase database) {
    String databaseUUID = database.getUUID();
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new SIARDManagerPage(database));
    }
    return instances.get(databaseUUID);
  }

  private SIARDManagerPage(ViewerDatabase database) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;

    loading.setVisible(true);

    BrowserService.Util.getInstance().getDateTimeHumanized(database.getValidatedAt(),
      new DefaultAsyncCallback<String>() {
        @Override
        public void onSuccess(String result) {
          validateAtHumanized = result;
          BrowserService.Util.getInstance().getDateTimeHumanized(database.getMetadata().getArchivalDate(),
            new DefaultAsyncCallback<String>() {
              @Override
              public void onSuccess(String result) {
                archivalDateHumanized = result;
                loading.setVisible(false);
                populateMetadataInfo();
                populateNavigationPanels();
                setupFooterButtons();
                populationFieldsCompleted = true;
              }
            });
        }
      });
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    this.breadcrumb = breadcrumb;
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDMainPage(database.getUUID(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  private void populateMetadataInfo() {
    metadataNavigationPanel = MetadataNavigationPanel.getInstance(database, archivalDateHumanized);
    metadataInformation.add(metadataNavigationPanel.build());
    metadataInformation.add(metadataNavigationPanel.buildDescription());
  }

  private void populateNavigationPanels() {
    siardNavigationPanel = SIARDNavigationPanel.getInstance(database);
    navigationPanels.add(siardNavigationPanel.build());

    validationNavigationPanel = ValidationNavigationPanel.getInstance(database, validateAtHumanized);
    navigationPanels.add(validationNavigationPanel.build());

    browseNavigationPanel = BrowseNavigationPanel.getInstance(database);
    navigationPanels.add(browseNavigationPanel.build());
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (database != null && populationFieldsCompleted) {
      refreshInstance(database.getUUID());
    }
  }

  public void refreshInstance(String databaseUUID) {
    GWT.log("refreshInstance");
    loading.setVisible(true);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          metadataNavigationPanel.update(database);
          siardNavigationPanel.update(database);
          browseNavigationPanel.update(database);
          validationNavigationPanel.update(database);

          List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDMainPage(databaseUUID,
            database.getMetadata().getName());
          BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

          loading.setVisible(false);
        }
      });
  }

  private void setupFooterButtons() {
    btnBack.setText(messages.basicActionBack());
    btnExclude.setText(messages.basicActionDelete());

    btnBack.addClickHandler(event -> {
      HistoryManager.gotoDatabase();
    });

    btnExclude.addClickHandler(event -> {
      if (ViewerDatabase.Status.AVAILABLE.equals(database.getStatus())
        || ViewerDatabase.Status.ERROR.equals(database.getStatus())
        || ViewerDatabase.Status.METADATA_ONLY.equals(database.getStatus())) {
        SafeHtml message = messages.SIARDHomePageTextForDeleteAllFromServer();;
        if(ApplicationType.getType().equals(ViewerConstants.DESKTOP)){
          message = messages.SIARDHomePageTextForDeleteAllFromDesktop();
        }
        CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDelete(),
          message, messages.basicActionCancel(), messages.basicActionConfirm(),
          CommonDialogs.Level.DANGER, "500px", new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
              if (result) {
                deleteAll();
              }
            }
          });
      } else if (ViewerDatabase.Status.INGESTING.equals(database.getStatus())) {
        Dialogs.showInformationDialog(messages.SIARDManagerPageInformationDialogTitle(), messages.SIARDManagerPageTextForWaitForFinishing(), messages.basicActionClose(), "btn btn-link");
      }
    });
  }

  private void deleteAll() {
    BrowserService.Util.getInstance().deleteAll(database.getUUID(), new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {

        }

        @Override
        public void onSuccess(Boolean result) {
          HistoryManager.gotoDatabase();
        }
      });
  }
}