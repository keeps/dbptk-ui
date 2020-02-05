package com.databasepreservation.server.client.browse;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.widgets.HTMLWidgetWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

import java.util.ArrayList;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class  HomePanel extends ContentPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static HomePanelUiBinder uiBinder = GWT.create(HomePanelUiBinder.class);

  interface HomePanelUiBinder extends UiBinder<Widget, HomePanel> {
  }

  private static HomePanel instance = null;

  @UiField (provided = true)
  HTMLWidgetWrapper layout;

  public static HomePanel getInstance() {
    if (instance == null) {
      instance = new HomePanel();
    }
    return instance;
  }

  public HomePanel() {
    layout = new HTMLWidgetWrapper("Welcome.html");
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
    BreadcrumbManager.updateBreadcrumb(breadcrumb, new ArrayList<>());
  }
}
