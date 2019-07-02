package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions.ExternalLOBExportOptions;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions.MetadataExportOptions;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions.SIARDExportOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

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
  private int position;

  private static CreateWizardManager instance = null;

  public static CreateWizardManager getInstance() {
    if (instance == null) {
      instance = new CreateWizardManager();
    }

    instance.init();
    return instance;
  }

  private void init() {
    updateButtons();
    updateBreadcrumb();
    wizardContent.clear();
    wizardContent.add(wizardInstances.get(position));
  }

  private CreateWizardManager() {
    initWidget(binder.createAndBindUi(this));

    Connection connection = Connection.getInstance();
    TableAndColumns tableAndColumns = TableAndColumns.getInstance();
    CustomViews customViews = CustomViews.getInstance();
    SIARDExportOptions SIARDOptions = SIARDExportOptions.getInstance();
    ExternalLOBExportOptions externalLOBOptions = ExternalLOBExportOptions.getInstance();
    MetadataExportOptions metadataOptions = MetadataExportOptions.getInstance();

    wizardInstances.add(0, connection);
    wizardInstances.add(1, tableAndColumns);
    wizardInstances.add(2, customViews);
    wizardInstances.add(3, SIARDOptions);
    wizardInstances.add(4, externalLOBOptions);
    wizardInstances.add(5, metadataOptions);

    position = 0;

    btnNext.addClickHandler(event -> {
      wizardContent.clear();
      wizardInstances.get(0).getValues();
      wizardContent.add(wizardInstances.get(++position));
      updateButtons();
      updateBreadcrumb();

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
      HistoryManager.gotoHome();
    });

    init();
  }

  private void updateButtons() {

    btnBack.setEnabled(true);
    btnNext.setText(messages.next());

    if (position == 0) {
      btnBack.setEnabled(false);
    }

    if (position == wizardInstances.size() - 1) {
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
    position = 0;
    for (WizardPanel panel : wizardInstances) {
      panel.clear();
    }
  }

  public void changeConnectionPage(String page) {
    if (position == 0) {
      Connection connection = (Connection) wizardInstances.get(0);
      connection.change(page);
    }
  }
}
