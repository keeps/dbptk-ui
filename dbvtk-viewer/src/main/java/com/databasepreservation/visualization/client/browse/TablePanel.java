package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.databasepreservation.visualization.client.common.search.TableSearchPanel;
import com.databasepreservation.visualization.client.common.utils.CommonClientUtils;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TablePanel extends RightPanel {
  private static Map<String, TablePanel> instances = new HashMap<>();

  public static TablePanel getInstance(ViewerDatabase database, String tableUUID) {
    return getInstance(database, tableUUID, null);
  }

  public static TablePanel getInstance(ViewerDatabase database, String tableUUID, String searchInfoJson) {
    String separator = "/";
    String code = database.getUUID() + separator + tableUUID;

    TablePanel instance = instances.get(code);
    if (instance == null) {
      instance = new TablePanel(database, tableUUID, searchInfoJson);
      instances.put(code, instance);
    } else if (searchInfoJson != null) {
      instance.applySearchInfoJson(searchInfoJson);
    } else if (instance.tableSearchPanel.isSearchInfoDefined()) {
      instance = new TablePanel(database, tableUUID);
      instances.put(code, instance);
    }

    return instance;
  }

  public static TablePanel createInstance(ViewerDatabase database, ViewerTable table, SearchInfo searchInfo) {
    return new TablePanel(database, table, searchInfo);
  }

  interface TablePanelUiBinder extends UiBinder<Widget, TablePanel> {
  }

  @UiField
  SimplePanel mainHeader;

  @UiField(provided = true)
  TableSearchPanel tableSearchPanel;

  @UiField
  HTML description;

  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerTable table;

  private static TablePanelUiBinder uiBinder = GWT.create(TablePanelUiBinder.class);

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
  private TablePanel(ViewerDatabase database, ViewerTable table, SearchInfo searchInfo) {
    tableSearchPanel = new TableSearchPanel(searchInfo);

    initWidget(uiBinder.createAndBindUi(this));

    this.database = database;
    this.table = table;
    this.schema = database.getMetadata().getSchemaFromTableUUID(table.getUUID());
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
  private TablePanel(ViewerDatabase viewerDatabase, final String tableUUID) {
    this(viewerDatabase, tableUUID, null);
  }

  /**
   * Asynchronous table panel that receives UUIDs and needs to get the objects
   * from solr. This method supports a predefined search (SearchInfo instance)
   * as a JSON String.
   *
   * @param viewerDatabase
   *          the database
   * @param tableUUID
   *          the table UUID
   * @param searchInfoJson
   *          the SearchInfo instance as a JSON String
   */
  private TablePanel(ViewerDatabase viewerDatabase, final String tableUUID, String searchInfoJson) {
    database = viewerDatabase;
    table = database.getMetadata().getTable(tableUUID);
    schema = database.getMetadata().getSchemaFromTableUUID(tableUUID);

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
    BreadcrumbManager.updateBreadcrumb(
      breadcrumb,
      BreadcrumbManager.forTable(database.getMetadata().getName(), database.getUUID(), schema.getName(),
        schema.getUUID(), table.getName(), table.getUUID()));
  }

  private void init() {
    mainHeader.setWidget(CommonClientUtils.getSchemaAndTableHeader(database.getUUID(), table, "h1"));

    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      description.setHTML(CommonClientUtils.getFieldHTML("Description", table.getDescription()));
    }

    tableSearchPanel.provideSource(database, table);
  }

  private void applySearchInfoJson(String searchInfoJson) {
    tableSearchPanel.applySearchInfoJson(searchInfoJson);
  }
}
