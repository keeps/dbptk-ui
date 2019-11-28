package com.databasepreservation.desktop.client.common.sidebar;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.common.client.common.sidebar.SidebarItem;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomViewsSidebar extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SidebarHyperlink> list = new HashMap<>();

  interface ConnectionSidebarUiBinder extends UiBinder<Widget, CustomViewsSidebar> {
  }

  private static ConnectionSidebarUiBinder uiBinder = GWT.create(ConnectionSidebarUiBinder.class);

  private static CustomViewsSidebar instance = null;

  @UiField
  FlowPanel sidebarGroup;

  public static CustomViewsSidebar getInstance() {
    if (instance == null) {
      instance = new CustomViewsSidebar();
    }
    return instance;
  }

  private CustomViewsSidebar() {
    initWidget(uiBinder.createAndBindUi(this));

    sidebarGroup.add(
      new SidebarItem(
        FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.COG, messages.customViewsPageTitle()))
          .setH5().setIndent1());
  }

  public void addSideBarHyperLink(final String nameToDisplay, final String customViewUUID, final String targetHistoryToken) {
    Hyperlink delete = new Hyperlink();
    delete.addStyleName("sidebar-hyperlink sidebarItem custom-views-sidebarItem-delete");
    delete.asWidget().addStyleName("far fa-trash-alt");
    delete.setTargetHistoryToken(targetHistoryToken);

    SidebarHyperlink customView = new SidebarHyperlink(
      FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.LIST, nameToDisplay),
      HistoryManager.linkToCreateWizardCustomViewsSelect(customViewUUID), delete);

    customView.setH5().setIndent2();
    list.put(customViewUUID, customView);
    sidebarGroup.add(customView);
  }

  public void updateSidebarHyperLink(final String id, final String customViewNameText) {
    final SidebarHyperlink sidebarHyperlink = list.get(id);
    sidebarHyperlink.setText(customViewNameText);
    list.put(id, sidebarHyperlink);
  }

  public void removeSideBarHyperLink(final String customViewUUID) {
    sidebarGroup.remove(list.get(customViewUUID));
  }

  public void selectNone() {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      entry.getValue().setSelected(false);
    }
  }

  public void select(String value) {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      list.get(value).setSelected(entry.getKey().equals(value));
    }
  }

  public void clear() {
    if (!list.isEmpty()) {
      list.clear();
    }
  }
}
