package com.databasepreservation.common.client.common.visualization.preferences;

import java.util.List;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PreferencesPanel extends ContentPanel {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forPreferencesPanel();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  interface ManageUiBinder extends UiBinder<Widget, PreferencesPanel> {
  }

  private static ManageUiBinder binder = GWT.create(ManageUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  SimplePanel description;

  @UiField
  FlowPanel content;

  public static PreferencesPanel createInstance() {
    return new PreferencesPanel();
  }

  private PreferencesPanel() {
    ;
    initWidget(binder.createAndBindUi(this));

    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.PREFERENCES),
      messages.menuTextForPreferences(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.preferencesPanelTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");

    description.setWidget(instance);

    content.add(new Alert(Alert.MessageAlertType.INFO, "Under construction"));
  }
}