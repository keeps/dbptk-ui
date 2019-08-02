package com.databasepreservation.main.desktop.client.dbptk.wizard.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.utils.AsyncCallbackUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.exportOptions.MetadataExportOptions;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.progressBar.ProgressBarPanel;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

import javax.ws.rs.Produces;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DBMSWizardManager extends WizardManager {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface SendToWizardManagerUiBinder extends UiBinder<Widget, DBMSWizardManager> {
  }

  private static SendToWizardManagerUiBinder binder = GWT.create(SendToWizardManagerUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel wizardContent, customButtons;

  @UiField
  Button btnNext, btnCancel, btnBack;

  private static HashMap<String, DBMSWizardManager> instances = new HashMap<>();
  private String databaseUUID;
  private ArrayList<WizardPanel> wizardInstances = new ArrayList<>();
  private final String databaseName;
  private ViewerDatabase database;
  private int position = 0;
  private final int positions = 4;

  private ConnectionParameters connectionParameters;
  private ExportOptionsParameters exportOptionsParameters;

  public static DBMSWizardManager getInstance(String databaseUUID, String databaseName) {
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new DBMSWizardManager(databaseUUID, databaseName));
      instances.get(databaseUUID).init(databaseName);
    }

    return instances.get(databaseUUID);
  }

  private void init(String databaseName) {
    wizardContent.clear();
    DBMSConnection connection = DBMSConnection.getInstance(databaseUUID, databaseName);
    wizardInstances.add(0, connection);
    wizardContent.add(connection);
    updateButtons();
    updateBreadcrumb();

  }

  private DBMSWizardManager(String databaseUUID, String databaseName) {
    initWidget(binder.createAndBindUi(this));

    this.databaseName = databaseName;
    this.databaseUUID = databaseUUID;

    btnNext.addClickHandler(event -> {
      handleWizard();
    });

    btnBack.addClickHandler(event -> {
      if (position != 0) {
        wizardContent.clear();
        wizardContent.add(wizardInstances.get(--position));
        updateButtons();
        updateBreadcrumb();
      }
    });

    btnCancel.addClickHandler(event -> {
      clear();
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
    switch (position) {
      case 0:
        handleDBMSConnection();
        break;
      case 1:
        handleSIARDExportOptions();
        break;
    }
  }

  private void handleSIARDExportOptions() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      exportOptionsParameters = (ExportOptionsParameters) wizardInstances.get(position).getValues();
      wizardContent.clear();
      MetadataExportOptions metadataExportOptions = MetadataExportOptions
        .getInstance(exportOptionsParameters.getSIARDVersion());
      wizardInstances.add(position, metadataExportOptions);
      wizardContent.add(metadataExportOptions);
      updateButtons();
      updateBreadcrumb();
    } else {
      wizardInstances.get(position).error();
    }
  }

  private void handleDBMSConnection() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      connectionParameters = (ConnectionParameters) wizardInstances.get(position).getValues();
      migrateToDBMS();
    } else {
      wizardInstances.get(position).error();
      DBMSConnection connection = (DBMSConnection) wizardInstances.get(position);
      connection.clearPasswords();
    }
  }

  private void migrateToDBMS() {
    wizardContent.clear();
    enableButtons(false);
    position = 3;
    updateBreadcrumb();

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          final String siardPath = database.getSIARDPath();

          ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
          progressBarPanel.setTitleText(messages.wizardProgressSendToDBMSTitle(
            ToolkitModuleName2ViewerModuleName.transform(connectionParameters.getModuleName())));
          progressBarPanel.setSubTitleText(messages.wizardProgressSendToDBMSSubTitle());
          wizardContent.add(progressBarPanel);

          BrowserService.Util.getInstance().migrateToDBMS(databaseUUID, siardPath, connectionParameters,
            new DefaultAsyncCallback<Boolean>() {

              @Override
              public void onFailure(Throwable caught) {
                wizardContent.clear();
                position = 0;
                wizardContent.add(wizardInstances.get(position));
                enableButtons(true);
                updateBreadcrumb();
                enableNext(false);
                Toast.showError(messages.errorMessagesConnectionTitle(), caught.getMessage());
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  Toast.showInfo(messages.sendToWizardManagerInformationTitle(),
                    messages.sendToWizardManagerInformationMessageDBMS(
                      connectionParameters.getJDBCConnectionParameters().getConnection().get("database")));
                  clear();
                  instances.clear();
                  HistoryManager.gotoSIARDInfo(databaseUUID);
                }
              }
            });
        }
      });
  }

  @Override
  protected void updateButtons() {
    btnBack.setEnabled(true);
    btnNext.setText(messages.migrate());

    if (position == 0) {
      btnBack.setEnabled(false);
    }

    if (position == positions - 1) {
      btnNext.setText(messages.migrate());
    }
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
    WizardPanel wizardPanel = wizardInstances.get(position);
    if (HistoryManager.ROUTE_WIZARD_CONNECTION.equals(wizardPage)) {
      if (wizardPanel instanceof DBMSConnection) {
        DBMSConnection connection = (DBMSConnection) wizardPanel;
        connection.sideBarHighlighter(toSelect);
      } else {
        HistoryManager.gotoSendToLiveDBMS(databaseUUID);
      }
    } else {
      HistoryManager.gotoSendToLiveDBMS(databaseUUID);
    }
  }
}
