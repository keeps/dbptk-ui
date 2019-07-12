package com.databasepreservation.main.desktop.client.common.sidebar;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarItem;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
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
public class TableAndColumnsSidebar extends Composite {
  public static final String DATABASE_LINK = "database";
  public static final String TABLES_LINK = "tables";
  public static final String TABLE_LINK = "table";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SidebarHyperlink> list = new HashMap<>();

  interface TableAndColumnsSidebarUiBinder extends UiBinder<Widget, TableAndColumnsSidebar> {
  }

  private static TableAndColumnsSidebarUiBinder uiBinder = GWT.create(TableAndColumnsSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  public static TableAndColumnsSidebar newInstance(ViewerMetadata viewerMetadata) {
    return new TableAndColumnsSidebar(viewerMetadata);
  }

  private TableAndColumnsSidebar(ViewerMetadata viewerMetadata) {
    initWidget(uiBinder.createAndBindUi(this));
    init(viewerMetadata);
  }

  private void init(ViewerMetadata viewerMetadata) {

    SidebarHyperlink database = new SidebarHyperlink(messages.menusidebar_database(),
      HistoryManager.linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS, DATABASE_LINK));
    database.addIcon(FontAwesomeIconManager.DATABASE).setH5().setIndent0();
    list.put(DATABASE_LINK, database);
    sidebarGroup.add(database);

    for (ViewerSchema schema : viewerMetadata.getSchemas()) {
      sidebarGroup.add(new SidebarItem(schema.getName()).addIcon(FontAwesomeIconManager.SCHEMA).setH5().setIndent1());

      SidebarHyperlink tables = new SidebarHyperlink(messages.sidebarTables(),
        HistoryManager.linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS, TABLES_LINK, schema.getUUID()));
      tables.addIcon(FontAwesomeIconManager.LIST).setH5().setIndent2();
      list.put(TABLES_LINK, tables);
      sidebarGroup.add(tables);

      for (ViewerTable table : schema.getTables()) {
        SidebarHyperlink sidebarHyperlink = new SidebarHyperlink(table.getName(),
          HistoryManager.linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS, TABLE_LINK, schema.getUUID(), table.getUUID()));
        sidebarHyperlink.addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent3();
        list.put(table.getName(), sidebarHyperlink);
        sidebarGroup.add(sidebarHyperlink);
      }
    }

    setVisible(true);
  }

  public void select(String value) {

    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      if (entry.getKey().equals(value)) {
        list.get(value).setSelected(true);
      } else {
        list.get(entry.getKey()).setSelected(false);
      }
    }
  }
}
