package com.databasepreservation.desktop.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.common.shared.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.common.shared.client.common.sidebar.SidebarItem;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.common.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
public class TableAndColumnsSendToSidebar extends Composite {
  public static final String DATABASE_LINK = "database";
  private static final String TABLES_LINK = "tables";
  private static final String TABLE_LINK = "table";
  private static final String VIEW_LINK = "view";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SidebarHyperlink> list = new HashMap<>();

  interface TableAndColumnsSendToSidebarUiBinder extends UiBinder<Widget, TableAndColumnsSendToSidebar> {
  }

  private static TableAndColumnsSendToSidebarUiBinder uiBinder = GWT.create(TableAndColumnsSendToSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  public static TableAndColumnsSendToSidebar newInstance(String databaseUUID, ViewerMetadata viewerMetadata) {
    return new TableAndColumnsSendToSidebar(databaseUUID, viewerMetadata);
  }

  private TableAndColumnsSendToSidebar(String databaseUUID, ViewerMetadata viewerMetadata) {
    initWidget(uiBinder.createAndBindUi(this));
    init(viewerMetadata, databaseUUID);
  }

  private void init(ViewerMetadata viewerMetadata, String databaseUUID) {
    SidebarHyperlink database = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.DATABASE, messages.menusidebar_database()),
      HistoryManager.linkToSendToWizardTableAndColumnsShowERDiagram(DATABASE_LINK, databaseUUID));
    database.setH5().setIndent0();
    list.put(DATABASE_LINK, database);
    sidebarGroup.add(database);

    for (ViewerSchema schema : viewerMetadata.getSchemas()) {

      SidebarHyperlink schemas = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA, schema.getName()),
          HistoryManager.linkToSendToWizardTableAndColumnsShowTables(TABLES_LINK, databaseUUID, schema.getUUID()));
      schemas.setH5().setIndent1();
      list.put(TABLES_LINK, schemas);
      sidebarGroup.add(schemas);

      SidebarItem tables = new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.LIST, messages.sidebarMenuTextForTables()));
      tables.setH5().setIndent2();

      FlowPanel tablesItems = new FlowPanel();
      for (ViewerTable table : schema.getTables()) {
          SidebarHyperlink sidebarHyperlink = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE, table.getName()),
              HistoryManager.linkToSendToWizardTableAndColumnsShowColumns(TABLE_LINK, databaseUUID, schema.getUUID(), table.getUUID()));
          sidebarHyperlink.setH6().setIndent3();
          list.put(table.getName(), sidebarHyperlink);
          tablesItems.add(sidebarHyperlink);
      }
      createSubItem(tables, tablesItems);

      SidebarItem views = new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.LIST, messages.sidebarMenuTextForViews()));
      views.setH5().setIndent2();

      FlowPanel viewsItems = new FlowPanel();
      for (ViewerView view : schema.getViews()) {
        SidebarHyperlink sidebarHyperlink = new SidebarHyperlink(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS, view.getName()),
            HistoryManager.linkToSendToWizardTableAndColumnsShowViews(VIEW_LINK, databaseUUID, schema.getUUID(), view.getUUID()));
        sidebarHyperlink.setH6().setIndent3();
        list.put(view.getName(), sidebarHyperlink);
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
      if (entry.getKey().equals(value)) {
        list.get(value).setSelected(true);
      } else {
        list.get(entry.getKey()).setSelected(false);
      }
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

            GWT.log(sb.getText());

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
}
