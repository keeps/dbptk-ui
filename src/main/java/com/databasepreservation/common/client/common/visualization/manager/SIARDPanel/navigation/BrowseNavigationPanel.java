package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;

import config.i18n.client.ClientMessages;

public class BrowseNavigationPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, BrowseNavigationPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private Button btnDelete;
  private Button btnBrowse;
  private Button btnConfiguration;
  private boolean btnIngestClicked = false;
  private MetadataField browsingStatus = null;

  public static BrowseNavigationPanel getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new BrowseNavigationPanel(database));
  }

  private BrowseNavigationPanel(ViewerDatabase database) {
    this.database = database;
  }

  private void browseButton() {
    btnBrowse = new Button();
    btnBrowse.setText(messages.SIARDHomePageButtonTextForBrowse());
    btnBrowse.addStyleName("btn btn-outline-primary btn-play");
    btnBrowse.setVisible(false);

    if (database.getPath() != null && !database.getPath().isEmpty()) {
      btnBrowse.addClickHandler(e -> handleBrowseAction());
    } else {
      if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
        btnBrowse.addClickHandler(e -> handleBrowseAction());
      }
    }

  }

  private void handleBrowseAction() {
    if (database.getStatus().equals(ViewerDatabaseStatus.METADATA_ONLY)) { // Initial state
      if (database.getVersion().equals(ViewerConstants.SIARD_V21)) {
        if (!btnIngestClicked) {
          btnIngestClicked = true;

          HistoryManager.gotoIngestSIARDData(database.getUuid(), database.getMetadata().getName());
          CollectionService.Util.call((String databaseUUID) -> {
            HistoryManager.gotoDatabase(databaseUUID);
            Dialogs.showInformationDialog(messages.SIARDHomePageDialogTitleForBrowsing(),
              messages.SIARDHomePageTextForIngestSuccess(), messages.basicActionClose(), "btn btn-link");
          }, (String errorMessage) -> {
            instances.clear();
            HistoryManager.gotoSIARDInfo(database.getUuid());
            Dialogs.showErrors(messages.SIARDHomePageDialogTitleForBrowsing(), errorMessage,
              messages.basicActionClose());
          }).createCollection(database.getUuid());
        }
      } else {
        Dialogs.showInformationDialog(messages.SIARDHomePageDialogTitleForBrowsing(),
          messages.SIARDHomePageTextForIngestNotSupported(), messages.basicActionUnderstood(), "btn btn-link");
      }
    } else if (database.getStatus().equals(ViewerDatabaseStatus.INGESTING)) { // Ingest the data
      HistoryManager.gotoIngestSIARDData(database.getUuid(), database.getMetadata().getName());
    } else if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) { // show the data
      HistoryManager.gotoDatabase(database.getUuid());
    }
  }

  private void deleteButton() {
    btnDelete = new Button();
    btnDelete.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
    btnDelete.addStyleName("btn btn-outline-danger btn-delete");
    btnDelete.setVisible(false);

    btnDelete.addClickHandler(event -> {
      if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)
        || database.getStatus().equals(ViewerDatabaseStatus.ERROR)) {
        CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDeleteBrowseContent(),
          messages.SIARDHomePageTextForDeleteFromSolr(), messages.basicActionCancel(), messages.basicActionConfirm(),
          CommonDialogs.Level.DANGER, "500px", new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
              if (result) {
                delete();
              }
            }
          });
      }
    });
  }

  private void advancedConfigurationButton() {
    btnConfiguration = new Button();
    btnConfiguration.setText("Configuration");
    btnConfiguration.addStyleName("btn btn-outline-primary btn-configuration");
    btnConfiguration.setVisible(false);

    btnConfiguration.addClickHandler(event -> {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    });
  }

  public NavigationPanel build() {
    // Browser now Button
    browseButton();

    // Delete Button
    deleteButton();

    advancedConfigurationButton();

    NavigationPanel browse = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForBrowsing());

    browse.addToDescriptionPanel(messages.SIARDHomePageOptionsDescriptionForBrowse());
    browse.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnBrowse));
    browse.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnConfiguration));
    browse.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnDelete));

    if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
      btnDelete.setVisible(true);
      btnConfiguration.setVisible(true);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.ERROR)) {
      btnDelete.setVisible(true);
      btnConfiguration.setVisible(false);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.METADATA_ONLY)) {
      btnDelete.setVisible(false);
      btnConfiguration.setVisible(false);
    }
    btnBrowse.setVisible(true);

    if (!database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
      if (database.getPath() == null || database.getPath().isEmpty()) {
        btnIngestClicked = false;
        btnBrowse.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
        btnBrowse.setEnabled(false);
      }
    }

    browsingStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForBrowseStatus(),
      LabelUtils.getDatabaseStatus(database.getStatus()));
    browsingStatus.setCSS(null, "label-field", "value-field");

    browse.addToInfoPanel(browsingStatus);

    return browse;
  }

  public void update(ViewerDatabase database) {
    this.database = database;
    browsingStatus.updateText(LabelUtils.getDatabaseStatus(database.getStatus()));

    if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
      btnConfiguration.setVisible(true);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.ERROR)) {
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
      btnConfiguration.setVisible(false);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.METADATA_ONLY)) {
      btnBrowse.setVisible(true);
      btnDelete.setVisible(false);
      btnConfiguration.setVisible(false);
      btnIngestClicked = false;
    }

    if (!database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
      if (database.getPath() == null || database.getPath().isEmpty()) {
        btnIngestClicked = false;
        btnBrowse.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
        btnBrowse.setEnabled(false);
      } else {
        btnBrowse.setEnabled(true);
        btnBrowse.setTitle("");
      }
    }
  }

  private void delete() {
    if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)
      || database.getStatus().equals(ViewerDatabaseStatus.ERROR)) {
      CollectionService.Util.call((Boolean result) -> {
        SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid());
      }).deleteCollection(database.getUuid());
    }
  }
}
