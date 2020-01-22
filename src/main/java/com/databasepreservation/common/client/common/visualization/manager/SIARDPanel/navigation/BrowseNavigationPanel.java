package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
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
  private Button btnIngest;
  private Button btnAdvancedConfiguration;
  private boolean btnIngestClicked = false;
  private MetadataField browsingStatus = null;

  public static BrowseNavigationPanel getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new BrowseNavigationPanel(database));
  }

  private BrowseNavigationPanel(ViewerDatabase database) {
    this.database = database;
  }

  private void browseButton() {
    // Browser now Button
    btnBrowse = new Button();
    btnBrowse.setText(messages.SIARDHomePageButtonTextForBrowseNow());
    btnBrowse.addStyleName("btn btn-link-info");
    btnBrowse.setVisible(false);

    btnBrowse.addClickHandler(event -> {
      HistoryManager.gotoDatabase(database.getUuid());
    });
  }

  private void deleteButton() {
    btnDelete = new Button();
    btnDelete.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
    btnDelete.addStyleName("btn btn-link-info");
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

  private void ingestButton() {
    btnIngest = new Button();
    btnIngest.setText(messages.SIARDHomePageButtonTextForBrowseNow());
    btnIngest.addStyleName("btn btn-link-info");
    btnIngest.setVisible(false);

    btnIngest.addClickHandler(event -> {
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
    });
  }

  private void advancedConfigurationButton() {
    btnAdvancedConfiguration = new Button();
    btnAdvancedConfiguration.setText("Configuration");
    btnAdvancedConfiguration.addStyleName("btn btn-link-info");
    btnAdvancedConfiguration.setVisible(true);

    btnAdvancedConfiguration.addClickHandler(event -> {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    });
  }

  public NavigationPanel build() {
    // Browser now Button
    browseButton();

    // Delete Button
    deleteButton();

    // Ingest Button
    ingestButton();

    advancedConfigurationButton();

    NavigationPanel browse = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForBrowsing());

    browse.addButton(btnIngest);
    browse.addButton(btnBrowse);
    browse.addButton(btnDelete);
    browse.addButton(btnAdvancedConfiguration);

    if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
      btnAdvancedConfiguration.setVisible(true);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.ERROR)) {
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
      btnAdvancedConfiguration.setVisible(false);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.METADATA_ONLY)) {
      btnIngest.setVisible(true);
      btnDelete.setVisible(false);
      btnAdvancedConfiguration.setVisible(false);
    }

    if (database.getPath() == null || database.getPath().isEmpty()) {
      btnIngest.setVisible(false);
      btnIngestClicked = false;
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
      btnIngest.setVisible(false);
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
      btnAdvancedConfiguration.setVisible(true);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.ERROR)) {
      btnIngest.setVisible(false);
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
      btnAdvancedConfiguration.setVisible(false);
    } else if (database.getStatus().equals(ViewerDatabaseStatus.INGESTING)) {
      if (btnIngestClicked) {
        btnIngest.setVisible(true);
        btnIngest.setText(messages.SIARDHomePageButtonTextForStartIngest());
        btnBrowse.setVisible(false);
        btnAdvancedConfiguration.setVisible(false);
        btnIngest.addClickHandler(
          event -> HistoryManager.gotoIngestSIARDData(database.getUuid(), database.getMetadata().getName()));
      }
    } else if (database.getStatus().equals(ViewerDatabaseStatus.METADATA_ONLY)) {
      btnIngest.setVisible(true);
      btnBrowse.setVisible(false);
      btnDelete.setVisible(false);
      btnAdvancedConfiguration.setVisible(false);
      btnIngestClicked = false;
    }

    if (database.getPath() == null || database.getPath().isEmpty()) {
      btnIngest.setEnabled(false);
      btnIngest.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
      btnIngestClicked = false;
    } else {
      btnIngest.setEnabled(true);
      btnIngest.setTitle(null);
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
