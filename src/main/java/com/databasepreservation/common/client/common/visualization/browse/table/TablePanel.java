package com.databasepreservation.common.client.common.visualization.browse.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.common.search.TableSearchPanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.visualization.browse.foreignKey.ForeignKeyPanelOptions;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.configuration.observer.CollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TablePanel extends RightPanel implements CollectionStatusObserver {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, TablePanel> instances = new HashMap<>();
  private static final String SEPARATOR = "/";

  public static TablePanel getInstance(CollectionStatus status, ViewerDatabase database, String tableId, String route) {
    return getInstance(status, database, tableId, null, route);
  }

  public static TablePanel getInstance(CollectionStatus status, ViewerDatabase database, String tableId,
    String searchInfoJson, String route) {

    String code = database.getUuid() + SEPARATOR + tableId;
    TablePanel instance = instances.get(code);
    if (instance == null) {
      instance = new TablePanel(status, database, tableId, searchInfoJson, route);
      instances.put(code, instance);
    } else if (searchInfoJson != null) {
      instance.applySearchInfoJson(searchInfoJson);
    } else if (instance.tableSearchPanel.isSearchInfoDefined()) {
      instance = new TablePanel(status, database, tableId, route);
      instances.put(code, instance);
    }

    return instance;
  }

  public static TablePanel createInstance(CollectionStatus status, ViewerDatabase database, ViewerTable table,
    SearchInfo searchInfo, String route) {
    return new TablePanel(status, database, table, searchInfo, route);
  }

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    instances.clear();
  }

  interface TablePanelUiBinder extends UiBinder<Widget, TablePanel> {
  }

  private static TablePanelUiBinder uiBinder = GWT.create(TablePanelUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField(provided = true)
  TableSearchPanel tableSearchPanel;

  @UiField
  FlowPanel mainContent;

  @UiField
  FlowPanel description;

  @UiField
  SimplePanel content;

  @UiField
  Button options;

  private CollectionStatus collectionStatus;
  private ViewerDatabase database;
  private ViewerTable table;
  private String route;
  private List<String> columnsAndValues;

  /**
   * Synchronous Table panel that receives the data and does not need to
   * asynchronously query solr
   *
   * @param database
   *          the database
   * @param table
   *          the table
   * @param searchInfo
   *          the predefined search
   */
  private TablePanel(CollectionStatus status, ViewerDatabase database, ViewerTable table, SearchInfo searchInfo,
    String route) {
    tableSearchPanel = new TableSearchPanel(searchInfo, status);

    initWidget(uiBinder.createAndBindUi(this));

    this.collectionStatus = status;
    this.database = database;
    this.table = table;
    this.route = route;
    init();
  }

  /**
   * Asynchronous table panel that receives UUIDs and needs to get the objects
   * from solr
   *
   * @param viewerDatabase
   *          the database
   * @param tableId
   *          the table ID
   */
  private TablePanel(CollectionStatus status, ViewerDatabase viewerDatabase, final String tableId, String route) {
    this(status, viewerDatabase, tableId, null, route);
  }

  /**
   * Asynchronous table panel that receives UUIDs and needs to get the objects
   * from solr. This method supports a predefined search (SearchInfo instance) as
   * a JSON String.
   *
   * @param viewerDatabase
   *          the database
   * @param tableId
   *          the table Id ('schema'.'table'
   * @param searchInfoJson
   *          the SearchInfo instance as a JSON String
   */
  private TablePanel(CollectionStatus status, ViewerDatabase viewerDatabase, final String tableId,
    String searchInfoJson, String route) {
    collectionStatus = status;
    database = viewerDatabase;
    table = database.getMetadata().getTableById(tableId);

    this.route = route;

    if (searchInfoJson != null) {
      tableSearchPanel = new TableSearchPanel(searchInfoJson, collectionStatus);
    } else {
      tableSearchPanel = new TableSearchPanel(collectionStatus);
    }

    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forTable(database.getMetadata().getName(),
      database.getUuid(), collectionStatus.getTableStatus(table.getUuid()).getCustomName(), table.getId()));
  }

  public void setColumnsAndValues(List<String> columnsAndValues) {
    this.columnsAndValues = columnsAndValues;
  }

  public void update() {
    final Map<String, Boolean> selectedColumns;
    if (HistoryManager.ROUTE_FOREIGN_KEY.equals(route)) {
      selectedColumns = ForeignKeyPanelOptions.getInstance(database, table.getId(), columnsAndValues)
        .getSelectedColumns();
    } else {
      selectedColumns = TablePanelOptions.getInstance(collectionStatus, database, table.getId()).getSelectedColumns();
    }
    tableSearchPanel.setColumnVisibility(selectedColumns);
    applyCurrentSearchInfoJsonIfExists();
  }

  private void init() {
    final CollectionObserver collectionObserver = ObserverManager.getCollectionObserver();
    collectionObserver.addObserver(this);

    mainHeader.setWidget(CommonClientUtils.getHeader(collectionStatus.getTableStatus(table.getUuid()), table, "h1",
      database.getMetadata().getSchemas().size() > 1));
    options.setText(messages.basicActionOptions());

    options.addClickHandler(event -> {
      if (HistoryManager.ROUTE_TABLE.equals(route)) {
        HistoryManager.gotoTableOptions(database.getUuid(), table.getId());
      } else if (HistoryManager.ROUTE_FOREIGN_KEY.equals(route)) {
        HistoryManager.gotoRelationOptions(database.getUuid(), table.getId(), columnsAndValues);
      }
    });

    if (ViewerStringUtils.isNotBlank(collectionStatus.getTableStatus(table.getUuid()).getCustomDescription())) {
      MetadataField instance = MetadataField
        .createInstance(collectionStatus.getTableStatus(table.getUuid()).getCustomDescription());
      instance.setCSS("table-row-description");
      description.add(instance);
    }

    tableSearchPanel.provideSource(database, table);
  }

  private void applyCurrentSearchInfoJsonIfExists() {
    if (tableSearchPanel.isSearchInfoDefined()) {
      tableSearchPanel.applySearchInfoJson();
    }
  }

  private void applySearchInfoJson(String searchInfoJson) {
    tableSearchPanel.applySearchInfoJson(searchInfoJson);
  }
}
