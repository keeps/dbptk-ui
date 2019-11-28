package com.databasepreservation.common.client.common.visualization.browse.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.common.search.TableSearchPanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.visualization.browse.foreignKey.ForeignKeyPanelOptions;
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
public class TablePanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, TablePanel> instances = new HashMap<>();

  public static TablePanel getInstance(ViewerDatabase database, String tableUUID, String route) {
    return getInstance(database, tableUUID, null, route);
  }

  public static TablePanel getInstance(ViewerDatabase database, String tableUUID, String searchInfoJson, String route) {
    String separator = "/";
    String code = database.getUuid() + separator + tableUUID;

    TablePanel instance = instances.get(code);
    if (instance == null) {
      instance = new TablePanel(database, tableUUID, searchInfoJson, route);
      instances.put(code, instance);
    } else if (searchInfoJson != null) {
      instance.applySearchInfoJson(searchInfoJson);
    } else if (instance.tableSearchPanel.isSearchInfoDefined()) {
      instance = new TablePanel(database, tableUUID, route);
      instances.put(code, instance);
    }

    return instance;
  }

  public static TablePanel createInstance(ViewerDatabase database, ViewerTable table, SearchInfo searchInfo,
    String route) {
    return new TablePanel(database, table, searchInfo, route);
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
  private TablePanel(ViewerDatabase database, ViewerTable table, SearchInfo searchInfo, String route) {
    tableSearchPanel = new TableSearchPanel(searchInfo);

    initWidget(uiBinder.createAndBindUi(this));

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
   * @param tableUUID
   *          the table UUID
   */
  private TablePanel(ViewerDatabase viewerDatabase, final String tableUUID, String route) {
    this(viewerDatabase, tableUUID, null, route);
  }

  /**
   * Asynchronous table panel that receives UUIDs and needs to get the objects
   * from solr. This method supports a predefined search (SearchInfo instance) as
   * a JSON String.
   *
   * @param viewerDatabase
   *          the database
   * @param tableUUID
   *          the table UUID
   * @param searchInfoJson
   *          the SearchInfo instance as a JSON String
   */
  private TablePanel(ViewerDatabase viewerDatabase, final String tableUUID, String searchInfoJson, String route) {
    database = viewerDatabase;
    table = database.getMetadata().getTable(tableUUID);
    this.route = route;

    if (searchInfoJson != null) {
      tableSearchPanel = new TableSearchPanel(searchInfoJson);
    } else {
      tableSearchPanel = new TableSearchPanel();
    }

    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forTable(database.getMetadata().getName(),
      database.getUuid(), table.getNameWithoutPrefix(), table.getUUID()));
  }

  public void setColumnsAndValues(List<String> columnsAndValues) {
    this.columnsAndValues = columnsAndValues;
  }

  public void update() {
    final Map<String, Boolean> selectedColumns;
    if (HistoryManager.ROUTE_FOREIGN_KEY.equals(route)) {
      selectedColumns = ForeignKeyPanelOptions.getInstance(database, table.getUUID(), columnsAndValues)
        .getSelectedColumns();
    } else {
      selectedColumns = TablePanelOptions.getInstance(database, table.getUUID()).getSelectedColumns();
    }
    tableSearchPanel.setColumnVisibility(selectedColumns);
    applyCurrentSearchInfoJsonIfExists();
  }

  private void init() {
    mainHeader.setWidget(CommonClientUtils.getHeader(table, "h1", database.getMetadata().getSchemas().size() > 1));
    options.setText(messages.basicActionOptions());

    options.addClickHandler(event -> {
      if (HistoryManager.ROUTE_TABLE.equals(route)) {
        HistoryManager.gotoTableOptions(database.getUuid(), table.getUUID());
      } else if (HistoryManager.ROUTE_FOREIGN_KEY.equals(route)) {
        HistoryManager.gotoRelationOptions(database.getUuid(), table.getUUID(), columnsAndValues);
      }
    });

    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      MetadataField instance = MetadataField.createInstance(table.getDescription());
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
