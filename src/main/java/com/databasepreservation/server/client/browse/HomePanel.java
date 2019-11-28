package com.databasepreservation.server.client.browse;

import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class HomePanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static HomePanelUiBinder uiBinder = GWT.create(HomePanelUiBinder.class);

  interface HomePanelUiBinder extends UiBinder<Widget, HomePanel> {
  }

  private static HomePanel instance = null;

  public static HomePanel getInstance() {
    if (instance == null) {
      instance = new HomePanel();
    }
    return instance;
  }

  public HomePanel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forHome());
  }
}
