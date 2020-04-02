package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.services.SiardService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.PathUtils;
import com.databasepreservation.common.client.tools.RestUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

import config.i18n.client.ClientMessages;

public class SIARDNavigationPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SIARDNavigationPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private Button btnDelete;
  private Button btnShowFiles;
  private Button btnEditMetadata;

  public static SIARDNavigationPanel getInstance(ViewerDatabase database) {
    String databaseUUID = database.getUuid();
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
    siard.addToDescriptionPanel(messages.SIARDHomePageOptionsDescriptionForSIARD());

    // Edit button
    btnEditMetadata = new Button();
    btnEditMetadata.setText(messages.SIARDHomePageButtonTextEditMetadata());
    btnEditMetadata.addStyleName("btn btn-outline-primary btn-edit");
    btnEditMetadata.addClickHandler(clickEvent -> {
      HistoryManager.gotoSIARDEditMetadata(database.getUuid());
    });

    // Migration button
    Button btnMigrateToSIARD = new Button();
    btnMigrateToSIARD.setText(messages.SIARDHomePageButtonTextMigrateToSIARD());
    btnMigrateToSIARD.addStyleName("btn btn-outline-primary btn-play");

    btnMigrateToSIARD.addClickHandler(event -> {
      HistoryManager.gotoMigrateSIARD(database.getUuid(), database.getMetadata().getName());
    });

    // Send to Live DBMS button
    Button btnSendToLiveDBMS = new Button();
    btnSendToLiveDBMS.setText(messages.SIARDHomePageButtonTextSendToLiveDBMS());
    btnSendToLiveDBMS.addStyleName("btn btn-outline-primary btn-play");

    btnSendToLiveDBMS.addClickHandler(event -> {
      HistoryManager.gotoSendToLiveDBMSExportFormat(database.getUuid(), database.getMetadata().getName());
    });

    // Show SIARD file button
    btnShowFiles = new Button(PathUtils.getFileName(database.getPath()));
    btnShowFiles.addStyleName("btn btn-link-info");
    if (database.getPath() != null && !database.getPath().isEmpty()) {
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        btnShowFiles.addClickHandler(clickEvent -> {
          JavascriptUtils.showItemInFolder(database.getPath());
        });
      } else {
        btnShowFiles.addClickHandler(clickEvent -> {
          SafeUri downloadUri = RestUtils.createFileResourceDownloadSIARDUri(database.getPath());
          Window.Location.assign(downloadUri.asString());
        });
      }
    }

    // Delete SIARD file button
    btnDelete = new Button();
    btnDelete.addStyleName("btn btn-outline-danger btn-delete");
    if (database.getPath() != null && !database.getPath().isEmpty()) {
      btnDelete.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
      btnDelete.addClickHandler(event -> {
        if (!database.getStatus().equals(ViewerDatabaseStatus.REMOVING)
          && !database.getStatus().equals(ViewerDatabaseStatus.INGESTING)) {
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
    MetadataField version = MetadataField.createInstance(messages.SIARDHomePageLabelForSIARDVersion(), messages
      .SIARDHomePageLabelForSIARDStandardVersion(database.getVersion(), ViewerConstants.SIARD_SPECIFICATION_LINK));
    version.setCSS(null, "label-field", "value-field");

    // size information
    MetadataField size = MetadataField.createInstance(messages.SIARDHomePageLabelForSIARDSize(),
      Humanize.readableFileSize(database.getSize()));
    size.setCSS(null, "label-field", "value-field");

    // path information
    GenericField path = GenericField.createInstance(messages.SIARDHomePageLabelForSIARDPath(), btnShowFiles);
    path.setCSSMetadata(null, "label-field");

    siard.addToInfoPanel(version);
    siard.addToInfoPanel(path);
    siard.addToInfoPanel(size);

    siard.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnEditMetadata));
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
      siard.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnMigrateToSIARD));
      siard.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnSendToLiveDBMS));
    } else {
      siard.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnDelete));
    }
    update(database);

    return siard;
  }

  public void update(ViewerDatabase database) {
    this.database = database;
    if (database.getPath() == null || database.getPath().isEmpty()) {
      btnShowFiles.setText(null);
      btnShowFiles.setVisible(false);
      btnDelete.setVisible(false);
      btnEditMetadata.setEnabled(false);
      btnEditMetadata.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
    } else {
      btnShowFiles.setText(PathUtils.getFileName(database.getPath()));
      btnShowFiles.setVisible(true);
      btnDelete.setVisible(true);
      btnEditMetadata.setEnabled(true);
      btnEditMetadata.setTitle(null);
    }
  }

  private void delete() {
    if (!database.getStatus().equals(ViewerDatabaseStatus.REMOVING)
      && !database.getStatus().equals(ViewerDatabaseStatus.INGESTING)
      && !database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_RUNNING)) {
      SiardService.Util.call((Void result) -> {
        SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid());
      }).deleteSIARDFile(database.getUuid(), database.getUuid());
    }
  }
}
