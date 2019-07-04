package com.databasepreservation.main.desktop.client.main;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.dbptk.EditMetadataInformation;
import com.databasepreservation.main.desktop.client.dbptk.HomePage;
import com.databasepreservation.main.desktop.client.dbptk.Manage;
import com.databasepreservation.main.desktop.client.dbptk.SIARDEditMetadataPage;
import com.databasepreservation.main.desktop.client.dbptk.SIARDMainPage;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.CreateWizardManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MainPanelDesktop extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MainPanelDesktopUiBinder extends UiBinder<Widget, MainPanelDesktop> {
  }

  private static MainPanelDesktopUiBinder binder = GWT.create(MainPanelDesktopUiBinder.class);

  @UiField
  SimplePanel contentPanel;

  MainPanelDesktop() {
    initWidget(binder.createAndBindUi(this));
  }

  private void setRightPanelContent(String databaseUUID, Composite content) {
    GWT.log("setRightPanelContent, dbuid " + databaseUUID);
    SIARDEditMetadataPage instance = SIARDEditMetadataPage.getInstance(databaseUUID);
    contentPanel.setWidget(instance);
    instance.load(content);

  }

  void onHistoryChanged(String token) {
    List<String> currentHistoryPath = HistoryManager.getCurrentHistoryPath();
    List<BreadcrumbItem> breadcrumbItemList = new ArrayList<>();

    if (currentHistoryPath.isEmpty() || HistoryManager.ROUTE_HOME.equals(currentHistoryPath.get(0))) {
      contentPanel.clear();
      contentPanel.add(HomePage.getInstance());

    } else if (HistoryManager.ROUTE_SIARD_INFO.equals(currentHistoryPath.get(0))) {
      SIARDMainPage instance = SIARDMainPage.getInstance(currentHistoryPath.get(1));

      contentPanel.clear();
      contentPanel.add(instance);

    } else if (HistoryManager.ROUTE_DATABASE.equals(currentHistoryPath.get(0))) {
      Manage manage = Manage.getInstance();
      contentPanel.clear();
      contentPanel.add(manage);
    } else if (HistoryManager.ROUTE_CREATE_SIARD.equals(currentHistoryPath.get(0))) {
      CreateWizardManager instance = CreateWizardManager.getInstance();
      contentPanel.clear();
      if (currentHistoryPath.size() == 3) {
        instance.change(currentHistoryPath.get(1), currentHistoryPath.get(2));
      } else if (currentHistoryPath.size() == 4) {
        final String wizardPage = currentHistoryPath.get(1);
        final String toSelect = currentHistoryPath.get(2);
        final String schemaUUID = currentHistoryPath.get(3);
        instance.change(wizardPage, toSelect, schemaUUID);
      } else if (currentHistoryPath.size() == 5) {
        final String wizardPage = currentHistoryPath.get(1);
        final String toSelect = currentHistoryPath.get(2);
        final String schemaUUID = currentHistoryPath.get(3);
        final String tableUUID = currentHistoryPath.get(4);
        instance.change(wizardPage, toSelect, schemaUUID, tableUUID);
      }
      contentPanel.add(instance);
    }  else if (HistoryManager.ROUTE_SIARD_EDIT_METADATA.equals(currentHistoryPath.get(0))) {
      String databaseUUID =  currentHistoryPath.get(1);

      EditMetadataInformation instance = EditMetadataInformation.getInstance(databaseUUID);
      setRightPanelContent(databaseUUID, instance );
    } else {
      handleErrorPath(currentHistoryPath);
    }
  }

  private void handleErrorPath(List<String> currentHistoryPath) {
    HistoryManager.gotoHome();
  }
}