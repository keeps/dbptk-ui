package com.databasepreservation.common.client.common.visualization.browse.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.common.search.TableSearchPanel;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.configuration.observer.IColumnVisibilityObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TablePanel extends RightPanel implements ICollectionStatusObserver, IColumnVisibilityObserver {
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
      // instance = new TablePanel(status, database, tableId, searchInfoJson, route);
      instance.applySearchInfoJson(searchInfoJson);
      // instances.put(code, instance);
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

  @UiField
  FlowPanel advancedOptions;

  @UiField
  MenuBar configurationMenu;

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

  private void init() {
    ObserverManager.getCollectionObserver().addObserver(this);
    ObserverManager.getColumnVisibilityObserver().addObserver(this);

    mainHeader.setWidget(CommonClientUtils.getHeader(collectionStatus.getTableStatus(table.getUuid()), table, "h1",
      database.getMetadata().getSchemas().size() > 1));
    UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
      @Override
      public void onSuccess(User user) {
        if (user.isAdmin() && ApplicationType.getType().equals(ViewerConstants.SERVER)) {
          buildMenu();
        } else if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
          advancedOptions.remove(configurationMenu);
          Button columnsManagementBtn = new Button(messages
            .dataTransformationBtnManageTable(collectionStatus.getTableStatus(table.getUuid()).getCustomName()));
          columnsManagementBtn.setStyleName("btn btn-link");
          columnsManagementBtn
            .addClickHandler(event -> HistoryManager.gotoColumnsManagement(database.getUuid(), table.getId()));
          advancedOptions.insert(columnsManagementBtn, 0);
        }
      }
    });
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

  private void buildMenu() {
    MenuBar configurationSubMenuBar = new MenuBar(true);
    MenuItem columnMenuItem = new MenuItem(
      SafeHtmlUtils.fromString(
        messages.dataTransformationBtnManageTable(collectionStatus.getTableStatus(table.getUuid()).getCustomName())),
      () -> HistoryManager.gotoColumnsManagement(database.getUuid(), table.getId()));
    configurationSubMenuBar.addItem(columnMenuItem);
    MenuItem dataTransformationMenuItem = new MenuItem(
      SafeHtmlUtils.fromString(
        messages.dataTransformationBtnTransformTable(collectionStatus.getTableStatus(table.getUuid()).getCustomName())),
      () -> HistoryManager.gotoDataTransformation(database.getUuid(), table.getId()));
    if (ApplicationType.getType().equals(ViewerConstants.SERVER)) {
      configurationSubMenuBar.addItem(dataTransformationMenuItem);
    }
    configurationMenu.addItem(SafeHtmlUtils.fromString(messages.advancedConfigurationLabelForMainTitle()),
      configurationSubMenuBar);
    configurationMenu.setStyleName("btn btn-link");
  }

  private void applyCurrentSearchInfoJsonIfExists() {
    if (tableSearchPanel.isSearchInfoDefined()) {
      tableSearchPanel.applySearchInfoJson();
    }
  }

  private void applySearchInfoJson(String searchInfoJson) {
    tableSearchPanel.applySearchInfoJson(searchInfoJson);
  }

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    instances.clear();
  }

  @Override
  public void updateColumnVisibility(String tableId, Map<String, Boolean> columns) {
    if (table.getId().equals(tableId)) {
      tableSearchPanel.setColumnVisibility(columns);
      applyCurrentSearchInfoJsonIfExists();
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (!collectionStatus.getTableStatusByTableId(table.getId()).isShow()) {
      History.back();
      Dialogs.showInformationDialog(messages.resourceNotAvailableTitle(),
        messages.resourceNotAvailableTableHiddenDescription(
          collectionStatus.getTableStatusByTableId(table.getId()).getCustomName()),
        messages.basicActionClose());
    }
  }
}
