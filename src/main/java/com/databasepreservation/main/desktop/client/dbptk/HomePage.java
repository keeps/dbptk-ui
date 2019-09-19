package com.databasepreservation.main.desktop.client.dbptk;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.common.Card;
import com.databasepreservation.main.desktop.client.common.helper.HelperUploadSIARDFile;
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

  @UiField
  Image ownerLink, DGLABFinanciersLink, EARKFinanciersLink, applicationLink, NAEFinanciersLink;

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

    btnManage.addClickHandler(event -> HistoryManager.gotoDesktopDatabase());

    Card createCard = Card.createInstance(messages.homePageHeaderTextForCreateSIARD(), messages.homePageDescriptionTextForCreateSIARD(), btnCreate);
    Card openCard = Card.createInstance(messages.homePageHeaderTextForOpenSIARD(), messages.homePageDescriptionTextForOpenSIARD(), btnOpen);
    Card manageCard = Card.createInstance(messages.homePageHeaderTextForManageSIARD(), messages.homePageDescriptionTextForManageSIARD(), btnManage);

    options.add(createCard);
    options.add(openCard);
    options.add(manageCard);

    applicationLink.addClickHandler(event -> {
      Window.open(ViewerConstants.APPLICATION_LINK, ViewerConstants.BLANK_LINK, null);
    });

    NAEFinanciersLink.addClickHandler(event -> {
      Window.open(ViewerConstants.NAE_FINANCIER_LINK, ViewerConstants.BLANK_LINK, null);
    });

    EARKFinanciersLink.addClickHandler(event -> {
      Window.open(ViewerConstants.EARK_FINANCIER_LINK, ViewerConstants.BLANK_LINK, null);
    });

    DGLABFinanciersLink.addClickHandler(event -> {
      Window.open(ViewerConstants.DGLAB_FINANCIER_LINK, ViewerConstants.BLANK_LINK, null);
    });

    ownerLink.addClickHandler(event -> {
      Window.open(ViewerConstants.OWNER_LINK, ViewerConstants.BLANK_LINK, null);
    });
  }
}