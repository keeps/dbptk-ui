package com.databasepreservation.main.desktop.client.dbptk.wizard.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.exportOptions.MetadataExportOptions;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.exportOptions.SIARDExportOptions;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.progressBar.ProgressBarPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.TableAndColumns;
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
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SIARDWizardManager extends Composite {

  interface MigrateToSIARDUiBinder extends UiBinder<Widget, SIARDWizardManager> {
  }

  private static MigrateToSIARDUiBinder binder = GWT.create(MigrateToSIARDUiBinder.class);

  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel wizardContent;

  @UiField
  Button btnNext, btnCancel, btnBack;

  private static Map<String, SIARDWizardManager> instances = new HashMap<>();
  private ArrayList<WizardPanel> wizardInstances = new ArrayList<>();
  private final String databaseUUID;
  private int position = 0;
  private final int positions = 4;

  private TableAndColumnsParameters tableAndColumnsParameters;
  private ExportOptionsParameters exportOptionsParameters;
  private MetadataExportOptionsParameters metadataExportOptionsParameters;

  public static SIARDWizardManager getInstance(String databaseUUID) {
    if (instances.get(databaseUUID) == null) {
      SIARDWizardManager instance = new SIARDWizardManager(databaseUUID);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  private SIARDWizardManager(String databaseUUID) {
    initWidget(binder.createAndBindUi(this));
    this.databaseUUID = databaseUUID;

    init();

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

  private void init() {
    wizardContent.clear();
    TableAndColumns tableAndColumns = TableAndColumns.getInstance(databaseUUID);
    wizardInstances.add(0, tableAndColumns);
    wizardContent.add(tableAndColumns);
    updateButtons();
    updateBreadcrumb();
  }

  private void handleWizard() {
    GWT.log("Position: " + position);
    switch (position) {
      case 0:
        handleTableAndColumnsPanel();
        break;
      case 1:
        handleSIARDExportOptions();
        break;
      case 2:
        handleMetadataExportOption();
        break;
    }
  }

  private void handleTableAndColumnsPanel() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      tableAndColumnsParameters = (TableAndColumnsParameters) wizardInstances.get(position).getValues();
      wizardContent.clear();
      position = 1;
      SIARDExportOptions exportOptions = SIARDExportOptions.getInstance();
      wizardInstances.add(position, exportOptions);
      wizardContent.add(exportOptions);
    } else {
      wizardInstances.get(position).error();
    }
  }

  private void handleSIARDExportOptions() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      exportOptionsParameters = (ExportOptionsParameters) wizardInstances.get(position).getValues();
      wizardContent.clear();
      position = 2;
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

  private void handleMetadataExportOption() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      metadataExportOptionsParameters = (MetadataExportOptionsParameters) wizardInstances.get(position).getValues();
      migrateToSIARD();
    }
  }

  private void migrateToSIARD() {
    wizardContent.clear();
    enableButtons(false);
    position = 3;
    updateBreadcrumb();

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

          BrowserService.Util.getInstance().migrateToSIARD(databaseUUID, siardPath, tableAndColumnsParameters,
            exportOptionsParameters, metadataExportOptionsParameters, new DefaultAsyncCallback<Boolean>() {

              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  Toast.showInfo(messages.sendToWizardManagerInformationTitle(),
                    messages.sendToWizardManagerInformationMessageSIARD());
                  clear();
                  instances.clear();
                  HistoryManager.gotoSIARDInfo(databaseUUID);
                }
              }

              @Override
              public void onFailure(Throwable caught) {
                enableButtons(true);
                enableNext(false);
                Toast.showError(messages.alertErrorTitle(), caught.getMessage());
              }
            });
        }
      });
  }

  private void updateButtons() {
    btnBack.setEnabled(true);
    btnNext.setText(messages.next());

    if (position == 0) {
      btnBack.setEnabled(false);
    }

    if (position == positions - 1) {
      btnNext.setText(messages.migrate());
    }
  }

  public void enableNext(boolean value) {
    btnNext.setEnabled(value);
  }

  private void enableButtons(boolean value) {
    btnCancel.setEnabled(value);
    btnNext.setEnabled(value);
    btnBack.setEnabled(value);
  }

  private void updateBreadcrumb() {
    List<BreadcrumbItem> breadcrumbItems;

    switch (position) {
      case 0:
        breadcrumbItems = BreadcrumbManager.forTableAndColumnsSendToWM(databaseUUID);
        break;
      case 1:
        breadcrumbItems = BreadcrumbManager.forSIARDExportOptionsSenToWM(databaseUUID);
        break;
      case 2:
        breadcrumbItems = BreadcrumbManager.forMetadataExportOptionsSendToWM(databaseUUID);
        break;
      case 3:
        breadcrumbItems = BreadcrumbManager.forProgressBarPanelSendToWM(databaseUUID);
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
    if (HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS.equals(wizardPage)) {
      if (wizardPanel instanceof TableAndColumns) {
        TableAndColumns tableAndColumns = (TableAndColumns) wizardPanel;
        tableAndColumns.sideBarHighlighter(toSelect, schemaUUID, tableUUID);
      } else {
        HistoryManager.gotoSendToLiveDBMS(databaseUUID);
      }
    } else {
      HistoryManager.gotoSendToLiveDBMS(databaseUUID);
    }
  }
}