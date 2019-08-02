package com.databasepreservation.main.desktop.client.dbptk;

import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.common.Card;
import com.databasepreservation.main.desktop.client.common.helper.HelperUploadSIARDFile;
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
public class HomePage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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
    btnCreate.setText(messages.createCardButton());
    btnCreate.addStyleName("btn btn-edit");

    btnCreate.addClickHandler(event -> HistoryManager.gotoCreateSIARD());

    Button btnOpen = new Button();
    btnOpen.setText(messages.openCardButton());
    btnOpen.addStyleName("btn btn-plus");

    btnOpen.addClickHandler(event -> new HelperUploadSIARDFile().openFile(options));

    Button btnManage = new Button();
    btnManage.setText(messages.manageCardButton());
    btnManage.addStyleName("btn btn-manage");

    btnManage.addClickHandler(event -> HistoryManager.gotoDatabaseList());

    Card createCard = Card.createInstance(messages.createCardHeader(), messages.createCardText(), btnCreate);
    Card openCard = Card.createInstance(messages.openCardHeader(), messages.openCardText(), btnOpen);
    Card manageCard = Card.createInstance(messages.manageCardHeader(), messages.manageCardText(), btnManage);

    options.add(createCard);
    options.add(openCard);
    options.add(manageCard);
  }
}