package com.databasepreservation.common.shared.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.NavigationPanel;
import com.databasepreservation.common.shared.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.common.shared.client.common.fields.MetadataField;
import com.databasepreservation.common.shared.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.SolrHumanizer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;

import config.i18n.client.ClientMessages;

public class BrowseNavigationPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, BrowseNavigationPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private Button btnDelete;
  private Button btnBrowse;
  private Button btnIngest;
  private boolean btnIngestClicked = false;
  private MetadataField browsingStatus = null;

  public static BrowseNavigationPanel getInstance(ViewerDatabase database) {
    String databaseUUID = database.getUUID();
    instances.computeIfAbsent(databaseUUID, k -> new BrowseNavigationPanel(database));
    return instances.get(databaseUUID);
  }

  private BrowseNavigationPanel(ViewerDatabase database) {
    this.database = database;
  }

  public NavigationPanel build() {

    // Browser now Button
    btnBrowse = new Button();
    btnBrowse.setText(messages.SIARDHomePageButtonTextForBrowseNow());
    btnBrowse.addStyleName("btn btn-link-info");
    btnBrowse.setVisible(false);

    btnBrowse.addClickHandler(event -> {
      HistoryManager.gotoDatabase(database.getUUID());
    });

    // Delete Button
    btnDelete = new Button();
    btnDelete.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
    btnDelete.addStyleName("btn btn-link-info");
    btnDelete.setVisible(false);

    btnDelete.addClickHandler(event -> {
      if (database.getStatus().equals(ViewerDatabase.Status.AVAILABLE)
        || database.getStatus().equals(ViewerDatabase.Status.ERROR)) {
        CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDelete(),
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

    // Ingest Button
    btnIngest = new Button();
    btnIngest.setText(messages.SIARDHomePageButtonTextForBrowseNow());
    btnIngest.addStyleName("btn btn-link-info");
    btnIngest.setVisible(false);

    btnIngest.addClickHandler(event -> {
      if (database.getSIARDVersion().equals(ViewerConstants.SIARD_V21)) {

        if (!btnIngestClicked) {
          btnIngestClicked = true;

          HistoryManager.gotoIngestSIARDData(database.getUUID(), database.getMetadata().getName());
          BrowserService.Util.getInstance().uploadSIARD(database.getSIARDPath(), database.getUUID(),
            new DefaultAsyncCallback<String>() {
              @Override
              public void onFailure(Throwable caught) {
                instances.clear();
                HistoryManager.gotoSIARDInfo(database.getUUID());
                Dialogs.showErrors(messages.SIARDHomePageDialogTitleForBrowsing(), caught.getMessage(),
                  messages.basicActionClose());
              }

              @Override
              public void onSuccess(String databaseUUID) {
                HistoryManager.gotoDatabase(databaseUUID);
                Dialogs.showInformationDialog(messages.SIARDHomePageDialogTitleForBrowsing(),
                  messages.SIARDHomePageTextForIngestSuccess(), messages.basicActionClose(), "btn btn-link");
              }
            });
        }
      } else {
        Dialogs.showInformationDialog(messages.SIARDHomePageDialogTitleForBrowsing(),
          messages.SIARDHomePageTextForIngestNotSupported(), messages.basicActionUnderstood(), "btn btn-link");
      }
    });

    NavigationPanel browse = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForBrowsing());

    browse.addButton(btnIngest);
    browse.addButton(btnBrowse);
    browse.addButton(btnDelete);

    if (database.getStatus().equals(ViewerDatabase.Status.AVAILABLE)
      || database.getStatus().equals(ViewerDatabase.Status.ERROR)) {
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
    } else if (database.getStatus().equals(ViewerDatabase.Status.METADATA_ONLY)) {
      btnIngest.setVisible(true);
      btnDelete.setVisible(false);
    }

    if (database.getSIARDPath() == null || database.getSIARDPath().isEmpty()) {
      btnIngest.setVisible(false);
      btnIngestClicked = false;
    }

    browsingStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForBrowseStatus(),
      SolrHumanizer.humanize(database.getStatus()));
    browsingStatus.setCSSMetadata(null, "label-field", "value-field");

    browse.addToInfoPanel(browsingStatus);

    return browse;
  }

  public void update(ViewerDatabase database) {
    this.database = database;
    browsingStatus.updateText(SolrHumanizer.humanize(database.getStatus()));

    if (database.getStatus().equals(ViewerDatabase.Status.AVAILABLE)
      || database.getStatus().equals(ViewerDatabase.Status.ERROR)) {
      btnIngest.setVisible(false);
      btnBrowse.setVisible(true);
      btnDelete.setVisible(true);
    } else if (database.getStatus().equals(ViewerDatabase.Status.INGESTING)) {
      if (btnIngestClicked) {
        btnIngest.setVisible(true);
        btnIngest.setText(messages.SIARDHomePageButtonTextForStartIngest());
        btnBrowse.setVisible(false);
        btnIngest.addClickHandler(
          event -> HistoryManager.gotoIngestSIARDData(database.getUUID(), database.getMetadata().getName()));
      }
    } else if (database.getStatus().equals(ViewerDatabase.Status.METADATA_ONLY)) {
      btnIngest.setVisible(true);
      btnBrowse.setVisible(false);
      btnDelete.setVisible(false);
      btnIngestClicked = false;
    }

    if (database.getSIARDPath() == null || database.getSIARDPath().isEmpty()) {
      btnIngest.setEnabled(false);
      btnIngest.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
      btnIngestClicked = false;
    } else {
      btnIngest.setEnabled(true);
      btnIngest.setTitle(null);
    }
  }

  private void delete() {
    if (database.getStatus().equals(ViewerDatabase.Status.AVAILABLE)
      || database.getStatus().equals(ViewerDatabase.Status.ERROR)) {
      BrowserService.Util.getInstance().deleteRowsCollection(database.getUUID(), new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {

        }

        @Override
        public void onSuccess(Boolean result) {
          SIARDManagerPage.getInstance(database).refreshInstance(database.getUUID());
        }
      });
    }
  }
}
