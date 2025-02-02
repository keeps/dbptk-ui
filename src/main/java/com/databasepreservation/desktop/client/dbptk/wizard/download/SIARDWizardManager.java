/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.desktop.client.dbptk.wizard.download;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.progressBar.ProgressBarPanel;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.wizard.CreateSIARDParameters;
import com.databasepreservation.common.client.models.wizard.export.ExportOptionsParameters;
import com.databasepreservation.common.client.models.wizard.export.MetadataExportOptionsParameters;
import com.databasepreservation.common.client.models.wizard.table.TableAndColumnsParameters;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.services.MigrationService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.PathUtils;
import com.databasepreservation.common.client.widgets.Toast;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardManager;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.desktop.client.dbptk.wizard.common.exportOptions.MetadataExportOptions;
import com.databasepreservation.desktop.client.dbptk.wizard.common.exportOptions.SIARDExportOptions;
import com.databasepreservation.desktop.client.dbptk.wizard.upload.TableAndColumns;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SIARDWizardManager extends WizardManager {

  interface SIARDWizardManagerUiBinder extends UiBinder<Widget, SIARDWizardManager> {
  }

  private static SIARDWizardManagerUiBinder binder = GWT.create(SIARDWizardManagerUiBinder.class);

  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel wizardContent;

  @UiField
  Button btnNext, btnCancel, btnBack;

  private static Map<String, SIARDWizardManager> instances = new HashMap<>();
  private final String databaseUUID;
  private final String databaseName;
  private ViewerDatabase database = null;
  private int position = 0;
  private final int positions = 4;

  private TableAndColumnsParameters tableAndColumnsParameters;
  private ExportOptionsParameters exportOptionsParameters;
  private MetadataExportOptionsParameters metadataExportOptionsParameters;

  public static SIARDWizardManager getInstance(String databaseUUID, String databaseName) {
    if (instances.get(databaseUUID) == null) {
      SIARDWizardManager instance = new SIARDWizardManager(databaseUUID, databaseName);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  private SIARDWizardManager(String databaseUUID, String databaseName) {
    initWidget(binder.createAndBindUi(this));

    DatabaseService.Util.call((IsIndexed result) -> {
      database = (ViewerDatabase) result;
    }).retrieve(databaseUUID);
    this.databaseUUID = databaseUUID;
    this.databaseName = databaseName;
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
    DatabaseService.Util.call((IsIndexed result) -> {
      database = (ViewerDatabase) result;
    }).retrieve(databaseUUID);
    wizardContent.clear();
    TableAndColumns tableAndColumns = TableAndColumns.getInstance(databaseUUID);
    wizardInstances.add(0, tableAndColumns);
    wizardContent.add(tableAndColumns);
    updateButtons();
    updateBreadcrumb();
  }

  @Override
  protected void handleWizard() {
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
      String SIARDDefaultPath = database.getPath().replace(PathUtils.getFileName(database.getPath()), "")
        + PathUtils.getFileName(database.getPath()).replaceFirst("[.][^.]+$", "") + "-"
        + DateTimeFormat.getFormat("yyyyMMddHHmmssSSS").format(new Date()) + ViewerConstants.SIARD_SUFFIX;

      SIARDExportOptions exportOptions = SIARDExportOptions.getInstance(SIARDDefaultPath);
      wizardInstances.add(position, exportOptions);
      wizardContent.add(exportOptions);
      updateButtons();
      updateBreadcrumb();
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
      if (exportOptionsParameters.getSiardVersion().equals(ViewerConstants.SIARDDK)) {
        migrateToSIARD();
      } else {
        MetadataExportOptions metadataExportOptions = MetadataExportOptions
          .getInstance(exportOptionsParameters.getSiardVersion(), true, databaseUUID);
        wizardInstances.add(position, metadataExportOptions);
        wizardContent.add(metadataExportOptions);
        updateButtons();
        updateBreadcrumb();
      }
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
    ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
    progressBarPanel.setTitleText(messages.progressBarPanelTextForCreateWizardProgressTitle());
    progressBarPanel.setSubtitleText(messages.progressBarPanelTextForCreateWizardProgressSubTitle());
    wizardContent.add(progressBarPanel);

    MigrationService.Util.call((StringResponse response) -> {
      Dialogs.showInformationDialog(messages.sendToWizardManagerInformationTitle(),
        messages.sendToWizardManagerInformationMessageSIARD(), messages.basicActionClose(), "btn btn-link",
        new DefaultAsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            clear();
            instances.clear();
            HistoryManager.gotoSIARDInfo(databaseUUID);
          }
        });
    }, (String errorMessage) -> {
      enableButtons(true);
      enableNext(false);
      Toast.showError(messages.alertErrorTitle(), errorMessage);
    }).run(databaseUUID, new CreateSIARDParameters(null, tableAndColumnsParameters, null, null, exportOptionsParameters,
      metadataExportOptionsParameters));
  }

  @Override
  protected void updateButtons() {
    btnBack.setEnabled(true);
    btnNext.setText(messages.basicActionNext());

    if (position == 0) {
      btnBack.setEnabled(false);
    }

    if (position == positions - 1) {
      btnNext.setText(messages.basicActionMigrate());
    }
  }

  @Override
  public void enableNext(boolean value) {
    btnNext.setEnabled(value);
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
        breadcrumbItems = BreadcrumbManager.forTableAndColumnsSendToWM(databaseUUID, databaseName);
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