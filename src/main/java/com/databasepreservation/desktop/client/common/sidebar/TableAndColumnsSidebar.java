package com.databasepreservation.desktop.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.common.client.common.sidebar.SidebarItem;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumnsSidebar extends Composite {
  public static final String DATABASE_LINK = "database";
  public static final String TABLES_LINK = "tables";
  public static final String TABLE_LINK = "table";
  public static final String VIEW_LINK = "view";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SidebarHyperlink> list = new HashMap<>();

  interface TableAndColumnsSidebarUiBinder extends UiBinder<Widget, TableAndColumnsSidebar> {
  }

  private static TableAndColumnsSidebarUiBinder uiBinder = GWT.create(TableAndColumnsSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  public static TableAndColumnsSidebar newInstance(ViewerMetadata viewerMetadata) {
    return new TableAndColumnsSidebar(viewerMetadata);
  }

  private TableAndColumnsSidebar(ViewerMetadata viewerMetadata) {
    initWidget(uiBinder.createAndBindUi(this));
    init(viewerMetadata);
  }

  private void init(ViewerMetadata viewerMetadata) {

    SidebarHyperlink database = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.DATABASE, messages.menusidebar_database()),
      HistoryManager.linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS, DATABASE_LINK));
    database.setH5().setIndent0();
    list.put(DATABASE_LINK, database);
    sidebarGroup.add(database);

    for (ViewerSchema schema : viewerMetadata.getSchemas()) {

      SidebarHyperlink schemas = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA, schema.getName()),
              HistoryManager.linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS, TABLES_LINK, schema.getUuid()));
      schemas.setH5().setIndent1();
      list.put(schema.getUuid(), schemas);
      sidebarGroup.add(schemas);

      SidebarItem tables = new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.LIST, messages.sidebarMenuTextForTables()));
      tables.setH5().setIndent2();

      FlowPanel tablesItems = new FlowPanel();
      for (ViewerTable table : schema.getTables()) {
        final SidebarHyperlink sidebarHyperlink = buildSidebarHyperLink(table.getName(), HistoryManager
          .linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS, TABLE_LINK, schema.getUuid(), table.getUuid()),
          FontAwesomeIconManager.TABLE);
        list.put(ViewerStringUtils.concat(schema.getUuid(), table.getUuid()), sidebarHyperlink);
        tablesItems.add(sidebarHyperlink);
      }
      createSubItem(tables, tablesItems);

      SidebarItem views = new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.LIST, messages.sidebarMenuTextForViews()));
      views.setH5().setIndent2();

      FlowPanel viewsItems = new FlowPanel();
      for (ViewerView view : schema.getViews()) {
        SidebarHyperlink sidebarHyperlink = buildSidebarHyperLink(view.getName(), HistoryManager
          .linkToCreateSIARD(HistoryManager.ROUTE_WIZARD_TABLES_COLUMNS, VIEW_LINK, schema.getUuid(), view.getUuid()),
          FontAwesomeIconManager.SCHEMA_VIEWS);
        list.put(ViewerStringUtils.concat(schema.getUuid(), view.getUuid()), sidebarHyperlink);
        viewsItems.add(sidebarHyperlink);
      }
      createSubItem(views, viewsItems);

      searchInit();
    }
    setVisible(true);
  }

  private void createSubItem(SidebarItem header, FlowPanel content) {
    DisclosurePanel panel = new DisclosurePanel();
    panel.setAnimationEnabled(true);
    panel.setHeader(header);
    panel.setContent(content);
    panel.getElement().addClassName("sidebar-collapse");
    panel.setOpen(true);
    sidebarGroup.add(panel);
  }

  public void select(String value) {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      entry.getValue().setSelected(entry.getKey().equals(value));
    }
  }

  public void selectNone() {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      entry.getValue().setSelected(false);
    }
  }

  private void searchInit() {
    searchInputBox.getElement().setPropertyString("placeholder", messages.menusidebar_filterSidebar());

    searchInputBox.addChangeHandler(event -> doSearch());

    searchInputBox.addKeyUpHandler(event -> doSearch());

    searchInputButton.addClickHandler(event -> doSearch());
  }

  private void doSearch() {
    String searchValue = searchInputBox.getValue();

    if (ViewerStringUtils.isBlank(searchValue)) {
      // show all
      for (Widget widget : sidebarGroup) {
        widget.setVisible(true);

        if (widget instanceof DisclosurePanel) {
          DisclosurePanel disclosurePanel = (DisclosurePanel) widget;
          FlowPanel fp = (FlowPanel) disclosurePanel.getContent();
          for (Widget value : fp) {
            SidebarItem sb = (SidebarItem) value;
            sb.setVisible(true);
          }
        }
      }
    } else {
      // show matching and their parents
      Set<DisclosurePanel> disclosurePanelsThatShouldBeVisible = new HashSet<>();

      for (Widget widget : sidebarGroup) {
        if (widget instanceof DisclosurePanel) {
          DisclosurePanel disclosurePanel = (DisclosurePanel) widget;
          disclosurePanel.setOpen(true);
          FlowPanel fp = (FlowPanel) disclosurePanel.getContent();

          for (Widget value : fp) {
            SidebarItem sb = (SidebarItem) value;

            if (sb.getText().toLowerCase().contains(searchValue.toLowerCase())) {
              sb.setVisible(true);
              disclosurePanelsThatShouldBeVisible.add(disclosurePanel);
            } else {
              sb.setVisible(false);
              disclosurePanel.setVisible(false);
            }
          }
        } else {
          widget.setVisible(true);
        }
      }

      for (DisclosurePanel disclosurePanel : disclosurePanelsThatShouldBeVisible) {
        disclosurePanel.setVisible(true);
      }
    }
  }

  private SidebarHyperlink buildSidebarHyperLink(final String displayName, final String route,
    final String fontAwesomeIcon) {
    SidebarHyperlink sidebarHyperlink = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(fontAwesomeIcon, displayName), route);
    sidebarHyperlink.setH6().setIndent3();

    return sidebarHyperlink;
  }
}
