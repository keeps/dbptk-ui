package com.databasepreservation.desktop.client.dbptk.wizard.download;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.progressBar.ProgressBarPanel;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.databasepreservation.common.client.models.parameters.ExportOptionsParameters;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.services.SIARDService;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardManager;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.desktop.client.dbptk.wizard.common.connection.Connection;
import com.databasepreservation.desktop.client.dbptk.wizard.common.exportOptions.MetadataExportOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DBMSWizardManager extends WizardManager {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface DBMSWizardManagerUiBinder extends UiBinder<Widget, DBMSWizardManager> {
  }

  private static DBMSWizardManagerUiBinder binder = GWT.create(DBMSWizardManagerUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel wizardContent;

  @UiField
  FlowPanel customButtons;

  @UiField
  Button btnNext;

  @UiField
  Button btnCancel;

  @UiField
  Button btnBack;

  private static Map<String, DBMSWizardManager> instances = new HashMap<>();
  private List<WizardPanel> wizardPanels = new ArrayList<>();
  private final String databaseUUID;
  private String databaseName;

  private ViewerDatabase database;
  private int position = 0;

  private ConnectionParameters connectionParameters;

  public static DBMSWizardManager getInstance(String databaseUUID) {
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new DBMSWizardManager(databaseUUID));
      instances.get(databaseUUID).init();
    }

    return instances.get(databaseUUID);
  }

  private void init() {
    wizardContent.clear();
    Connection connection = Connection.getInstance(databaseUUID);
    connection.initExportDBMS(ViewerConstants.DOWNLOAD_WIZARD_MANAGER, HistoryManager.ROUTE_SEND_TO_LIVE_DBMS);
    wizardPanels.add(0, connection);
    wizardContent.add(connection);
    btnBack.setVisible(false);
    btnNext.setEnabled(false);
    btnNext.setText(messages.wizardSendToDBMSExportButton());
    updateButtons();
    updateBreadcrumb();

  }

  public void setBreadcrumbDatabaseName(final String databaseName) {
    this.databaseName = databaseName;
    updateBreadcrumb();
  }

  private DBMSWizardManager(String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;

    btnNext.addClickHandler(event -> handleWizard());

    btnCancel.addClickHandler(event -> {
      clear();
      for (WizardPanel wizardPanel : wizardPanels) {
        wizardPanel.clear();
      }
      instances.clear();
      HistoryManager.gotoSIARDInfo(databaseUUID);
    });
  }

  @Override
  public void enableNext(boolean value) {
    btnNext.setEnabled(value);
  }

  @Override
  protected void handleWizard() {
    GWT.log("Position: " + position);
    if (position == 0) {
      handleDBMSConnection();
    } else if (position == 1) {
      handleSIARDExportOptions();
    }
  }

  private void handleSIARDExportOptions() {
    final boolean valid = wizardPanels.get(position).validate();
    if (valid) {
      ExportOptionsParameters exportOptionsParameters = (ExportOptionsParameters) wizardPanels.get(position)
          .getValues();
      wizardContent.clear();
      MetadataExportOptions metadataExportOptions = MetadataExportOptions
          .getInstance(exportOptionsParameters.getSiardVersion(), false);
      wizardPanels.add(position, metadataExportOptions);
      wizardContent.add(metadataExportOptions);
      updateButtons();
      updateBreadcrumb();
    } else {
      wizardPanels.get(position).error();
    }
  }

  private void handleDBMSConnection() {
    final boolean valid = wizardPanels.get(position).validate();
    if (valid) {
      connectionParameters = (ConnectionParameters) wizardPanels.get(position).getValues();
      migrateToDBMS();
    } else {
      wizardPanels.get(position).error();
    }
  }

  private void migrateToDBMS() {
    wizardContent.clear();
    enableButtons(false);
    position = 3;
    updateBreadcrumb();

    DatabaseService.Util.call((ViewerDatabase result) -> {
      database = result;
      final String siardPath = database.getPath();

      ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
      progressBarPanel.setTitleText(messages.progressBarPanelTextForDBMSWizardTitle(
          ToolkitModuleName2ViewerModuleName.transform(connectionParameters.getModuleName())));
      progressBarPanel.setSubtitleText(messages.progressBarPanelTextForDBMSWizardSubTitle());
      wizardContent.add(progressBarPanel);

      SIARDService.Util.call((Boolean response) -> {
        if (response) {
          Dialogs.showInformationDialog(messages.sendToWizardManagerInformationTitle(),
              messages.sendToWizardManagerInformationMessageDBMS(
                  connectionParameters.getJdbcParameters().getConnection().get("database")),
              messages.basicActionClose(), "btn btn-link", new DefaultAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                  clear();
                  for (WizardPanel wizardPanel : wizardPanels) {
                    wizardPanel.clear();
                  }
                  instances.clear();
                  HistoryManager.gotoSIARDInfo(databaseUUID);
                }
              });
        }

      }, (String errorMessage) -> {
        wizardContent.clear();
        position = 0;
        wizardContent.add(wizardPanels.get(position));
        enableButtons(true);
        updateBreadcrumb();
        enableNext(false);

        Dialogs.showErrors(messages.errorMessagesConnectionTitle(), errorMessage, messages.basicActionClose());

      }).migrateToDBMS(databaseUUID, database.getVersion(), siardPath, connectionParameters);

    }).retrieve(databaseUUID, databaseUUID);
  }

  @Override
  protected void updateButtons() {
    // DO NOTHING
  }

  @Override
  protected void enableButtons(boolean value) {
    btnCancel.setEnabled(value);
    btnNext.setEnabled(value);
    btnBack.setEnabled(value);
  }

  @Override
  protected void updateBreadcrumb() {
    List<BreadcrumbItem> breadcrumbItems;

    switch (position) {
      case 0:
        breadcrumbItems = BreadcrumbManager.forDBMSConnectionSendToWM(databaseUUID, databaseName);
        break;
      case 1:
        breadcrumbItems = BreadcrumbManager.forSIARDExportOptionsSenToWM(databaseUUID, databaseName);
        break;
      case 2:
        breadcrumbItems = BreadcrumbManager.forMetadataExportOptionsSendToWM(databaseUUID, databaseName);
        break;
      case 3:
        breadcrumbItems = BreadcrumbManager.forProgressBarPanelSendToWM(databaseUUID, databaseName);
        break;
      default:
        breadcrumbItems = new ArrayList<>();
        break;
    }

    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  @Override
  protected void clear() {
    super.clear();

    ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
    progressBarPanel.clear(databaseUUID);
  }

  public void change(String wizardPage, String toSelect) {
    internalChanger(wizardPage, toSelect);
  }

  private void internalChanger(String wizardPage, String toSelect) {
    WizardPanel wizardPanel = wizardPanels.get(position);
    if (HistoryManager.ROUTE_WIZARD_CONNECTION.equals(wizardPage)) {
      if (wizardPanel instanceof Connection) {
        Connection connection = (Connection) wizardPanel;
        connection.sideBarHighlighter(toSelect);
      } else {
        HistoryManager.gotoSendToLiveDBMS(databaseUUID);
      }
    } else {
      HistoryManager.gotoSendToLiveDBMS(databaseUUID);
    }
  }
}
