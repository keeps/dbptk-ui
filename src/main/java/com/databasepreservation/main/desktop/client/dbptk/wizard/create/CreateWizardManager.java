package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.utils.AsyncCallbackUtils;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.codehaus.janino.Java;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class CreateWizardManager extends Composite {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface CreateWizardManagerUiBinder extends UiBinder<Widget, CreateWizardManager> {
  }

  private static CreateWizardManagerUiBinder binder = GWT.create(CreateWizardManagerUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel wizardContent;

  @UiField
  Button btnNext, btnCancel, btnBack;

  private ArrayList<WizardPanel> wizardInstances = new ArrayList<>();
  private int position = 0;
  private final int positions = 6;

  private static CreateWizardManager instance = null;

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
    Connection connection = Connection.getInstance();
    wizardContent.clear();
    wizardInstances.add(0, connection);
    wizardContent.add(connection);
  }

  private CreateWizardManager() {
    initWidget(binder.createAndBindUi(this));

    /*
     * CustomViews customViews = CustomViews.getInstance(); SIARDExportOptions
     * SIARDOptions = SIARDExportOptions.getInstance(); ExternalLOBExportOptions
     * externalLOBOptions = ExternalLOBExportOptions.getInstance();
     * MetadataExportOptions metadataOptions = MetadataExportOptions.getInstance();
     */

    /*
     * wizardInstances.add(2, customViews); wizardInstances.add(3, SIARDOptions);
     * wizardInstances.add(4, externalLOBOptions); wizardInstances.add(5,
     * metadataOptions);
     */

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
        updateButtons();
        updateBreadcrumb();
      }
    });

    btnCancel.addClickHandler(event -> {
      clear();
      HistoryManager.gotoHome();
    });

    init();
  }

  private void handleWizard() {
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
        handleExternalLOBSExportOptions();
        break;
      case 5:
        handleMetadataExportOptions();
        break;
    }
  }

  private void handleConnectionPanel() {
    final boolean valid = wizardInstances.get(position).validate();
    if (valid) {
      HashMap<String, String> values = wizardInstances.get(position).getValues();
      String moduleName = values.get("module_name");
      values.remove("module_name");

      Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
        "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

      wizardContent.add(spinner);

      BrowserService.Util.getInstance().testConnection("test", moduleName, values, new DefaultAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean aBoolean) {
          wizardContent.clear();
          position = 1;
          TableAndColumns tableAndColumns = TableAndColumns.getInstance(moduleName, values);
          wizardInstances.add(position, tableAndColumns);
          wizardContent.add(tableAndColumns);
          updateButtons();
          updateBreadcrumb();

          wizardContent.remove(spinner);
        }

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
          wizardContent.remove(spinner);
        }
      });
    } else {
      wizardInstances.get(position).error();
      Connection connection = (Connection) wizardInstances.get(position);
      connection.clearPasswords();

    }
  }

  private void handleTableAndColumnsPanel() {
    wizardInstances.get(position).getValues();
  }

  private void handleCustomViewsPanel() {
  }

  private void handleSIARDExportOptions() {
  }

  private void handleExternalLOBSExportOptions() {
  }

  private void handleMetadataExportOptions() {
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

  private void updateBreadcrumb() {
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
        breadcrumbItems = BreadcrumbManager.forExternalLOBSExportOptions();
        break;
      case 5:
        breadcrumbItems = BreadcrumbManager.forMetadataExportOptions();
        break;
      default: breadcrumbItems = new ArrayList<>();
        break;
    }

    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  private void clear() {
    for (WizardPanel panel : wizardInstances) {
      panel.clear();
    }
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
          customViews.sideBarHighlighter(toSelect);
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
