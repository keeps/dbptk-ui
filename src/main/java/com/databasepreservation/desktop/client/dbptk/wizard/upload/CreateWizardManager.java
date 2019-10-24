package com.databasepreservation.desktop.client.dbptk.wizard.upload;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.widgets.Toast;
import com.databasepreservation.common.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.common.shared.models.wizardParameters.CustomViewsParameters;
import com.databasepreservation.common.shared.models.wizardParameters.ExportOptionsParameters;
import com.databasepreservation.common.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.databasepreservation.common.shared.models.wizardParameters.TableAndColumnsParameters;
import com.databasepreservation.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardManager;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.desktop.client.dbptk.wizard.common.connection.Connection;
import com.databasepreservation.desktop.client.dbptk.wizard.common.exportOptions.MetadataExportOptions;
import com.databasepreservation.desktop.client.dbptk.wizard.common.exportOptions.SIARDExportOptions;
import com.databasepreservation.common.shared.client.common.visualization.browse.wizard.common.progressBar.ProgressBarPanel;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateWizardManager extends WizardManager {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface CreateWizardManagerUiBinder extends UiBinder<Widget, CreateWizardManager> {
  }

  interface ConnectionMapper extends ObjectMapper<ConnectionParameters> {
  }

  private static CreateWizardManagerUiBinder binder = GWT.create(CreateWizardManagerUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel wizardContent, customButtons;

  @UiField
  Button btnNext, btnCancel, btnBack;

  private static CreateWizardManager instance = null;
  private int position = 0;
  private final int positions = 5;
  private String databaseUUID;
  private ConnectionParameters connectionParameters;
  private TableAndColumnsParameters tableAndColumnsParameters;
  private CustomViewsParameters customViewsParameters;
  private ExportOptionsParameters exportOptionsParameters;
  private MetadataExportOptionsParameters metadataExportOptionsParameters = null;

  public static CreateWizardManager getInstance() {
    if (instance == null) {
      instance = new CreateWizardManager();
      instance.init();
    }

    return instance;
  }

  private void init() {
    updateButtons();
    updateBreadcrumb();

    BrowserService.Util.getInstance().generateUUID(new DefaultAsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        databaseUUID = result;
        Connection connection = Connection.getInstance(databaseUUID);
        connection.initImportDBMS(ViewerConstants.UPLOAD_WIZARD_MANAGER, HistoryManager.ROUTE_CREATE_SIARD);
        wizardContent.clear();
        wizardInstances.add(0, connection);
        wizardContent.add(connection);
      }
    });
  }

  private CreateWizardManager() {
    initWidget(binder.createAndBindUi(this));

    btnNext.addClickHandler(event -> {
      handleWizard();
    });

    btnBack.addClickHandler(event -> {
      if (position != 0) {
        wizardContent.clear();
        wizardContent.add(wizardInstances.get(--position));
        if (wizardInstances.get(position) instanceof Connection) {
          Connection conn = (Connection) wizardInstances.get(position);
          conn.clearPasswords();
        }

        if (wizardInstances.get(position) instanceof CustomViews) {
          CustomViews customViews = (CustomViews) wizardInstances.get(position);
          customViews.refreshCustomButtons();
        } else {
          customButtons.clear();
        }

        updateButtons();
        updateBreadcrumb();
      }
    });

    btnCancel.addClickHandler(event -> {
      clear();
      instance = null;
      HistoryManager.gotoHome();
    });
  }

  @Override
  protected void handleWizard() {
    switch (position) {
      case 0:
        handleConnectionPanel();
        break;
      case 1:
        handleTableAndColumnsPanel();
        break;
      case 2:
        handleCustomViewsPanel();
        break;
      case 3:
        handleSIARDExportOptions();
        break;
      case 4:
        handleMetadataExportOptions();
        break;
    }
  }

  private void handleConnectionPanel() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      connectionParameters = (ConnectionParameters) wizardInstances.get(position).getValues();

      Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
        "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

      wizardContent.add(spinner);

      ConnectionMapper mapper = GWT.create(ConnectionMapper.class);
      String connectionParametersJSON = mapper.write(connectionParameters);
      BrowserService.Util.getInstance().testConnection(databaseUUID, connectionParametersJSON,
        new DefaultAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean aBoolean) {
            wizardContent.clear();
            position = 1;
            TableAndColumns tableAndColumns = TableAndColumns.getInstance(databaseUUID, connectionParameters);
            wizardInstances.add(position, tableAndColumns);
            wizardContent.add(tableAndColumns);
            updateButtons();
            updateBreadcrumb();
            wizardContent.remove(spinner);
          }

          @Override
          public void onFailure(Throwable caught) {
            Dialogs.showErrors(messages.errorMessagesConnectionTitle(), caught.getMessage(),
              messages.basicActionClose());
            wizardContent.remove(spinner);
          }
        });
    } else {
      Connection connection = (Connection) wizardInstances.get(position);
      if (connection.sidebarWasClicked()) {
        wizardInstances.get(position).error();
        connection.clearPasswords();
      }
      Toast.showError(messages.createSIARDWizardManagerErrorTitle(),
        messages.createSIARDWizardManagerSelectDataSourceError());
    }
  }

  private void handleTableAndColumnsPanel() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      tableAndColumnsParameters = (TableAndColumnsParameters) wizardInstances.get(position).getValues();
      wizardContent.clear();
      position = 2;
      CustomViews customViews = CustomViews.getInstance(tableAndColumnsParameters.getSelectedSchemas(), btnNext,
        connectionParameters, databaseUUID);
      customViews.refreshCustomButtons();
      wizardInstances.add(position, customViews);
      wizardContent.add(customViews);
      updateButtons();
      updateBreadcrumb();
      customViews.checkIfHaveCustomViews();
    } else {
      wizardInstances.get(position).error();
    }
  }

  private void handleCustomViewsPanel() {
    final boolean valid = wizardInstances.get(position).validate();

    if (!valid) {
      Dialogs.showConfirmDialog(messages.customViewsPageTextForDialogTitle(),
        messages.customViewsPageTextForDialogMessage(), messages.basicActionDiscard(), messages.basicActionConfirm(),
        new DefaultAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            if (result) {
              if (wizardInstances.get(position) instanceof CustomViews) {
                final CustomViews customViewInstance = (CustomViews) wizardInstances.get(position);
                final DialogBox dialogBox = Dialogs.showWaitResponse(messages.customViewsPageTitle(),
                  messages.customViewsPageTextForDialogValidatingQuery());
                BrowserService.Util.getInstance().validateCustomViewQuery(databaseUUID, connectionParameters,
                  customViewInstance.getCustomViewParameter().getCustomViewQuery(),
                  new DefaultAsyncCallback<List<List<String>>>() {
                    @Override
                    public void onSuccess(List<List<String>> result) {
                      dialogBox.hide();
                      customViewsParameters = customViewInstance.getValues();
                      wizardContent.clear();
                      position = 3;
                      SIARDExportOptions exportOptions = SIARDExportOptions.getInstance();
                      wizardInstances.add(position, exportOptions);
                      wizardContent.add(exportOptions);
                      updateButtons();
                      updateBreadcrumb();
                      customButtons.clear();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      dialogBox.hide();
                      Dialogs.showErrors(messages.customViewsPageTitle(), caught.getMessage(),
                        messages.basicActionClose());
                    }
                  });
              }
            } else {
              customViewsParameters = (CustomViewsParameters) wizardInstances.get(position).getValues();
              customViewsParameters.getCustomViewsParameter()
                .remove(customViewsParameters.getCustomViewsParameter().size() - 1); // DISCARD THE LAST
              wizardContent.clear();
              position = 3;
              SIARDExportOptions exportOptions = SIARDExportOptions.getInstance();
              wizardInstances.add(position, exportOptions);
              wizardContent.add(exportOptions);
              updateButtons();
              updateBreadcrumb();
              customButtons.clear();
            }
          }
        });
    } else {
      customViewsParameters = (CustomViewsParameters) wizardInstances.get(position).getValues();
      wizardContent.clear();
      position = 3;
      SIARDExportOptions exportOptions = SIARDExportOptions.getInstance();
      wizardInstances.add(position, exportOptions);
      wizardContent.add(exportOptions);
      updateButtons();
      updateBreadcrumb();
      customButtons.clear();
    }
  }

  private void handleSIARDExportOptions() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      exportOptionsParameters = (ExportOptionsParameters) wizardInstances.get(position).getValues();

      if (!exportOptionsParameters.getSIARDVersion().equals(ViewerConstants.SIARDDK)) {
        wizardContent.clear();
        position = 4;
        MetadataExportOptions metadataExportOptions = MetadataExportOptions
          .getInstance(exportOptionsParameters.getSIARDVersion(), false);
        wizardInstances.add(position, metadataExportOptions);
        wizardContent.add(metadataExportOptions);
        updateButtons();
        updateBreadcrumb();
        customButtons.clear();
      } else {
        createSIARD(false);
      }
    } else {
      wizardInstances.get(position).error();
    }
  }

  private void handleMetadataExportOptions() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      metadataExportOptionsParameters = (MetadataExportOptionsParameters) wizardInstances.get(position).getValues();
      createSIARD(true);
    }
  }

  private void createSIARD(final boolean redirect) {
    wizardContent.clear();
    enableButtons(false);
    position = 5;
    updateBreadcrumb();

    ProgressBarPanel progressBarPanel = ProgressBarPanel.getInstance(databaseUUID);
    progressBarPanel.setTitleText(messages.progressBarPanelTextForCreateWizardProgressTitle());
    progressBarPanel.setSubtitleText(messages.progressBarPanelTextForCreateWizardProgressSubTitle());
    wizardContent.add(progressBarPanel);

    BrowserService.Util.getInstance().createSIARD(databaseUUID, connectionParameters, tableAndColumnsParameters,
      customViewsParameters, exportOptionsParameters, metadataExportOptionsParameters, new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {
          wizardContent.clear();
          position--;
          wizardContent.add(wizardInstances.get(position));
          enableButtons(true);
          updateBreadcrumb();
          Dialogs.showErrors(messages.createSIARDWizardManagerInformationMessagesTitle(), caught.getMessage(),
            messages.basicActionClose());
        }

        @Override
        public void onSuccess(Boolean result) {
          if (redirect) {
            final String siardPath = exportOptionsParameters.getSiardPath();
            Dialogs.showConfirmDialog(messages.createSIARDWizardManagerInformationMessagesTitle(),
              messages.createSIARDWizardManagerSIARDCreated(), messages.basicActionCancel(),
              messages.basicActionImport(), new DefaultAsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                  if (result) {
                    importSIARDMetadata(siardPath);
                  } else {
                    clear();
                    instance = null;
                    HistoryManager.gotoHome();
                  }
                }
              });
          } else {
            clear();
            instance = null;
            Dialogs.showInformationDialog(messages.createSIARDWizardManagerInformationMessagesTitle(),
              messages.createSIARDWizardManagerInformationMessage(), messages.basicActionClose(), "btn btn-link");
            HistoryManager.gotoHome();
          }
        }
      });
  }

  @Override
  protected void updateButtons() {
    btnBack.setEnabled(true);
    if (position != 2) {
      btnNext.setText(messages.basicActionNext());
    }

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
        breadcrumbItems = BreadcrumbManager.forCreateConnection();
        break;
      case 1:
        breadcrumbItems = BreadcrumbManager.forTableAndColumns();
        break;
      case 2:
        breadcrumbItems = BreadcrumbManager.forCustomViews();
        break;
      case 3:
        breadcrumbItems = BreadcrumbManager.forSIARDExportOptions();
        break;
      case 4:
        breadcrumbItems = BreadcrumbManager.forMetadataExportOptions();
        break;
      case 5:
        breadcrumbItems = BreadcrumbManager.forCreateSIARD();
        break;
      default:
        breadcrumbItems = new ArrayList<>();
        break;
    }

    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
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

    if (wizardInstances.isEmpty()) {
      HistoryManager.gotoCreateSIARD();
    } else {

      WizardPanel wizardPanel = wizardInstances.get(position);
      switch (wizardPage) {
        case HistoryManager.ROUTE_WIZARD_CONNECTION:
          if (wizardPanel instanceof Connection) {
            Connection connection = (Connection) wizardPanel;
            connection.sideBarHighlighter(toSelect);
          }
          break;
        case HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS:
          if (wizardPanel instanceof TableAndColumns) {
            TableAndColumns tableAndColumns = (TableAndColumns) wizardPanel;
            tableAndColumns.sideBarHighlighter(toSelect, schemaUUID, tableUUID);
          }
          break;
        case HistoryManager.ROUTE_WIZARD_CUSTOM_VIEWS:
          if (wizardPanel instanceof CustomViews) {
            CustomViews customViews = (CustomViews) wizardPanel;
            customViews.sideBarHighlighter(toSelect, schemaUUID);
          }
          break;
        case HistoryManager.ROUTE_WIZARD_EXPORT_SIARD_OPTIONS:
        case HistoryManager.ROUTE_WIZARD_EXPORT_EXT_OPTIONS:
        case HistoryManager.ROUTE_WIZARD_EXPORT_METADATA_OPTIONS:
        default:
          break;
      }
    }
  }

  private void importSIARDMetadata(String path) {
    final Widget loading = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div id='loading' class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));
    wizardContent.add(loading);
    BrowserService.Util.getInstance().uploadMetadataSIARD(databaseUUID, path, new DefaultAsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        clear();
        instance = null;
        wizardContent.remove(loading);
        HistoryManager.gotoSIARDInfo(databaseUUID);
      }

      @Override
      public void onFailure(Throwable caught) {
        HistoryManager.gotoHome();
        Dialogs.showErrors(messages.createSIARDWizardManagerInformationMessagesTitle(), caught.getMessage(),
          messages.basicActionClose());
      }
    });
  }
}
