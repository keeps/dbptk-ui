/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DataTransformationSidebar extends Composite implements Sidebar, ICollectionStatusObserver {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DataTransformationSidebar> instances = new HashMap<>();

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    instances.clear();
    this.collectionStatus = collectionStatus;
  }

  interface DatabaseSidebarUiBinder extends UiBinder<Widget, DataTransformationSidebar> {
  }

  private static DatabaseSidebarUiBinder uiBinder = GWT.create(DatabaseSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  FlowPanel controller;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  private ViewerDatabase database;
  private String databaseUUID;
  private CollectionStatus collectionStatus;
  private boolean initialized = false;
  private Map<String, SidebarHyperlink> list = new HashMap<>();
  private String firstElement = null;

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
  public static DataTransformationSidebar getInstance(ViewerDatabase database, CollectionStatus status) {
    if (database == null) {
      return getEmptyInstance();
    }

    DataTransformationSidebar instance = instances.get(database.getUuid());
    if (instance == null || instance.database == null
      || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DataTransformationSidebar(database, status);
      instances.put(database.getUuid(), instance);
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
    init(other.database, other.collectionStatus);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DataTransformationSidebar(ViewerDatabase database, CollectionStatus status) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database, status);
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
    list.put(databaseUUID, informationLink);
    sidebarGroup.add(informationLink);

    /* Schemas */
    final int totalSchemas = metadata.getSchemas().size();

    String iconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR, "fa-sm");

    for (final ViewerSchema schema : metadata.getSchemas()) {
      schema.setViewsSchemaUUID();

      for (ViewerTable table : schema.getTables()) {
        if (firstElement == null) {
          firstElement = table.getId();
        }
        if (!table.isCustomView() && !table.isMaterializedView()) {
          if (collectionStatus.showTable(table.getUuid())) {
            sidebarGroup.add(createTableItem(schema, table, totalSchemas, iconTag));
          }
        }
      }
    }

    searchInit();
    setVisible(true);
  }

  private SidebarHyperlink createTableItem(final ViewerSchema schema, final ViewerTable table, final int totalSchemas,
    final String iconTag) {
    SafeHtml html;
    String customName = collectionStatus.getTableStatus(table.getUuid()).getCustomName();
    if (totalSchemas == 1) {
      html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE, customName);
    } else {
      html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        schema.getName() + " " + iconTag + " " + customName);
    }
    SidebarHyperlink tableLink = new SidebarHyperlink(html,
      HistoryManager.linkToDataTransformationTable(database.getUuid(), table.getId()));
    tableLink.setH6().setIndent0();
    list.put(table.getId(), tableLink);

    return tableLink;
  }

  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void init(ViewerDatabase db, CollectionStatus status) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    if (ViewerDatabaseStatus.AVAILABLE.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUuid()))) {
        initialized = true;
        database = db;
        ObserverManager.getCollectionObserver().addObserver(this);
        collectionStatus = status;
        databaseUUID = db.getUuid();
        init();
      }
    }
  }

  @Override
  public void reset(ViewerDatabase database, CollectionStatus status) {

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
      if (widget instanceof SidebarItem) {
        SidebarItem sb = (SidebarItem) widget;
        sb.setVisible(true);
      }
    }
  }

  private void showMatching(final String searchValue) {
    // show matching and their parents
    Set<Widget> disclosurePanelsThatShouldBeVisible = new HashSet<>();

    for (Widget widget : sidebarGroup) {
      if (widget instanceof SidebarHyperlink) {
        SidebarItem sb = (SidebarItem) widget;
        if (sb.getText().toLowerCase().contains(searchValue.toLowerCase())) {
          sb.setVisible(true);
          disclosurePanelsThatShouldBeVisible.add(widget);
        } else {
          sb.setVisible(false);
          widget.setVisible(false);
        }
      } else {
        widget.setVisible(true);
      }
    }

    for (Widget disclosurePanel : disclosurePanelsThatShouldBeVisible) {
      disclosurePanel.setVisible(true);
    }
  }

  public void updateControllerPanel(List<Button> buttonList) {
    controller.clear();
    if (buttonList != null) {
      for (Button button : buttonList) {
        controller.add(button);
      }
    }
  }

  public String getFirstElement() {
    return firstElement;
  }

  public static void clear(String databaseUUID) {
    instances.remove(databaseUUID);
  }
}
