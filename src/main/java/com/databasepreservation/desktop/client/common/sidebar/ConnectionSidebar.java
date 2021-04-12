/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.desktop.client.common.sidebar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.databasepreservation.common.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.common.client.common.sidebar.SidebarItem;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.parameters.PreservationParameter;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ToolkitModuleName2ViewerModuleName;
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
  private final List<Module> modules;
  private Map<String, SidebarHyperlink> list = new HashMap<>();
  private static Map<String, ConnectionSidebar> instances = new HashMap<>();

  interface ConnectionSidebarUiBinder extends UiBinder<Widget, ConnectionSidebar> {
  }

  private static ConnectionSidebarUiBinder uiBinder = GWT.create(ConnectionSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  public static ConnectionSidebar getInstance(String databaseUUID, String headerText, String headerIcon,
    List<Module> modules, String targetHistoryToken) {
    return instances.computeIfAbsent(databaseUUID,
      k -> new ConnectionSidebar(databaseUUID, headerText, headerIcon, modules, targetHistoryToken));
  }

  public static ConnectionSidebar getInstance(String databaseUUID, String headerText, List<Module> modules,
    String targetHistoryToken) {
    return ConnectionSidebar.getInstance(databaseUUID, headerText, null, modules, targetHistoryToken);
  }

  private ConnectionSidebar(String databaseUUID, String headerText, String headerIcon, List<Module> modules,
    String targetHistoryToken) {
    this.headerText = headerText;
    this.headerIcon = headerIcon;
    this.modules = modules;
    initWidget(uiBinder.createAndBindUi(this));
    init(targetHistoryToken, databaseUUID);
  }

  private void init(String target, String databaseUUID) {
    if (headerIcon != null) {
      sidebarGroup.add(new SidebarItem(headerText).addIcon(headerIcon).setH5().setIndent0());
    } else {
      sidebarGroup.add(new SidebarItem(headerText).setH5().setIndent0());
    }

    final Map<String, List<PreservationParameter>> parameters = new TreeMap<>();

    modules.forEach(module -> {
      parameters.put(module.getModuleName(), module.getParameters());
    });

    for (String moduleName : parameters.keySet()) {
      String targetHistoryToken = null;
      if (target.equals(HistoryManager.ROUTE_SEND_TO_LIVE_DBMS)) {
        targetHistoryToken = HistoryManager.linkToSendToWizardDBMSConnection(databaseUUID,
          HistoryManager.ROUTE_WIZARD_CONNECTION, moduleName);
      } else if (target.equals(HistoryManager.ROUTE_CREATE_SIARD)) {
        targetHistoryToken = HistoryManager.linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_CONNECTION, moduleName);
      }

      SidebarHyperlink sidebarHyperlink = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(
        FontAwesomeIconManager.DATABASE, ToolkitModuleName2ViewerModuleName.transform(moduleName)), targetHistoryToken);
      sidebarHyperlink.setH6().setIndent1();
      list.put(moduleName, sidebarHyperlink);
      sidebarGroup.add(sidebarHyperlink);
    }

    setVisible(true);
  }

  public void select(String connection) {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      entry.getValue().setSelected(entry.getKey().equals(connection));
    }
  }

  public void selectNone() {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      entry.getValue().setSelected(false);
    }
  }
}
