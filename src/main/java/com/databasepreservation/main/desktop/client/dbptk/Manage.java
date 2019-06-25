package com.databasepreservation.main.desktop.client.dbptk;

import java.util.List;

import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Manage extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ManageUiBinder extends UiBinder<Widget, Manage> {
  }

  private static ManageUiBinder binder = GWT.create(ManageUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  private static Manage instance = null;

  public static Manage getInstance() {
    if (instance == null) {
      instance = new Manage();
    }
    return instance;
  }

  private Manage() {
    initWidget(binder.createAndBindUi(this));

    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forManageSIARD();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }
}