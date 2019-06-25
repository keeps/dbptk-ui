package com.databasepreservation.main.desktop.client.main;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.dbptk.HomePage;
import com.databasepreservation.main.desktop.client.dbptk.Manage;
import com.databasepreservation.main.desktop.client.dbptk.SIARDMainPage;
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
    } else {
      handleErrorPath(currentHistoryPath);
    }
  }

  private void handleErrorPath(List<String> currentHistoryPath) {
    HistoryManager.gotoHome();
  }
}