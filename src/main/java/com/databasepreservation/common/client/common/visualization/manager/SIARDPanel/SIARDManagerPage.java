/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.BrowseNavigationPanel;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.MetadataNavigationPanel;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.PermissionsNavigationPanel;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.SIARDNavigationPanel;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation.ValidationNavigationPanel;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroup;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.services.ContextService;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class SIARDManagerPage extends ContentPanel {
  interface SIARDManagerPageUiBinder extends UiBinder<Widget, SIARDManagerPage> {
  }

  private static SIARDManagerPageUiBinder binder = GWT.create(SIARDManagerPageUiBinder.class);
  private static Map<String, SIARDManagerPage> instances = new HashMap<>();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerDatabase database;
  private MetadataNavigationPanel metadataNavigationPanel = null;
  private SIARDNavigationPanel siardNavigationPanel = null;
  private ValidationNavigationPanel validationNavigationPanel = null;
  private BrowseNavigationPanel browseNavigationPanel = null;

  private PermissionsNavigationPanel permissionsNavigationPanel = null;
  private Boolean populationFieldsCompleted;
  private boolean initialized = false;

  @UiField
  LoadingDiv loading;

  @UiField
  FlowPanel mainHeader;

  @UiField
  FlowPanel description;

  @UiField
  FlowPanel metadataInformation;

  @UiField
  FlowPanel navigationPanels;

  @UiField
  Button btnBack;

  @UiField
  Button btnDeleteAll;

  public static SIARDManagerPage getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new SIARDManagerPage(database));
  }

  private SIARDManagerPage(ViewerDatabase database) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    loading.setVisible(true);
    init();
  }

  private void init() {
    createHeader();

    Label label = new Label();
    label.addStyleName("text-muted font-size-small");
    label.setText("(" + database.getUuid() + ")");

    description.add(label);

    MetadataField instance = MetadataField.createInstance(database.getMetadata().getDescription());
    instance.setCSS("table-row-description", "font-size-description");

    description.add(instance);

    populateMetadataInfo();
    populateNavigationPanels();
    setupFooterButtons();
    populationFieldsCompleted = true;
    loading.setVisible(false);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDMainPage(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  private void populateMetadataInfo() {
    metadataNavigationPanel = MetadataNavigationPanel.getInstance(database);
    metadataInformation.add(metadataNavigationPanel.build());
  }

  private void populateNavigationPanels() {
    siardNavigationPanel = SIARDNavigationPanel.getInstance(database);
    navigationPanels.add(siardNavigationPanel.build());

    validationNavigationPanel = ValidationNavigationPanel.getInstance(database);
    navigationPanels.add(validationNavigationPanel.build());

    browseNavigationPanel = BrowseNavigationPanel.getInstance(database);
    navigationPanels.add(browseNavigationPanel.build());

    if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      DatabaseService.Util.call((Set<String> databasePermissions) -> {
        ContextService.Util.call((Set<AuthorizationGroup> authorizationGroups) -> {
          permissionsNavigationPanel = PermissionsNavigationPanel.getInstance(database, databasePermissions,
            authorizationGroups);
          if (permissionsNavigationPanel.hasPermissionsOrGroups()) {
            navigationPanels.add(permissionsNavigationPanel.build());
          }
        }).getAuthorizationGroupsList();
      }).getDatabasePermissions(database.getUuid());
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (!initialized) {
      initialized = true;
    } else {
      if (database != null && populationFieldsCompleted) {
        refreshInstance(database.getUuid());
      }
    }
  }

  public void refreshInstance(String databaseUUID) {
    loading.setVisible(true);

    DatabaseService.Util.call((IsIndexed result) -> {
      database = (ViewerDatabase) result;
      createHeader();
      metadataNavigationPanel.update(database);
      siardNavigationPanel.update(database);
      browseNavigationPanel.update(database);
      validationNavigationPanel.update(database);

      if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
        if (permissionsNavigationPanel.hasPermissionsOrGroups()) {
          DatabaseService.Util.call((Set<String> databasePermissions) -> {
            permissionsNavigationPanel.update(databasePermissions);
          }).getDatabasePermissions(database.getUuid());
        }
      }

      loading.setVisible(false);
    }).retrieve(databaseUUID);
  }

  private void createHeader() {
    mainHeader.clear();
    mainHeader.add(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.BOX_OPEN),
      database.getMetadata().getName(), "h1"));
  }

  private void setupFooterButtons() {
    btnBack.setText(messages.basicActionBack());
    btnDeleteAll.setText(messages.basicActionDelete());

    btnBack.addClickHandler(event -> HistoryManager.gotoDatabase());

    btnDeleteAll.addClickHandler(event -> {
      if (ViewerDatabaseStatus.AVAILABLE.equals(database.getStatus())
        || ViewerDatabaseStatus.ERROR.equals(database.getStatus())
        || ViewerDatabaseStatus.METADATA_ONLY.equals(database.getStatus())) {
        SafeHtml message = messages.SIARDHomePageTextForDeleteAllFromServer();
        if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
          message = messages.SIARDHomePageTextForDeleteAllFromDesktop();
        }
        if (database.getVersion().equals(ViewerConstants.SIARD_DK_1007)
          || database.getVersion().equals(ViewerConstants.SIARD_DK_128)) {
          Dialogs.showInformationDialog(messages.SIARDHomePageDialogTitleForDelete(),
            messages.SIARDHomePageDialogTextForDeleteNotAvailable(ViewerConstants.SIARD_V21),
            messages.basicActionUnderstood(), "btn btn-link");
        } else {
          CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDelete(), message,
            messages.basicActionCancel(), messages.basicActionConfirm(), CommonDialogs.Level.DANGER, "500px",
            new DefaultAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  deleteAll();
                }
              }
            });
        }
      } else if (ViewerDatabaseStatus.INGESTING.equals(database.getStatus())) {
        Dialogs.showInformationDialog(messages.SIARDManagerPageInformationDialogTitle(),
          messages.SIARDManagerPageTextForWaitForFinishing(), messages.basicActionClose(), "btn btn-link");
      }
    });
  }

  private void deleteAll() {
    DatabaseService.Util.call((Boolean success) -> HistoryManager.gotoDatabase()).delete(database.getUuid());
  }
}
