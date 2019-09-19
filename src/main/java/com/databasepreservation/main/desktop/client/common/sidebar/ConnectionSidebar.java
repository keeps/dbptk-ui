package com.databasepreservation.main.desktop.client.common.sidebar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarItem;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.main.common.shared.models.DBPTKModule;
import com.databasepreservation.main.common.shared.models.PreservationParameter;
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
  private final DBPTKModule dbptkModule;
  private Map<String, SidebarHyperlink> list = new HashMap<>();
  private static Map<String, ConnectionSidebar> instances = new HashMap<>();

  interface ConnectionSidebarUiBinder extends UiBinder<Widget, ConnectionSidebar> {
  }

  private static ConnectionSidebarUiBinder uiBinder = GWT.create(ConnectionSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  public static ConnectionSidebar getInstance(String databaseUUID, String headerText, String headerIcon, String itemIcon,
    DBPTKModule items, String targetHistoryToken) {
    if (instances.get(databaseUUID) == null) {
      ConnectionSidebar instance = new ConnectionSidebar(databaseUUID, headerText, headerIcon, itemIcon, items, targetHistoryToken);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  public static ConnectionSidebar getInstance(String databaseUUID, String headerText, String itemIcon, DBPTKModule dbptkModule, String targetHistoryToken) {
    return ConnectionSidebar.getInstance(databaseUUID, headerText, null, itemIcon, dbptkModule, targetHistoryToken);
  }

  private ConnectionSidebar(String databaseUUID, String headerText, String headerIcon, String itemIcon, DBPTKModule dbptkModule, String targetHistoryToken) {
    this.headerText = headerText;
    this.headerIcon = headerIcon;
    this.itemIcon = itemIcon;
    this.dbptkModule = dbptkModule;
    initWidget(uiBinder.createAndBindUi(this));
    init(targetHistoryToken, databaseUUID);
  }

  private void init(String target, String databaseUUID) {
    if (headerIcon != null) {
      sidebarGroup.add(new SidebarItem(headerText).addIcon(headerIcon).setH5().setIndent0());
    } else {
      sidebarGroup.add(new SidebarItem(headerText).setH5().setIndent0());
    }

    final TreeMap<String, ArrayList<PreservationParameter>> parameters = new TreeMap<>(dbptkModule.getParameters());

    for (String moduleName : parameters.keySet()) {
      String targetHistoryToken = null;
      if (target.equals(HistoryManager.ROUTE_SEND_TO_LIVE_DBMS)) {
        targetHistoryToken = HistoryManager.linkToSendToWizardDBMSConnection(databaseUUID, HistoryManager.ROUTE_WIZARD_CONNECTION, moduleName);
      } else if (target.equals(HistoryManager.ROUTE_CREATE_SIARD)) {
        targetHistoryToken = HistoryManager.linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_CONNECTION, moduleName);
      }

      SidebarHyperlink sidebarHyperlink = new SidebarHyperlink(ToolkitModuleName2ViewerModuleName.transform(moduleName),
        targetHistoryToken);
      sidebarHyperlink.addIcon(itemIcon).setH6().setIndent1();
      list.put(moduleName, sidebarHyperlink);
      sidebarGroup.add(sidebarHyperlink);
    }

    setVisible(true);
  }

  public void select(String connection) {

    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      if (entry.getKey().equals(connection)) {
        entry.getValue().setSelected(true);
      } else {
        entry.getValue().setSelected(false);
      }
    }
  }

  public void selectNone() {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      entry.getValue().setSelected(false);
    }
  }
}
