package com.databasepreservation.common.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
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
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ColumnsManagementSidebar extends Composite implements Sidebar, ICollectionStatusObserver {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  protected static Map<String, ColumnsManagementSidebar> instances = new HashMap<>();

  interface ColumnsManagementSidebarUiBinder extends UiBinder<Widget, ColumnsManagementSidebar> {
  }

  protected static ColumnsManagementSidebarUiBinder uiBinder = GWT.create(ColumnsManagementSidebarUiBinder.class);

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
  private CollectionStatus collectionStatus;
  private boolean initialized = false;
  private Map<String, SidebarHyperlink> list = new LinkedHashMap<>();

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param databaseUUID
   *          the database UUID
   * @return a DatabaseSidebar instance
   */
  public static ColumnsManagementSidebar getInstance(String databaseUUID) {
    return instances.computeIfAbsent(databaseUUID, k -> new ColumnsManagementSidebar(databaseUUID));
  }

  private ColumnsManagementSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private ColumnsManagementSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  public void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    final int totalSchemas = metadata.getSchemas().size();

    String iconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR, "fa-sm");

    for (final ViewerSchema schema : metadata.getSchemas()) {
      schema.setViewsSchemaUUID();

      for (ViewerTable table : schema.getTables()) {
        if (!table.isMaterializedView()) {
          if (collectionStatus.showTable(table.getUuid())) {
            sidebarGroup.add(createTableItem(schema, table, totalSchemas, iconTag));
          }
        }
      }

      for (ViewerView view : schema.getViews()) {
        if (!view.getColumns().isEmpty()) {
          if (schema.getCustomViewTable(view.getName()) == null) {
            final SidebarHyperlink viewItem = createViewItem(schema, view, totalSchemas, iconTag);
            if (viewItem != null) {
              sidebarGroup.add(viewItem);
            }
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
    if (totalSchemas == 1) {
      html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        collectionStatus.getTableStatusByTableId(table.getId()).getCustomName());
    } else {
      html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE, schema.getName() + " " + iconTag + " "
        + collectionStatus.getTableStatusByTableId(table.getId()).getCustomName());
    }
    SidebarHyperlink tableLink = new SidebarHyperlink(html,
      HistoryManager.linkToColumnManagement(database.getUuid(), table.getId()));
    tableLink.setH6().setIndent0();
    list.put(table.getId(), tableLink);

    return tableLink;
  }

  private SidebarHyperlink createViewItem(final ViewerSchema schema, final ViewerView view, final int totalSchemas,
    final String iconTag) {
    SafeHtml html;
    SidebarHyperlink viewLink = null;
    final ViewerTable materializedTable = schema.getMaterializedTable(view.getName());
    if (materializedTable != null) {
      if (totalSchemas == 1) {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.TABLE,
          collectionStatus.getTableStatusByTableId(materializedTable.getId()).getCustomName());
      } else {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.TABLE, schema.getName() + " " + iconTag + " "
            + collectionStatus.getTableStatusByTableId(materializedTable.getId()).getCustomName());
      }
      viewLink = new SidebarHyperlink(html,
        HistoryManager.linkToColumnManagement(database.getUuid(), materializedTable.getId()))
          .setTooltip("Materialized View");
      list.put(materializedTable.getId(), viewLink);
    } else if (schema.getCustomViewTable(view.getName()) != null) {
      final ViewerTable customViewTable = schema.getCustomViewTable(view.getName());
      if (totalSchemas == 1) {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.COG,
          collectionStatus.getTableStatusByTableId(customViewTable.getId()).getCustomName());
      } else {
        html = FontAwesomeIconManager.getStackedIconSafeHtml(FontAwesomeIconManager.SCHEMA_VIEWS,
          FontAwesomeIconManager.COG, schema.getName() + " " + iconTag + " "
            + collectionStatus.getTableStatusByTableId(customViewTable.getId()).getCustomName());
      }
      viewLink = new SidebarHyperlink(html,
        HistoryManager.linkToColumnManagement(database.getUuid(), customViewTable.getId())).setTooltip("Custom View");
      list.put(customViewTable.getId(), viewLink);
    }
    if (viewLink != null) {
      viewLink.setH6().setIndent0();
    }

    return viewLink;
  }

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void init(ViewerDatabase db, CollectionStatus status) {
    if (!isInitialized()) {
      initialized = true;
      ObserverManager.getCollectionObserver().addObserver(this);
      database = db;
      collectionStatus = status;
      databaseUUID = db.getUuid();
      init();
    }
  }

  @Override
  public void reset(ViewerDatabase database, CollectionStatus collectionStatus) {
    sidebarGroup.clear();
    initialized = false;
    init(database, collectionStatus);
  }

  @Override
  public void select(String value) {
    if (value.equals(databaseUUID)) {
      selectFirst();
    } else {
      for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
        entry.getValue().setSelected(entry.getKey().equals(value));
      }
    }
  }

  @Override
  public void selectFirst() {
    final String uuid = collectionStatus.getFirstTableVisible();
    list.forEach((key, hyperlink) -> hyperlink.setSelected(key.equals(uuid)));
  }

  @Override
  public void updateSidebarItem(String key, boolean value) {
    final SidebarHyperlink sidebarHyperlink = list.get(key);
    if (value) {
      sidebarHyperlink.setChanged(FontAwesomeIconManager.ACTION_EDIT);
    } else {
      sidebarHyperlink.setChanged(FontAwesomeIconManager.TABLE);
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

  @Override
  protected void onAttach() {
    super.onAttach();
    // JavascriptUtils.stickSidebar();
  }
}
