package com.databasepreservation.main.desktop.client.dbptk.wizard.sendTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.dbptk.wizard.ProgressBarPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.TableAndColumns;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions.MetadataExportOptions;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions.SIARDExportOptions;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.TableAndColumnsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SendToWizardManager extends Composite {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface SendToWizardManagerUiBinder extends UiBinder<Widget, SendToWizardManager> {
  }

  private static SendToWizardManagerUiBinder binder = GWT.create(SendToWizardManagerUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel wizardContent, customButtons;

  @UiField
  Button btnNext, btnCancel, btnBack;

  private static HashMap<String, SendToWizardManager> instances = new HashMap<>();
  private ArrayList<WizardPanel> wizardInstances = new ArrayList<>();
  private String databaseUUID;
  private int format = -1;
  private int position = 0;
  private final int positions = 4;

  private TableAndColumnsParameters tableAndColumnsParameters;
  private ConnectionParameters connectionParameters;
  private ExportOptionsParameters exportOptionsParameters;
  private MetadataExportOptionsParameters metadataExportOptionsParameters;

  public static SendToWizardManager getInstance(String databaseUUID) {
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new SendToWizardManager(databaseUUID));
      instances.get(databaseUUID).init();
    }

    return instances.get(databaseUUID);
  }

  private void init() {
    updateButtons(-1);
    updateBreadcrumb(-1);
    ExportFormat exportFormat = ExportFormat.getInstance(databaseUUID);
    wizardContent.clear();
    wizardInstances.add(0, exportFormat);
    wizardContent.add(exportFormat);

  }

  private SendToWizardManager(String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;

    btnNext.addClickHandler(event -> {
      handleWizard();
    });

    btnBack.addClickHandler(event -> {
      if (position != 0) {
        wizardContent.clear();
        wizardContent.add(wizardInstances.get(--position));
        updateButtons(-1);
        updateBreadcrumb(-1);
      }
    });

    btnCancel.addClickHandler(event -> {
      clear();
      instances.clear();
      HistoryManager.gotoSIARDInfo(databaseUUID);
    });
  }

  public void enableNext(boolean value) {
    btnNext.setEnabled(value);
  }

  private void handleWizard() {
    GWT.log("Position: " + position);
    switch (position) {
      case 0:
        format = handleExportFormat();
        break;
      case 1:
        if (format == 1) {
          handleDBMSConnection();
        }
        if (format == 2) {
          handleTableAndColumnsPanel();
        }
        break;
      case 2:
        handleSIARDExportOptions();
        break;
      case 3:
        handleMetadataExportOption();
        break;
    }
  }

  private int handleExportFormat() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      String value = (String) wizardInstances.get(position).getValues();
      wizardContent.clear();
      position = 1;
      if (value.equals(ViewerConstants.EXPORT_FORMAT_DBMS)) {
        DBMSConnection connection = DBMSConnection.getInstance(databaseUUID);
        wizardInstances.add(position, connection);
        wizardContent.add(connection);
        updateButtons(1);
        updateBreadcrumb(1);
        return 1;
      } else if (value.equals(ViewerConstants.EXPORT_FORMAT_SIARD)) {
        TableAndColumns tableAndColumns = TableAndColumns.getInstance(databaseUUID);
        wizardInstances.add(position, tableAndColumns);
        wizardContent.add(tableAndColumns);
        updateButtons(-1);
        updateBreadcrumb(2);
        return 2;
      }
    }
    return -1;
  }

  private void handleSIARDExportOptions() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      exportOptionsParameters = (ExportOptionsParameters) wizardInstances.get(position).getValues();
      wizardContent.clear();
      position = 3;
      MetadataExportOptions metadataExportOptions = MetadataExportOptions
        .getInstance(exportOptionsParameters.getSIARDVersion());
      wizardInstances.add(position, metadataExportOptions);
      wizardContent.add(metadataExportOptions);
      updateButtons(-1);
      updateBreadcrumb(-1);
    } else {
      wizardInstances.get(position).error();
    }
  }

  private void handleMetadataExportOption() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      metadataExportOptionsParameters = (MetadataExportOptionsParameters) wizardInstances.get(position).getValues();
      migrateToSIARD();
    }
  }

  private void handleTableAndColumnsPanel() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      tableAndColumnsParameters = (TableAndColumnsParameters) wizardInstances.get(position).getValues();
      wizardContent.clear();
      position = 2;
      SIARDExportOptions exportOptions = SIARDExportOptions.getInstance();
      wizardInstances.add(position, exportOptions);
      wizardContent.add(exportOptions);
      updateButtons(-1);
      updateBreadcrumb(-1);
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
    position = 4;
    updateBreadcrumb(-1);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          ViewerDatabase database = (ViewerDatabase) result;
          final String siardPath = database.getSIARDPath();

          ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
          progressBarPanel.setTitleText(messages.wizardProgressSendToDBMSTitle(
            ToolkitModuleName2ViewerModuleName.transform(connectionParameters.getModuleName())));
          progressBarPanel.setSubTitleText(messages.wizardProgressSendToDBMSSubTitle());
          wizardContent.add(progressBarPanel);

          BrowserService.Util.getInstance().migrateToDBMS(databaseUUID, siardPath, connectionParameters,
            new DefaultAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  Toast.showInfo("SIARD", "created");
                  clear();
                  instances.clear();
                  HistoryManager.gotoSIARDInfo(databaseUUID);
                }
              }
            });
        }
      });
  }

  private void migrateToSIARD() {
    wizardContent.clear();
    enableButtons(false);
    position = 4;
    updateBreadcrumb(-1);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          ViewerDatabase database = (ViewerDatabase) result;
          final String siardPath = database.getSIARDPath();

          ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
          progressBarPanel.setTitleText(messages.wizardProgressSIARDTitle());
          progressBarPanel.setSubTitleText(messages.wizardProgressSIARDSubTitle());
          wizardContent.add(progressBarPanel);

          BrowserService.Util.getInstance().migrateToSIARD(databaseUUID, siardPath, connectionParameters, tableAndColumnsParameters,
            exportOptionsParameters,metadataExportOptionsParameters, new DefaultAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  Toast.showInfo("SIARD", "created");
                  clear();
                  instances.clear();
                  HistoryManager.gotoSIARDInfo(databaseUUID);
                }
              }
            });
        }
      });
  }

  private void updateButtons(int format) {
    btnBack.setEnabled(true);
    btnNext.setText(messages.next());

    if (position == 0) {
      btnBack.setEnabled(false);
    }

    if (format == 1) {
      btnNext.setText(messages.migrate());
    }

    if (position == positions - 1) {
      btnNext.setText(messages.migrate());
    }
  }

  private void enableButtons(boolean value) {
    btnCancel.setEnabled(value);
    btnNext.setEnabled(value);
    btnBack.setEnabled(value);
  }

  private void updateBreadcrumb(int format) {
    List<BreadcrumbItem> breadcrumbItems;

    switch (position) {
      case 0:
        breadcrumbItems = BreadcrumbManager.forExportFormatSendToWM(databaseUUID);
        break;
      case 1:
        if (format == 1) {
          breadcrumbItems = BreadcrumbManager.forDBMSConnectionSendToWM(databaseUUID);
        } else if (format == 2) {
          breadcrumbItems = BreadcrumbManager.forTableAndColumnsSendToWM(databaseUUID);
        } else {
          breadcrumbItems = new ArrayList<>();
        }
        break;
      case 2:
        breadcrumbItems = BreadcrumbManager.forSIARDExportOptionsSenToWM(databaseUUID);
        break;
      case 3:
        breadcrumbItems = BreadcrumbManager.forMetadataExportOptionsSendToWM(databaseUUID);
        break;
      default:
        breadcrumbItems = new ArrayList<>();
        break;
    }

    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  private void clear() {
    for (WizardPanel panel : wizardInstances) {
      panel.clear();
    }

    ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
    progressBarPanel.clear(databaseUUID);

    wizardInstances.clear();
  }

  public void change(String wizardPage, String toSelect) {
    internalChanger(wizardPage, toSelect, null, null);
  }

  public void change(String wizardPage, String toSelect, String schemaUUID) {
    internalChanger(wizardPage, toSelect, schemaUUID, null);
  }

  public void change(String wizardPage, String toSelect, String schemaUUID, String tableUUID) {
    internalChanger(wizardPage, toSelect, schemaUUID, tableUUID);
  }

  private void internalChanger(String wizardPage, String toSelect, String schemaUUID, String tableUUID) {
    WizardPanel wizardPanel = wizardInstances.get(position);
    switch (wizardPage) {
      case HistoryManager.ROUTE_WIZARD_CONNECTION:
        GWT.log(wizardPanel.getClass().getName());
        if (wizardPanel instanceof DBMSConnection) {
          DBMSConnection connection = (DBMSConnection) wizardPanel;
          connection.sideBarHighlighter(toSelect);
        }
        break;
      case HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS:
        if (wizardPanel instanceof TableAndColumns) {
          TableAndColumns tableAndColumns = (TableAndColumns) wizardPanel;
          tableAndColumns.sideBarHighlighter(toSelect, schemaUUID, tableUUID);
        }
        break;
      default:
        break;
    }
  }
}
