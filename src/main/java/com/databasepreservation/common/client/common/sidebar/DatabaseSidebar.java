package com.databasepreservation.common.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.configuration.observer.CollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseSidebar extends Composite implements Sidebar, CollectionStatusObserver {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  protected static Map<String, DatabaseSidebar> instances = new HashMap<>();

  interface DatabaseSidebarUiBinder extends UiBinder<Widget, DatabaseSidebar> {
  }

  protected static DatabaseSidebarUiBinder uiBinder = GWT.create(DatabaseSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  protected ViewerDatabase database;
  protected String databaseUUID;
  protected CollectionStatus collectionStatus;
  protected boolean initialized = false;
  protected Map<String, SidebarHyperlink> list = new HashMap<>();

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param databaseUUID
   *          the database UUID
   * @return a DatabaseSidebar instance
   */
  public static DatabaseSidebar getInstance(String databaseUUID) {

    if (databaseUUID == null) {
      return getEmptyInstance();
    }

    DatabaseSidebar instance = instances.get(databaseUUID);
    if (instance == null || instance.database == null
      || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DatabaseSidebar(databaseUUID);
      instances.put(databaseUUID, instance);
    } else {
      // workaround because the same DatabaseSidebar can not belong to multiple
      // widgets
      return new DatabaseSidebar(instance);
    }
    return instance;
  }

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param database
   *          the database
   * @return a DatabaseSidebar instance
   */
  public static DatabaseSidebar getInstance(ViewerDatabase database, CollectionStatus status) {
    if (database == null) {
      return getEmptyInstance();
    }

    DatabaseSidebar instance = instances.get(database.getUuid());
    if (instance == null || instance.database == null
      || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DatabaseSidebar(database, status);
      instances.put(database.getUuid(), instance);
    } else {
      // workaround because the same DatabaseSidebar can not belong to multiple
      // widgets
      return new DatabaseSidebar(instance);
    }
    return instance;
  }

  /**
   * Creates a new (dummy) DatabaseSidebar that is not visible. This method exists
   * so that pages can opt for not using a sidebar at all.
   *
   * @return a new invisible DatabaseSidebar
   */
  public static DatabaseSidebar getEmptyInstance() {
    return new DatabaseSidebar();
  }

  /**
   * Clone constructor, because the same DatabaseSidebar can not be child in more
   * than one widget
   *
   * @param other
   *          the DatabaseSidebar used in another widget
   */
  private DatabaseSidebar(DatabaseSidebar other) {
    initialized = other.initialized;
    initWidget(uiBinder.createAndBindUi(this));
    searchInputBox.setText(other.searchInputBox.getText());
    init(other.database, other.collectionStatus);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DatabaseSidebar(ViewerDatabase database, CollectionStatus status) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database, status);
  }

  /**
   * Empty constructor, for pages that do not have a sidebar
   */
  private DatabaseSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DatabaseSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  public void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    SidebarHyperlink informationLink = new SidebarHyperlink(FontAwesomeIconManager
      .getTagSafeHtml(FontAwesomeIconManager.DATABASE_INFORMATION, messages.menusidebar_information()),
      HistoryManager.linkToDatabase(database.getUuid()));
    informationLink.setH5().setIndent0();
    list.put(HistoryManager.ROUTE_DATABASE, informationLink);
    sidebarGroup.add(informationLink);

    SidebarHyperlink searchLink = new SidebarHyperlink(FontAwesomeIconManager
      .getTagSafeHtml(FontAwesomeIconManager.DATABASE_SEARCH, messages.menusidebar_searchAllRecords()),
      HistoryManager.linkToDatabaseSearch(database.getUuid()));
    searchLink.setH5().setIndent0();
    list.put(HistoryManager.ROUTE_DATABASE_SEARCH, searchLink);
    sidebarGroup.add(searchLink);

    SidebarHyperlink savedSearchesLink = new SidebarHyperlink(
      FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SAVED_SEARCH, messages.menusidebar_savedSearches()),
      HistoryManager.linkToSavedSearches(database.getUuid()));
    savedSearchesLink.setH5().setIndent0();
    list.put(HistoryManager.ROUTE_SAVED_SEARCHES, savedSearchesLink);
    sidebarGroup.add(savedSearchesLink);

    /* Schemas */
    SidebarItem schemasHeader = createSidebarSubItemHeaderSafeHMTL("Tables", FontAwesomeIconManager.LIST);
    FlowPanel schemaItems = new FlowPanel();

    final int totalSchemas = metadata.getSchemas().size();

    String iconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR, "fa-sm");

    for (final ViewerSchema schema : metadata.getSchemas()) {
      schema.setViewsSchemaUUID();

      for (ViewerTable table : schema.getTables()) {
        if (!table.isCustomView() && !table.isMaterializedView()) {
          if (collectionStatus.showTable(table.getUuid())) {
            schemaItems.add(createTableItem(schema, table, totalSchemas, iconTag));
          }
        }
      }

      for (ViewerView view : schema.getViews()) {
        schemaItems.add(createViewItem(schema, view, totalSchemas, iconTag));
      }
    }

      createSubItem(schemasHeader, schemaItems, true);

    /* Technical Information */
    SidebarItem technicalHeader = createSidebarSubItemHeaderSafeHMTL("Technical Information",
      FontAwesomeIconManager.TECHNICAL);
    FlowPanel technicalItems = new FlowPanel();

    SidebarHyperlink routinesLink = new SidebarHyperlink(
      FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA_ROUTINES, messages.menusidebar_routines()),
      HistoryManager.linkToSchemaRoutines(database.getUuid()));
    routinesLink.setH6().setIndent1();
    list.put(HistoryManager.ROUTE_SCHEMA_ROUTINES, routinesLink);
    sidebarGroup.add(routinesLink);
    technicalItems.add(routinesLink);

    SidebarHyperlink usersLink = new SidebarHyperlink(
      FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.DATABASE_USERS, messages.menusidebar_usersRoles()),
      HistoryManager.linkToDatabaseUsers(database.getUuid()));
    usersLink.setH6().setIndent1();
    list.put(HistoryManager.ROUTE_DATABASE_USERS, usersLink);
    sidebarGroup.add(usersLink);
    technicalItems.add(usersLink);

    SidebarHyperlink reportLink = new SidebarHyperlink(
      FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.DATABASE_REPORT, messages.titleReport(), true),
      HistoryManager.linkToDatabaseReport(database.getUuid()));
    reportLink.setH6().setIndent1();
    list.put(HistoryManager.ROUTE_DATABASE_REPORT, reportLink);
    sidebarGroup.add(reportLink);
    technicalItems.add(reportLink);

    createSubItem(technicalHeader, technicalItems, true);

    searchInit();
    setVisible(true);
  }

  private SidebarHyperlink createTableItem(final ViewerSchema schema, final ViewerTable table, final int totalSchemas,
    final String iconTag) {
      SafeHtml html;
      if (totalSchemas == 1) {
      html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        collectionStatus.getTableStatus(table.getUuid()).getCustomName());
      } else {
        html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        schema.getName() + " " + iconTag + " " + collectionStatus.getTableStatus(table.getUuid()).getCustomName());
      }
      SidebarHyperlink tableLink = new SidebarHyperlink(html,
          HistoryManager.linkToTable(database.getUuid(), table.getSchemaName(), table.getName()));
      tableLink.setH6().setIndent2();
      list.put(table.getId(), tableLink);
      sidebarGroup.add(tableLink);

      return tableLink;
  }

  private SidebarHyperlink createViewItem(final ViewerSchema schema, final ViewerView view, final int totalSchemas,
    final String iconTag) {
    SafeHtml html;
    SidebarHyperlink viewLink;
    final ViewerTable materializedTable = schema.getMaterializedTable(view.getName());
    if (materializedTable != null) {
      if (totalSchemas == 1) {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.TABLE, collectionStatus.getTableStatus(materializedTable.getUuid()).getCustomName());
      } else {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.TABLE, schema.getName() + " " + iconTag + " "
            + collectionStatus.getTableStatus(materializedTable.getUuid()).getCustomName());
      }
      viewLink = new SidebarHyperlink(html,
        HistoryManager.linkToTable(database.getUuid(), materializedTable.getSchemaName(), materializedTable.getName())).setTooltip("Materialized View");
      list.put(materializedTable.getUuid(), viewLink);
    } else if (schema.getCustomViewTable(view.getName()) != null) {
      final ViewerTable customViewTable = schema.getCustomViewTable(view.getName());
      if (totalSchemas == 1) {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.COG, collectionStatus.getTableStatus(customViewTable.getUuid()).getCustomName());
      } else {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.COG, schema.getName() + " " + iconTag + " "
            + collectionStatus.getTableStatus(customViewTable.getUuid()).getCustomName());
      }
      viewLink = new SidebarHyperlink(html, HistoryManager.linkToTable(database.getUuid(), customViewTable.getSchemaName(), customViewTable.getName())).setTooltip("Custom View");
      list.put(customViewTable.getUuid(), viewLink);
    } else {
      if (totalSchemas == 1) {
        html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS, view.getName());
      } else {
        html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          schema.getName() + " " + iconTag + " " + view.getName());
      }
      viewLink = new SidebarHyperlink(html, HistoryManager.linkToView(database.getUuid(), view.getUuid()));
      list.put(view.getUuid(), viewLink);
    }

    viewLink.setH6().setIndent2();

    sidebarGroup.add(viewLink);

    return viewLink;
  }

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
    instances.clear();
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void init(ViewerDatabase db, CollectionStatus status) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    if (ViewerDatabaseStatus.AVAILABLE.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUuid()))) {
        initialized = true;
        ObserverManager.getCollectionObserver().addObserver(this);
        database = db;
        collectionStatus = status;
        databaseUUID = db.getUuid();
        init();
      }
    }
  }

  @Override
  public void reset(ViewerDatabase database, CollectionStatus status) {

  }

  private SidebarItem createSidebarSubItemHeaderSafeHMTL(String headerText, String headerIcon) {
    return new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(headerIcon, headerText)).setH5().setIndent0();
  }

  private void createSubItem(SidebarItem header, FlowPanel content, boolean collapsed) {
    DisclosurePanel panel = new DisclosurePanel();
    panel.setOpen(!collapsed);
    panel.setAnimationEnabled(true);
    panel.setHeader(header);
    panel.setContent(content);
    panel.getElement().addClassName("sidebar-collapse");
    sidebarGroup.add(panel);
  }

  @Override
  public void select(String value) {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      if (entry.getKey().equals(value)) {
        list.get(value).setSelected(true);
      } else {
        list.get(entry.getKey()).setSelected(false);
      }
    }
  }

  @Override
  public void selectFirst() {
    // do nothing
  }

  @Override
  public void updateSidebarItem(String key, boolean value) {
    // do nothing
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
      showAll();
    } else {
      showMatching(searchValue);
    }
  }

  private void showAll() {
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
  }

  private void showMatching(final String searchValue) {
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

  @Override
  protected void onAttach() {
    super.onAttach();
    JavascriptUtils.stickSidebar();
  }
}
