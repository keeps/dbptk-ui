package com.databasepreservation.main.desktop.client.common.sidebar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.databasepreservation.main.common.shared.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarItem;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.shared.models.ConnectionModule;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConnectionSidebar extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final String headerText;
  private final String headerIcon;
  private final String itemIcon;
  private final ConnectionModule items;
  private static Map<String, ConnectionSidebar> instances = new HashMap<>();
  private static Map<String, SidebarHyperlink> list = new HashMap<>();

  interface ConnectionSidebarUiBinder extends UiBinder<Widget, ConnectionSidebar> {
  }

  private static ConnectionSidebarUiBinder uiBinder = GWT.create(ConnectionSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  public static ConnectionSidebar getInstance(String headerText, String headerIcon, String itemIcon,
    ConnectionModule items) {
    if (instances.get(headerText) == null) {
      ConnectionSidebar instance = new ConnectionSidebar(headerText, headerIcon, itemIcon, items);
      instances.put(headerText, instance);
    }

    return instances.get(headerText);
  }

  public static ConnectionSidebar getInstance(String headerText, String itemIcon, ConnectionModule items) {
    return ConnectionSidebar.getInstance(headerText, null, itemIcon, items);
  }

  private ConnectionSidebar(String headerText, String headerIcon, String itemIcon, ConnectionModule items) {
    this.headerText = headerText;
    this.headerIcon = headerIcon;
    this.itemIcon = itemIcon;
    this.items = items;
    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    if (headerIcon != null) {
      sidebarGroup.add(new SidebarItem(headerText).addIcon(headerIcon).setH5().setIndent0());
    } else {
      sidebarGroup.add(new SidebarItem(headerText).setH5().setIndent0());
    }

    final TreeMap<String, ArrayList<PreservationParameter>> parameters = new TreeMap<>(items.getParameters());

    for (String moduleName : parameters.keySet()) {
      SidebarHyperlink sidebarHyperlink = new SidebarHyperlink(ToolkitModuleName2ViewerModuleName.transform(moduleName), HistoryManager.linkToCreateSIARD(moduleName));
      sidebarHyperlink.addIcon(itemIcon).setH6().setIndent1();
      list.put(moduleName, sidebarHyperlink);
      sidebarGroup.add(sidebarHyperlink);
    }

    setVisible(true);
  }

  public void select(String connection) {

    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      if (entry.getKey().equals(connection)) {
        list.get(connection).setSelected(true);
      } else {
        list.get(entry.getKey()).setSelected(false);
      }
    }
  }
}
