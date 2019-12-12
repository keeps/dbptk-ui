package com.databasepreservation.common.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.models.structure.*;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DataTransformationSidebar extends Composite implements Sidebar {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DataTransformationSidebar> instances = new HashMap<>();

  interface DatabaseSidebarUiBinder extends UiBinder<Widget, DataTransformationSidebar> {
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
  private Map<String, SidebarHyperlink> list = new HashMap<>();

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param databaseUUID
   *          the database UUID
   * @return a DatabaseSidebar instance
   */
  public static DataTransformationSidebar getInstance(String databaseUUID) {
    if (databaseUUID == null) {
      return getEmptyInstance();
    }

    DataTransformationSidebar instance = instances.get(databaseUUID);
    if (instance == null || instance.database == null
      || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DataTransformationSidebar(databaseUUID);
      instances.put(databaseUUID, instance);
    } else {
      return new DataTransformationSidebar(instance);
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
  public static DataTransformationSidebar getInstance(ViewerDatabase database) {
    if (database == null) {
      return getEmptyInstance();
    }

    DataTransformationSidebar instance = instances.get(database.getUuid());
    if (instance == null || instance.database == null
      || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DataTransformationSidebar(database);
      instances.put(database.getUuid(), instance);
    } else {
      // workaround because the same DatabaseSidebar can not belong to multiple
      // widgets
      return new DataTransformationSidebar(instance);
    }
    return instance;
  }

  /**
   * Creates a new (dummy) DatabaseSidebar that is not visible. This method exists
   * so that pages can opt for not using a sidebar at all.
   *
   * @return a new invisible DatabaseSidebar
   */
  public static DataTransformationSidebar getEmptyInstance() {
    return new DataTransformationSidebar();
  }

  /**
   * Clone constructor, because the same DatabaseSidebar can not be child in more
   * than one widget
   *
   * @param other
   *          the DatabaseSidebar used in another widget
   */
  private DataTransformationSidebar(DataTransformationSidebar other) {
    initialized = other.initialized;
    initWidget(uiBinder.createAndBindUi(this));
    searchInputBox.setText(other.searchInputBox.getText());
    init(other.database);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DataTransformationSidebar(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database);
  }

  /**
   * Empty constructor, for pages that do not have a sidebar
   */
  private DataTransformationSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DataTransformationSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  public void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    SidebarHyperlink informationLink = new SidebarHyperlink(FontAwesomeIconManager
      .getTagSafeHtml(FontAwesomeIconManager.DATABASE_INFORMATION, messages.menusidebar_information()),
      HistoryManager.linkToDataTransformation(database.getUuid()));
    informationLink.setH5().setIndent0();
    list.put(database.getUuid(), informationLink);
    sidebarGroup.add(informationLink);

    /* Schemas */
    SidebarItem schemasHeader = createSidebarSubItemHeaderSafeHMTL("Tables", FontAwesomeIconManager.LIST);
    FlowPanel schemaItems = new FlowPanel();

    final int totalSchemas = metadata.getSchemas().size();

    String iconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR, "fa-sm");

    for (final ViewerSchema schema : metadata.getSchemas()) {
      schema.setViewsSchemaUUID();

      for (ViewerTable table : schema.getTables()) {
        if (!table.isCustomView() && !table.isMaterializedView()) {
          schemaItems.add(createTableItem(schema, table, totalSchemas, iconTag));
        }
      }
    }

    createSubItem(schemasHeader, schemaItems, false);

    searchInit();
    setVisible(true);
  }

  private SidebarHyperlink createTableItem(final ViewerSchema schema, final ViewerTable table, final int totalSchemas,
    final String iconTag) {
      SafeHtml html;
      if (totalSchemas == 1) {
        html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE, table.getName());
      } else {
        html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
            schema.getName() + " " + iconTag + " " + table.getName());
      }
      SidebarHyperlink tableLink = new SidebarHyperlink(html,
          HistoryManager.linkToDataTransformationTable(database.getUuid(), table.getUUID()));
      tableLink.setH6().setIndent2();
      list.put(table.getUUID(), tableLink);
      sidebarGroup.add(tableLink);

      return tableLink;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void init(ViewerDatabase db) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    if (ViewerDatabaseStatus.AVAILABLE.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUuid()))) {
        initialized = true;
        database = db;
        databaseUUID = db.getUuid();
        init();
      }
    }
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
}
