package com.databasepreservation.common.shared.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.NavigationPanel;
import com.databasepreservation.common.shared.client.common.desktop.GenericField;
import com.databasepreservation.common.shared.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.shared.client.common.fields.MetadataField;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.shared.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.Humanize;
import com.databasepreservation.common.shared.client.tools.PathUtils;
import com.databasepreservation.common.shared.client.tools.RestUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;

import config.i18n.client.ClientMessages;

public class SIARDNavigationPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SIARDNavigationPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private Button btnDeleteSIARD, btnShowFiles, btnEditMetadata;

  public static SIARDNavigationPanel getInstance(ViewerDatabase database) {
    String databaseUUID = database.getUUID();
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new SIARDNavigationPanel(database));
    }
    return instances.get(databaseUUID);
  }

  private SIARDNavigationPanel(ViewerDatabase database) {
    this.database = database;
  }

  public NavigationPanel build() {
    NavigationPanel siard = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForSIARD());

    // Edit button
    btnEditMetadata = new Button();
    btnEditMetadata.setText(messages.SIARDHomePageButtonTextEditMetadata());
    btnEditMetadata.addStyleName("btn btn-link-info");
    btnEditMetadata.addClickHandler(clickEvent -> {
      HistoryManager.gotoSIARDEditMetadata(database.getUUID());
    });

    // Migration button
    Button btnMigrateToSIARD = new Button();
    btnMigrateToSIARD.setText(messages.SIARDHomePageButtonTextMigrateToSIARD());
    btnMigrateToSIARD.addStyleName("btn btn-link-info");

    btnMigrateToSIARD.addClickHandler(event -> {
      HistoryManager.gotoMigrateSIARD(database.getUUID(), database.getMetadata().getName());
    });

    // Send to Live DBMS button
    Button btnSendToLiveDBMS = new Button();
    btnSendToLiveDBMS.setText(messages.SIARDHomePageButtonTextSendToLiveDBMS());
    btnSendToLiveDBMS.addStyleName("btn btn-link-info");

    btnSendToLiveDBMS.addClickHandler(event -> {
      HistoryManager.gotoSendToLiveDBMSExportFormat(database.getUUID(), database.getMetadata().getName());
    });

    // Show SIARD file button
    btnShowFiles = new Button(PathUtils.getFileName(database.getSIARDPath()));
    btnShowFiles.addStyleName("btn btn-link-info");
    if (database.getSIARDPath() != null && !database.getSIARDPath().isEmpty()) {
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        btnShowFiles.addClickHandler(clickEvent -> {
          JavascriptUtils.showItemInFolder(database.getSIARDPath());
        });
      } else {
        btnShowFiles.addClickHandler(clickEvent -> {
          SafeUri downloadUri = RestUtils.createFileResourceDownloadSIARDUri(database.getUUID());
          Window.Location.assign(downloadUri.asString());
        });
      }
    }

    // Delete SIARD file button
    btnDeleteSIARD = new Button();
    btnDeleteSIARD.addStyleName("btn btn-link-info");
    if (database.getSIARDPath() != null && !database.getSIARDPath().isEmpty()) {
      GWT.log("pathsiard: " + database.getSIARDPath());
      btnDeleteSIARD.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
      btnDeleteSIARD.addClickHandler(event -> {
        if (!database.getStatus().equals(ViewerDatabase.Status.REMOVING)
          && !database.getStatus().equals(ViewerDatabase.Status.INGESTING)) {
          CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDelete(),
            messages.SIARDHomePageTextForDeleteSIARD(), messages.basicActionCancel(), messages.basicActionConfirm(),
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

    // version information
    MetadataField version = MetadataField.createInstance(messages.SIARDHomePageLabelForSIARDVersion(),
      database.getSIARDVersion());
    version.setCSSMetadata(null, "label-field", "value-field");

    // size information
    MetadataField size = MetadataField.createInstance(messages.SIARDHomePageLabelForSIARDSize(),
      Humanize.readableFileSize(database.getSIARDSize()));
    size.setCSSMetadata(null, "label-field", "value-field");

    // path information
    GenericField path = GenericField.createInstance(messages.SIARDHomePageLabelForSIARDPath(), btnShowFiles);
    path.setCSSMetadata(null, "label-field");

    siard.addToInfoPanel(version);
    siard.addToInfoPanel(path);
    siard.addToInfoPanel(size);

    siard.addButton(btnEditMetadata);
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
      siard.addButton(btnMigrateToSIARD);
      siard.addButton(btnSendToLiveDBMS);
    } else {
      siard.addButton(btnDeleteSIARD);
    }
    update(database);

    return siard;
  }

  public void update(ViewerDatabase database) {
    this.database = database;
    if (database.getSIARDPath() == null || database.getSIARDPath().isEmpty()) {
      btnShowFiles.setText(null);
      btnShowFiles.setVisible(false);
      btnDeleteSIARD.setVisible(false);
      btnEditMetadata.setEnabled(false);
      btnEditMetadata.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
    } else {
      btnShowFiles.setText(PathUtils.getFileName(database.getSIARDPath()));
      btnShowFiles.setVisible(true);
      btnDeleteSIARD.setVisible(true);
      btnEditMetadata.setEnabled(true);
      btnEditMetadata.setTitle(null);
    }
  }

  private void delete() {
    if (!database.getStatus().equals(ViewerDatabase.Status.REMOVING)
      && !database.getStatus().equals(ViewerDatabase.Status.INGESTING)
      && !database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_RUNNING)) {
      BrowserService.Util.getInstance().deleteSIARDFile(database.getSIARDPath(), database.getUUID(),
        new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable caught) {
          }

          @Override
          public void onSuccess(Void result) {
            SIARDManagerPage.getInstance(database).refreshInstance(database.getUUID());
          }
        });
    }
  }
}
