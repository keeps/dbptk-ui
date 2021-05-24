/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.sidebar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRoutine;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerView;
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
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataEditSidebar extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataEditSidebar> instances = new HashMap<>();
  private static Map<String, SidebarHyperlink> list = new HashMap<>();


  /**
   * Creates a new MetadataEditSidebar
   *
   * @param databaseUUID
   *          the database
   * @return a MetadataEditSidebar instance
   */
  public static MetadataEditSidebar getInstance(String databaseUUID) {
    if (databaseUUID == null) {
      return getEmptyInstance();
    }

    MetadataEditSidebar instance = instances.get(databaseUUID);
    if (instance == null || instance.database == null || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new MetadataEditSidebar(databaseUUID);
      instances.put(databaseUUID, instance);
    } else {
      return new MetadataEditSidebar(instance);
    }
    return instance;
  }

  /**
   * Creates a new (dummy) MetadataEditSidebar that is not visible. This method exists
   * so that pages can opt for not using a sidebar at all.
   *
   * @return a new invisible MetadataEditSidebar
   */
  public static MetadataEditSidebar getEmptyInstance() {
    return new MetadataEditSidebar();
  }

  interface DatabaseSidebarUiBinder extends UiBinder<Widget, MetadataEditSidebar> {
  }

  private static DatabaseSidebarUiBinder uiBinder = GWT.create(DatabaseSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  private ViewerDatabase database;
  private String databaseUUID;
  private boolean initialized = false;

  /**
   * Clone constructor, because the same MetadataEditSidebar can not be child in more
   * than one widget
   *
   * @param other
   *          the MetadataEditSidebar used in another widget
   */
  private MetadataEditSidebar(MetadataEditSidebar other) {
    initialized = other.initialized;
    initWidget(uiBinder.createAndBindUi(this));
    searchInputBox.setText(other.searchInputBox.getText());
    init(other.database);
  }

  /**
   * Empty constructor, for pages that do not have a sidebar
   */
  private MetadataEditSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  /**
   * Use MetadataEditSidebar.getInstance to obtain an instance
   */
  private MetadataEditSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  public void init(ViewerDatabase db) {
    if (ViewerDatabaseStatus.AVAILABLE.equals(db.getStatus())
      || ViewerDatabaseStatus.METADATA_ONLY.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUuid()))) {
        GWT.log("initialize");
        initialized = true;
        database = db;
        databaseUUID = db.getUuid();
        init();
      }
    }
  }

  public boolean isInitialized() {
    return initialized;
  }

  private void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    /* Information */
    SidebarHyperlink databaseLink = new SidebarHyperlink(messages.menusidebar_database(),
      HistoryManager.linkToDatabaseMetadata(database.getUuid()));
    databaseLink.setH5().setIndent0();
    databaseLink.setTextBySafeHTML(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.DATABASE, messages.menusidebar_database()));
    list.put(database.getUuid(), databaseLink);
    sidebarGroup.add(databaseLink);

    /* Users */
    SidebarHyperlink usersLink = new SidebarHyperlink(messages.menusidebar_usersRoles(),
      HistoryManager.linkToDatabaseMetadataUsers(database.getUuid()));
    usersLink.setH5().setIndent0();
    usersLink.setTextBySafeHTML(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.DATABASE_USERS, messages.menusidebar_usersRoles()));
    list.put(HistoryManager.ROUTE_DATABASE_USERS, usersLink);
    sidebarGroup.add(usersLink);

    for (final ViewerSchema schema : metadata.getSchemas()) {

      /* Schemas */
      sidebarGroup.add(new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA, schema.getName())).setH5().setIndent0());

      /* Tables */
      SidebarItem tableHeader = createSidebarSubItemHeaderSafeHMTL(messages.sidebarMenuTextForTables(),
        FontAwesomeIconManager.LIST);
      FlowPanel tableItems = new FlowPanel();
      for (ViewerTable table : schema.getTables()) {

        SidebarHyperlink tableLink = new SidebarHyperlink(table.getName(),
          HistoryManager.linkToDesktopMetadataTable(database.getUuid(), table.getUuid()));
        tableLink.setH6().setIndent2();
        tableLink.setTextBySafeHTML(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE, table.getName()));
        list.put(table.getUuid(), tableLink);
        sidebarGroup.add(tableLink);
        tableItems.add(tableLink);

      }
      createSubItem(tableHeader, tableItems);

      /* Views */
      SidebarItem viewsHeader = createSidebarSubItemHeaderSafeHMTL(messages.menusidebar_views(),
        FontAwesomeIconManager.SCHEMA_VIEWS);
      FlowPanel viewsItems = new FlowPanel();
      for (ViewerView view : schema.getViews()) {

        SidebarHyperlink viewLink = new SidebarHyperlink(view.getName(),
          HistoryManager.linTokDesktopMetadataView(database.getUuid(), schema.getUuid(), view.getUuid()));
        viewLink.setH6().setIndent2();
        viewLink.setTextBySafeHTML(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS, view.getName()));
        list.put(view.getUuid(), viewLink);
        sidebarGroup.add(viewLink);
        viewsItems.add(viewLink);
      }
      createSubItem(viewsHeader, viewsItems);

      /* Routines */
      SidebarItem routineHeader = createSidebarSubItemHeaderSafeHMTL(messages.menusidebar_routines(),
        FontAwesomeIconManager.SCHEMA_ROUTINES);
      FlowPanel routineItems = new FlowPanel();
      for (ViewerRoutine routine : schema.getRoutines()) {

        SidebarHyperlink routineLink = new SidebarHyperlink(routine.getName(),
          HistoryManager.linkToDesktopMetadataRoutine(database.getUuid(), schema.getUuid(), routine.getUuid()));
        routineLink.setH6().setIndent2();
        routineLink.setTextBySafeHTML(FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA_ROUTINES, routine.getName()));
        list.put(routine.getUuid(), routineLink);
        routineItems.add(routineLink);
      }

      createSubItem(routineHeader, routineItems);

      searchInit();
    }

    setVisible(true);
  }

  private SidebarItem createSidebarSubItemHeader(String headerText, String headerIcon) {
    return new SidebarItem(headerText).addIcon(headerIcon).setH6().setIndent1();
  }

  private SidebarItem createSidebarSubItemHeaderSafeHMTL(String headerText, String headerIcon) {
    return new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(headerIcon, headerText)).setH6().setIndent1();
  }

  private void createSubItem(SidebarItem header, FlowPanel content) {
    DisclosurePanel panel = new DisclosurePanel();
    panel.setAnimationEnabled(true);
    panel.setHeader(header);
    panel.setContent(content);
    panel.getElement().addClassName("sidebar-collapse");
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
          disclosurePanel.setOpen(false);
          FlowPanel fp = (FlowPanel) disclosurePanel.getContent();
          for (Widget value : fp) {
            SidebarItem sb = (SidebarItem) value;
            sb.setVisible(true);
          }
        }
      }
    } else {
      // show matching and their parents
      Set<SidebarItem> parentsThatShouldBeVisible = new HashSet<>();
      List<SidebarItem> parentsList = new ArrayList<>();

      Set<DisclosurePanel> disclosurePanelsThatShouldBeVisible = new HashSet<>();

      for (Widget widget : sidebarGroup) {
        if (widget instanceof SidebarItem) {
          SidebarItem sidebarItem = (SidebarItem) widget;

          int indent = sidebarItem.getIndent();
          if (indent >= 0) {
            parentsList.add(indent, sidebarItem);
          }

          if (sidebarItem.getText().toLowerCase().contains(searchValue.toLowerCase())) {
            widget.setVisible(true);
            for (int i = 0; i < indent; i++) {
              parentsThatShouldBeVisible.add(parentsList.get(i));
            }
          } else {
            widget.setVisible(false);
          }
        } else if (widget instanceof DisclosurePanel) {
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

      for (SidebarItem sidebarItem : parentsThatShouldBeVisible) {
        sidebarItem.setVisible(true);
      }

      for (DisclosurePanel disclosurePanel : disclosurePanelsThatShouldBeVisible) {
        disclosurePanel.setVisible(true);
      }
    }
  }
}
