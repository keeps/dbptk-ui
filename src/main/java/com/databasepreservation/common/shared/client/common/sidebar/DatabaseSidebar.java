package com.databasepreservation.common.shared.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.ViewerStructure.ViewerView;
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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
public class DatabaseSidebar extends Composite {
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
  protected boolean initialized = false;
  protected static Map<String, SidebarHyperlink> list = new HashMap<>();

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
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
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
  public static DatabaseSidebar getInstance(ViewerDatabase database) {
    if (database == null) {
      return getEmptyInstance();
    }

    DatabaseSidebar instance = instances.get(database.getUUID());
    if (instance == null || instance.database == null
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DatabaseSidebar(database);
      instances.put(database.getUUID(), instance);
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
    init(other.database);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DatabaseSidebar(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database);
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

    SidebarHyperlink informationLink = new SidebarHyperlink(messages.menusidebar_information(),
      HistoryManager.linkToDatabase(database.getUUID()));
    informationLink.addIcon(FontAwesomeIconManager.DATABASE_INFORMATION).setH5().setIndent0();
    list.put(HistoryManager.ROUTE_DATABASE, informationLink);
    sidebarGroup.add(informationLink);

    SidebarHyperlink searchLink = new SidebarHyperlink(messages.menusidebar_searchAllRecords(),
      HistoryManager.linkToDatabaseSearch(database.getUUID()));
    searchLink.addIcon(FontAwesomeIconManager.DATABASE_SEARCH).setH5().setIndent0();
    list.put(HistoryManager.ROUTE_DATABASE_SEARCH, searchLink);
    sidebarGroup.add(searchLink);

    SidebarHyperlink savedSearchesLink = new SidebarHyperlink(messages.menusidebar_savedSearches(),
      HistoryManager.linkToSavedSearches(database.getUUID()));
    savedSearchesLink.addIcon(FontAwesomeIconManager.SAVED_SEARCH).setH5().setIndent0();
    list.put(HistoryManager.ROUTE_SAVED_SEARCHES, savedSearchesLink);
    sidebarGroup.add(savedSearchesLink);

    /* Schemas */
    SidebarItem schemasHeader = createSidebarSubItemHeader("Tables", FontAwesomeIconManager.LIST);
    FlowPanel schemaItems = new FlowPanel();

    final int totalSchemas = metadata.getSchemas().size();

    for (final ViewerSchema schema : metadata.getSchemas()) {
      schema.setViewsSchemaUUID();

      for (ViewerTable table : schema.getTables()) {
        if (!table.getName().startsWith(ViewerConstants.MATERIALIZED_VIEW_PREFIX)) {
          String iconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR,
            "font-size-small");
          SafeHtml html;
          if (totalSchemas == 1) {
            html = SafeHtmlUtils.fromSafeConstant(table.getName());
          } else {
            html = SafeHtmlUtils.fromSafeConstant(schema.getName() + " " + iconTag + " " + table.getName());
          }

          SidebarHyperlink tableLink = new SidebarHyperlink(html,
            HistoryManager.linkToTable(database.getUUID(), table.getUUID()));
          tableLink.addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2();
          list.put(table.getUUID(), tableLink);
          sidebarGroup.add(tableLink);
          schemaItems.add(tableLink);
        }
      }

      for (ViewerView view : schema.getViews()) {
        String iconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR,
          "font-size-small");
        SafeHtml html;
        if (totalSchemas == 1) {
          html = SafeHtmlUtils.fromSafeConstant(view.getName());
        } else {
          html = SafeHtmlUtils.fromSafeConstant(schema.getName() + " " + iconTag + " " + view.getName());
        }

        SidebarHyperlink viewLink;
        final ViewerTable materializedTable = schema.getMaterializedTable(view.getName());
        if (materializedTable != null) {
          viewLink = new SidebarHyperlink(html,
            HistoryManager.linkToTable(database.getUUID(), materializedTable.getUUID()));
        } else {
          viewLink = new SidebarHyperlink(html, HistoryManager.linkToView(database.getUUID(), view.getUUID()));
        }

        viewLink.addIcon(FontAwesomeIconManager.SCHEMA_VIEWS).setH6().setIndent2();
        list.put(view.getUUID(), viewLink);

        sidebarGroup.add(viewLink);
        schemaItems.add(viewLink);
      }
    }

      createSubItem(schemasHeader, schemaItems, true);

    /* Technical Information */
    SidebarItem technicalHeader = createSidebarSubItemHeader("Technical Information", FontAwesomeIconManager.TECHNICAL);
    FlowPanel technicalItems = new FlowPanel();

    SidebarHyperlink routinesLink = new SidebarHyperlink(messages.menusidebar_routines(),
      HistoryManager.linkToSchemaRoutines(database.getUUID()));
    routinesLink.addIcon(FontAwesomeIconManager.SCHEMA_ROUTINES).setH6().setIndent1();
    list.put(HistoryManager.ROUTE_SCHEMA_ROUTINES, routinesLink);
    sidebarGroup.add(routinesLink);
    technicalItems.add(routinesLink);

    SidebarHyperlink usersLink = new SidebarHyperlink(messages.menusidebar_usersRoles(),
      HistoryManager.linkToDatabaseUsers(database.getUUID()));
    usersLink.addIcon(FontAwesomeIconManager.DATABASE_USERS).setH6().setIndent1();
    list.put(HistoryManager.ROUTE_DATABASE_USERS, usersLink);
    sidebarGroup.add(usersLink);
    technicalItems.add(usersLink);

    SidebarHyperlink reportLink = new SidebarHyperlink(messages.titleReport(),
      HistoryManager.linkToDatabaseReport(database.getUUID()));
    reportLink.addIcon(FontAwesomeIconManager.DATABASE_REPORT).setH6().setIndent1();
    list.put(HistoryManager.ROUTE_DATABASE_REPORT, reportLink);
    sidebarGroup.add(reportLink);
    technicalItems.add(reportLink);

    createSubItem(technicalHeader, technicalItems, true);

    searchInit();
    setVisible(true);
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void init(ViewerDatabase db) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    if (ViewerDatabase.Status.AVAILABLE.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUUID()))) {
        initialized = true;
        database = db;
        databaseUUID = db.getUUID();
        init();
      }
    }
  }

  private SidebarItem createSidebarSubItemHeader(String headerText, String headerIcon) {
    return new SidebarItem(headerText).addIcon(headerIcon).setH5().setIndent0();
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
