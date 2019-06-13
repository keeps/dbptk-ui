package com.databasepreservation.visualization.client.main;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.browse.DatabaseListPanel;
import com.databasepreservation.visualization.client.browse.DatabasePanel;
import com.databasepreservation.visualization.client.browse.RightPanel;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.dialogs.Dialogs;
import com.databasepreservation.visualization.client.common.utils.JavascriptUtils;
import com.databasepreservation.visualization.client.common.utils.RightPanelLoader;
import com.databasepreservation.visualization.client.homePage.Card;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class HomePage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);


  interface HomePageUiBinder extends UiBinder<Widget, HomePage> {
  }

  private static HomePageUiBinder binder = GWT.create(HomePageUiBinder.class);

  @UiField
  FlowPanel contentPanel;


  HomePage() {
    initWidget(binder.createAndBindUi(this));

    init();
  }

  private void init() {
    Button btnCreate = new Button();
    btnCreate.setText(messages.createCardButton());

    btnCreate.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        Dialogs.showInformationDialog("Test", "Super test", "YEAH");
      }
    });

    Button btnOpen = new Button();
    btnOpen.setText(messages.openCardButton());

    btnOpen.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        String path = JavascriptUtils.openFileDialog();

        BrowserService.Util.getInstance().uploadSIARD(path, new DefaultAsyncCallback<String>() {
          @Override
          public void onFailure(Throwable caught) {
            Dialogs.showInformationDialog("Test", "Super test", "SADASDSADSA");
          }

          @Override
          public void onSuccess(String newDatabaseUUID) {
            Dialogs.showInformationDialog("Test", "Super test", "SADSA");
            HistoryManager.gotoSiardInfo(newDatabaseUUID);
          }
        });
      }
    });

    Button btnManage = new Button();
    btnManage.setText(messages.manageCardButton());

    btnManage.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        HistoryManager.gotoSiardInfo("test");
        JavascriptUtils.log(HistoryManager.linkToDatabaseList());
      }
    });

    Card createCard = Card.createInstance(messages.createCardHeader(), messages.createCardText(), btnCreate);
    Card openCard = Card.createInstance(messages.openCardHeader(), messages.openCardText(), btnOpen);
    Card manageCard = Card.createInstance(messages.manageCardHeader(), messages.manageCardText(), btnManage);

    contentPanel.add(createCard);
    contentPanel.add(openCard);
    contentPanel.add(manageCard);
  }


  void onHistoryChanged(String token) {
    List<String> currentHistoryPath = HistoryManager.getCurrentHistoryPath();
    List<BreadcrumbItem> breadcrumbItemList = new ArrayList<>();

    if (currentHistoryPath.isEmpty()) {
      HistoryManager.gotoHome();
    } else if (HistoryManager.ROUTE_SIARD_INFO.equals(currentHistoryPath.get(0))) {
      SIARDInfo instance = SIARDInfo.getInstance(currentHistoryPath.get(1));

      contentPanel.clear();
      contentPanel.add(instance);

    } else {
      handleErrorPath(currentHistoryPath);
    }
  }

  private void handleErrorPath(List<String> currentHistoryPath) {
    HistoryManager.gotoHome();
  }
}