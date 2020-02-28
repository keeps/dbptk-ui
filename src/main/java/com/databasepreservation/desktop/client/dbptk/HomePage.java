package com.databasepreservation.desktop.client.dbptk;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.helpers.HelperUploadSIARDFile;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.desktop.client.common.Card;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class HomePage extends Composite {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface HomePageUiBinder extends UiBinder<Widget, HomePage> {
  }

  private static HomePageUiBinder binder = GWT.create(HomePageUiBinder.class);

  @UiField
  FlowPanel options;

  private static HomePage instance = null;

  public static HomePage getInstance() {
    if (instance == null) {
      instance = new HomePage();
    }
    return instance;
  }

  private HomePage() {
    initWidget(binder.createAndBindUi(this));

    init();
  }

  private void init() {
    Button btnCreate = new Button();
    btnCreate.setText(messages.homePageButtonTextForCreateSIARD());
    btnCreate.addStyleName("btn btn-edit");

    btnCreate.addClickHandler(event -> HistoryManager.gotoCreateSIARD());

    Button btnOpen = new Button();
    btnOpen.setText(messages.homePageButtonTextForOpenSIARD());
    btnOpen.addStyleName("btn btn-plus");

    btnOpen.addClickHandler(event -> new HelperUploadSIARDFile().openFile(options));

    Button btnManage = new Button();
    btnManage.setText(messages.homePageButtonTextForManageSIARD());
    btnManage.addStyleName("btn btn-manage");

    btnManage.addClickHandler(event -> HistoryManager.gotoDatabase());

    Card createCard = Card.createInstance(messages.homePageHeaderTextForCreateSIARD(),
      messages.homePageDescriptionTextForCreateSIARD(), btnCreate);
    Card openCard = Card.createInstance(messages.homePageHeaderTextForOpenSIARD(),
      messages.homePageDescriptionTextForOpenSIARD(), btnOpen);
    Card manageCard = Card.createInstance(messages.homePageHeaderTextForManageSIARD(),
      messages.homePageDescriptionTextForManageSIARD(), btnManage);

    options.add(createCard);
    options.add(openCard);
    options.add(manageCard);
  }
}