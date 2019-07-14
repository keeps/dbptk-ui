package com.databasepreservation.main.desktop.client.common.sidebar;

import java.util.*;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataEditSidebar extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataEditSidebar> instances = new HashMap<>();
  private static Map<String, SidebarHyperlink> list = new HashMap<>();

  public static MetadataEditSidebar getInstance(String databaseUUID) {
    if (databaseUUID == null) {
      return getEmptyInstance();
    }

    MetadataEditSidebar instance = instances.get(databaseUUID);
    if (instance == null || instance.database == null
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new MetadataEditSidebar(databaseUUID);
      instances.put(databaseUUID, instance);
    } else {
      return new MetadataEditSidebar(instance);
    }
    return instance;
  }

  public static MetadataEditSidebar getInstance(ViewerDatabase database) {
    if (database == null) {
      return getEmptyInstance();
    }

    MetadataEditSidebar instance = instances.get(database.getUUID());
    if (instance == null || instance.database == null
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new MetadataEditSidebar(database);
      instances.put(database.getUUID(), instance);
    } else {
      // workaround because the same MetadataEditSidebar can not belong to multiple
      // widgets
      return new MetadataEditSidebar(instance);
    }
    return instance;
  }

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

  private MetadataEditSidebar(MetadataEditSidebar other) {
    initialized = other.initialized;
    initWidget(uiBinder.createAndBindUi(this));
    searchInputBox.setText(other.searchInputBox.getText());
    init(other.database);
  }

  private MetadataEditSidebar(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database);
  }

  private MetadataEditSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  private MetadataEditSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  public void init(ViewerDatabase db) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    GWT.log("started");
    if (ViewerDatabase.Status.AVAILABLE.equals(db.getStatus())
      || ViewerDatabase.Status.METADATA_ONLY.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUUID()))) {
        GWT.log("initialize");
        initialized = true;
        database = db;
        databaseUUID = db.getUUID();
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

    SidebarHyperlink databaseLink = new SidebarHyperlink(messages.menusidebar_database(),
      HistoryManager.linkToDatabaseMetadata(database.getUUID()));
    databaseLink.addIcon(FontAwesomeIconManager.DATABASE).setH5().setIndent0();
    list.put(database.getUUID(), databaseLink);
    sidebarGroup.add(databaseLink);

    SidebarHyperlink usersLink = new SidebarHyperlink(messages.menusidebar_usersRoles(),
      HistoryManager.linkToDatabaseMetadataUsers(database.getUUID()));
    usersLink.addIcon(FontAwesomeIconManager.DATABASE_USERS).setH5().setIndent0();
    list.put(HistoryManager.ROUTE_DATABASE_USERS, usersLink);
    sidebarGroup.add(usersLink);

    for (final ViewerSchema schema : metadata.getSchemas()) {
      sidebarGroup.add(new SidebarItem(schema.getName()).addIcon(FontAwesomeIconManager.SCHEMA).setH5().setIndent0());

      SidebarItem routineHeader = createSidebarSubItemHeader(messages.menusidebar_routines(), FontAwesomeIconManager.SCHEMA_ROUTINES);
      FlowPanel routineItems = new FlowPanel();
      for (ViewerRoutine routine : schema.getRoutines()) {

        SidebarHyperlink routineLink = new SidebarHyperlink(routine.getName(),
          HistoryManager.linkToRoutine(database.getUUID(), schema.getUUID(), routine.getUUID()));
        routineLink.addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2();
        list.put(routine.getUUID(), routineLink);
        routineItems.add(routineLink);
      }

      createSubItem(routineHeader,routineItems);

      SidebarItem viewsHeader = createSidebarSubItemHeader(messages.menusidebar_views(), FontAwesomeIconManager.SCHEMA_VIEWS);
      FlowPanel viewsItems = new FlowPanel();
      for (ViewerView view : schema.getViews()) {

        SidebarHyperlink viewLink = new SidebarHyperlink(view.getName(),
          HistoryManager.linkToView(database.getUUID(), schema.getUUID(), view.getUUID()));
        viewLink.addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2();
        list.put(view.getUUID(), viewLink);
        sidebarGroup.add(viewLink);
        viewsItems.add(viewLink);
      }

      createSubItem(viewsHeader,viewsItems);

      SidebarItem tableHeader = createSidebarSubItemHeader(messages.menusidebar_data(), FontAwesomeIconManager.SCHEMA_DATA);
      FlowPanel tableItems = new FlowPanel();
      for (ViewerTable table : schema.getTables()) {

        SidebarHyperlink tableLink = new SidebarHyperlink(table.getName(),
          HistoryManager.linkToTable(database.getUUID(), table.getUUID()));
        tableLink.addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2();
        list.put(table.getUUID(), tableLink);
        sidebarGroup.add(tableLink);
        tableItems.add(tableLink);

      }
      createSubItem(tableHeader,tableItems);

      searchInit();
    }

    setVisible(true);
  }

  private SidebarItem createSidebarSubItemHeader(String headerText, String headerIcon) {
    return new SidebarItem(headerText).addIcon(headerIcon).setH6().setIndent1();
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
    GWT.log("SELECTED:::" + value);
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

    searchInputBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        doSearch();
      }
    });

    searchInputBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });
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
          Iterator<Widget> iterator = fp.iterator();
          while (iterator.hasNext()) {
            SidebarItem sb = (SidebarItem) iterator.next();
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

          Iterator<Widget> iterator = fp.iterator();

          while (iterator.hasNext()){
            SidebarItem sb = (SidebarItem) iterator.next();

            GWT.log(sb.getText());

            if(sb.getText().toLowerCase().contains(searchValue.toLowerCase())) {
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
