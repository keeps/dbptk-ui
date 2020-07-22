package com.databasepreservation.desktop.client.dbptk.wizard.upload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.ComboBoxField;
import com.databasepreservation.common.client.common.fields.FileUploadField;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.lists.cells.DisableableCheckboxCell;
import com.databasepreservation.common.client.common.lists.cells.helper.CheckboxData;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.table.ExternalLobParameter;
import com.databasepreservation.common.client.models.wizard.table.ExternalLobsDialogBoxResult;
import com.databasepreservation.common.client.models.wizard.table.TableAndColumnsParameters;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.services.MigrationService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.tools.PathUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.tools.WizardUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.desktop.client.common.sidebar.TableAndColumnsSendToSidebar;
import com.databasepreservation.desktop.client.common.sidebar.TableAndColumnsSidebar;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.desktop.client.dbptk.wizard.common.diagram.ErDiagram;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumns extends WizardPanel<TableAndColumnsParameters> {
  private static final String SELECT_TABLES = "select_tables";
  private static final String SELECT_COLUMNS_TABLE = "select_table_columns";
  private static final String SELECT_COLUMNS_VIEW = "select_view_columns";
  private static final String SELECT_VIEWS = "select_views";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface TableAndColumnsUiBinder extends UiBinder<Widget, TableAndColumns> {
  }

  private static TableAndColumnsUiBinder binder = GWT.create(TableAndColumnsUiBinder.class);

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel tableAndColumnsList;

  @UiField
  FlowPanel panel;

  private static Map<String, TableAndColumns> instances = new HashMap<>();
  private TableAndColumnsSidebar tableAndColumnsSidebar;
  private TableAndColumnsSendToSidebar tableAndColumnsSendToSidebar;
  private ViewerMetadata metadata;
  private Map<String, MultipleSelectionTablePanel<ViewerColumn>> columns = new HashMap<>();
  private Map<String, MultipleSelectionTablePanel<ViewerTable>> tables = new HashMap<>();
  private Map<String, MultipleSelectionTablePanel<ViewerView>> views = new HashMap<>();
  private Map<String, Boolean> tableSelectedStatus = new HashMap<>();
  private Map<String, Boolean> viewSelectedStatus = new HashMap<>();
  private Map<String, Boolean> viewMaterializationStatus = new HashMap<>();
  private Map<String, Boolean> merkleColumnStatus = new HashMap<>();
  private Map<String, ExternalLobParameter> externalLOBsParameters = new HashMap<>();
  private String currentBasePath = null;
  private String databaseUUID;
  // false: "SELECT ALL"; true: "SELECT NONE";
  private Map<String, Boolean> toggleSelectionTablesMap = new HashMap<>();
  private Map<String, Boolean> toggleSelectionViewsMap = new HashMap<>();
  private Map<String, Boolean> toggleSelectionColumnsMap = new HashMap<>();
  private Map<String, Button> btnToggleSelectionMap = new HashMap<>();
  private Map<String, Boolean> initialLoading = new HashMap<>();
  private boolean externalLOBsDelete = false;
  private String externalLOBsBtnText = messages.basicActionAdd();
  private boolean doSSH = false;

  public static TableAndColumns getInstance(String databaseUUID, ConnectionParameters values) {
    if (instances.get(values.getURLConnection()) == null) {
      instances.put(values.getURLConnection(), new TableAndColumns(databaseUUID, values));
    }
    return instances.get(values.getURLConnection());
  }

  public static TableAndColumns getInstance(String databaseUUID) {
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new TableAndColumns(databaseUUID));
    }

    return instances.get(databaseUUID);
  }

  private TableAndColumns(String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;
    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);

    DatabaseService.Util.call((IsIndexed result) -> {
      ViewerDatabase database = (ViewerDatabase) result;
      metadata = database.getMetadata();
      tableAndColumnsSendToSidebar = TableAndColumnsSendToSidebar.newInstance(databaseUUID, metadata);
      tableAndColumnsList.add(tableAndColumnsSendToSidebar);
      initTables();

      content.remove(spinner);
      sideBarHighlighter(TableAndColumnsSendToSidebar.DATABASE_LINK, null, null);
    }, (String errorMessage) -> {
      content.remove(spinner);
      HistoryManager.gotoSIARDInfo(databaseUUID);
      Dialogs.showErrors(messages.tableAndColumnsPageTitle(), errorMessage, messages.basicActionClose());
    }).retrieve(databaseUUID);
  }

  private TableAndColumns(String databaseUUID, ConnectionParameters values) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = values.getURLConnection();
    this.doSSH = values.doSSH();
    final DialogBox dialogBox = Dialogs.showWaitResponse(
      messages.tableAndColumnsPageDialogTitleForRetrievingInformation(),
        messages.tableAndColumnsPageDialogMessageForRetrievingInformation());
    CreateWizardManager.getInstance().enableNext(false);

    MigrationService.Util.call((ViewerMetadata metadata) -> {
      this.metadata = metadata;
      tableAndColumnsSidebar = TableAndColumnsSidebar.newInstance(metadata);
      tableAndColumnsList.add(tableAndColumnsSidebar);
      initTables();
      sideBarHighlighter(TableAndColumnsSidebar.DATABASE_LINK, null, null);
      dialogBox.hide();
      CreateWizardManager.getInstance().enableNext(true);
    }, (String errorMessage) -> {
      dialogBox.hide();
      Dialogs.showErrors(messages.tableAndColumnsPageTitle(), errorMessage, messages.basicActionClose());
    }).getMetadata(values);
  }

  @Override
  public void clear() {
    instances.clear();
    if (columns != null)
      columns.clear();
    if (tables != null)
      tables.clear();
    if (views != null)
      views.clear();
    columns = null;
    tables = null;
    views = null;
    if (tableAndColumnsSidebar != null)
      tableAndColumnsSidebar.selectNone();
    if (tableAndColumnsSendToSidebar != null)
      tableAndColumnsSendToSidebar.selectNone();
  }

  @Override
  public boolean validate() {
    return true;

    /*
     * boolean tablesEmpty = false; boolean viewsEmpty = false;
     * 
     * for (MultipleSelectionTablePanel<ViewerTable> cellTable : tables.values()) {
     * if (cellTable.getSelectionModel().getSelectedSet().isEmpty()) tablesEmpty =
     * true; }
     * 
     * for (MultipleSelectionTablePanel<ViewerView> cellTable : views.values()) { if
     * (cellTable.getSelectionModel().getSelectedSet().isEmpty()) viewsEmpty = true;
     * }
     * 
     * if (views.values().isEmpty()) { return !tablesEmpty; }
     * 
     * return !tablesEmpty || !viewsEmpty;
     */
  }

  @Override
  public TableAndColumnsParameters getValues() {
    return WizardUtils.getTableAndColumnsParameter(tables, views, columns, externalLOBsParameters,
      viewMaterializationStatus, merkleColumnStatus, metadata);
  }

  @Override
  public void error() {
    Toast.showError(messages.tableAndColumnsPageTitle(), messages.tableAndColumnsPageErrorMessageFor(1));
  }

  public void sideBarHighlighter(String toSelect, String schemaUUID, String tableUUID) {
    panel.clear();

    if (tableUUID != null) {
      panel.add(getColumns(tableUUID));
      if (toSelect.equals(TableAndColumnsSidebar.VIEW_LINK) || toSelect.equals(TableAndColumnsSidebar.TABLE_LINK)) {
        toSelect = ViewerStringUtils.concat(schemaUUID, tableUUID);
      }
    } else if (schemaUUID != null) {
      /*Label title = new Label();
      title.setText(metadata.getSchema(schemaUUID).getName());
      title.addStyleName("h1");*/

      FlowPanel flowPanelTables = new FlowPanel();
      if (getTable(schemaUUID) != null) {
        flowPanelTables.add(getTable(schemaUUID));
      } else {
        Alert alert = new Alert(Alert.MessageAlertType.LIGHT, messages.noItemsToDisplay());
        flowPanelTables.add(alert);
      }

      FlowPanel flowPanelViews = new FlowPanel();
      if (getView(schemaUUID) != null) {
        flowPanelViews.add(getView(schemaUUID));
      } else {
        Alert alert = new Alert(Alert.MessageAlertType.LIGHT, messages.noItemsToDisplay());
        flowPanelViews.add(alert);
      }

      TabPanel tabPanel = new TabPanel();
      tabPanel.addStyleName("browseItemMetadata connection-panel");
      tabPanel.add(flowPanelTables, messages.sidebarMenuTextForTables());
      tabPanel.add(flowPanelViews, messages.sidebarMenuTextForViews());
      tabPanel.selectTab(0);

      //panel.add(title);
      panel.add(tabPanel);
      toSelect = schemaUUID;
    } else {
      final ErDiagram instance = ErDiagram.getInstance(databaseUUID, metadata, HistoryManager.getCurrentHistoryPath().get(0));
      panel.add(instance);
    }

    if (tableAndColumnsSidebar != null)
      tableAndColumnsSidebar.select(toSelect);
    if (tableAndColumnsSendToSidebar != null)
      tableAndColumnsSendToSidebar.select(toSelect);
  }

  private MultipleSelectionTablePanel<ViewerTable> getTable(String schemaUUID) {
    return tables.get(schemaUUID);
  }

  private MultipleSelectionTablePanel<ViewerColumn> getColumns(String tableUUID) {
    return columns.get(tableUUID);
  }

  private MultipleSelectionTablePanel<ViewerView> getView(String schemaUUID) {
    return views.get(schemaUUID);
  }

  private void initTables() {
    defaultSetSelectAll();
    for (ViewerSchema schema : metadata.getSchemas()) {
      if (!schema.getTables().isEmpty()) {
        MultipleSelectionTablePanel<ViewerTable> schemaTable = createCellTableForViewerTable();
        schemaTable.setHeight("70vh");
        populateTable(schemaTable, metadata.getSchema(schema.getUuid()));
        tables.put(schema.getUuid(), schemaTable);
      }

      if (!schema.getViews().isEmpty()) {
        MultipleSelectionTablePanel<ViewerView> schemaViews = createCellTableForViewerView();
        schemaViews.setHeight("70vh");
        populateViews(schemaViews, metadata.getSchema(schema.getUuid()));
        metadata.getSchema(schema.getUuid()).setViewsSchemaUUID();
        views.put(schema.getUuid(), schemaViews);
      }

      for (ViewerTable vTable : schema.getTables()) {
        MultipleSelectionTablePanel<ViewerColumn> tableColumns = createCellTableForViewerColumn();
        tableColumns.setHeight("70vh");
        populateTableColumns(tableColumns, metadata.getTable(vTable.getUuid()));
        columns.put(vTable.getUuid(), tableColumns);
      }
      for (ViewerView vView : schema.getViews()) {
        MultipleSelectionTablePanel<ViewerColumn> viewColumns = createCellTableForViewerColumn();
        viewColumns.setHeight("70vh");
        populateViewColumns(viewColumns, metadata.getView(vView.getUuid()));
        columns.put(vView.getUuid(), viewColumns);
      }
    }
  }

  private void defaultSetSelectAll() {
    for (ViewerSchema schema : metadata.getSchemas()) {
      for (ViewerTable vTable : schema.getTables()) {
        for (ViewerColumn column : vTable.getColumns()) {
          initialLoading.put(vTable.getUuid() + column.getDisplayName(), true);
        }
      }
      for (ViewerView vView : schema.getViews()) {
        for (ViewerColumn column : vView.getColumns()) {
          initialLoading.put(vView.getUuid() + column.getDisplayName(), true);
        }
      }
    }
  }

  private MultipleSelectionTablePanel<ViewerColumn> createCellTableForViewerColumn() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private MultipleSelectionTablePanel<ViewerTable> createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private MultipleSelectionTablePanel<ViewerView> createCellTableForViewerView() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private FlowPanel getSelectPanel(final String id, final String schemaUUID) {
    return getSelectPanel(id, schemaUUID, null);
  }

  private FlowPanel getSelectPanel(final String id, final String schemaUUID, final String viewOrTableUUID) {
    Button btnSelectToggle = new Button();
    btnSelectToggle.setText(messages.basicActionSelectNone());
    btnSelectToggle.addStyleName("btn btn-primary btn-select-none");
    btnSelectToggle.getElement().setId(id);

    final String toggleBtnKey;

    if (viewOrTableUUID != null) {
      toggleBtnKey = id + schemaUUID + viewOrTableUUID;
    } else {
      toggleBtnKey = id + schemaUUID;
    }

    // Map compose by a key and the button associated
    btnToggleSelectionMap.put(toggleBtnKey, btnSelectToggle);

    btnSelectToggle.addClickHandler(event -> {
      switch (btnSelectToggle.getElement().getId()) {
        case SELECT_COLUMNS_TABLE:
          toggleSelectionTableColumns(toggleBtnKey, viewOrTableUUID);
          break;
        case SELECT_COLUMNS_VIEW:
          toggleSelectionViewColumns(toggleBtnKey, viewOrTableUUID);
          break;
        case SELECT_VIEWS:
          toggleSelectionViews(toggleBtnKey, schemaUUID);
          break;
        case SELECT_TABLES:
          toggleSelectionTables(toggleBtnKey, schemaUUID);
          break;
        default:
      }
    });

    FlowPanel flowPanel = new FlowPanel();
    flowPanel.addStyleName("select-buttons-container");
    flowPanel.add(btnSelectToggle);

    return flowPanel;
  }

  private void toggleSelectionTables(final String toggleBtnKey, final String schemaUUID) {
    final boolean result = toggleSelectionButton(toggleBtnKey, toggleSelectionTablesMap, schemaUUID);
    MultiSelectionModel<ViewerTable> selectionModel = getTable(schemaUUID).getSelectionModel();
    for (ViewerTable viewerTable : metadata.getSchema(schemaUUID).getTables()) {
      selectionModel.setSelected(viewerTable, result);
    }
  }

  private void toggleSelectionTableColumns(final String toggleBtnKey, final String tableUUID) {
    final boolean result = toggleSelectionButton(toggleBtnKey, toggleSelectionColumnsMap, tableUUID);
    MultiSelectionModel<ViewerColumn> selectionModel = getColumns(tableUUID).getSelectionModel();
    for (ViewerColumn viewerColumn : metadata.getTable(tableUUID).getColumns()) {
      selectionModel.setSelected(viewerColumn, result);
    }
  }

  private void toggleSelectionViewColumns(final String toggleBtnKey, final String viewUUID) {
    final boolean result = toggleSelectionButton(toggleBtnKey, toggleSelectionColumnsMap, viewUUID);
    MultiSelectionModel<ViewerColumn> selectionModel = getColumns(viewUUID).getSelectionModel();
    for (ViewerColumn viewerColumn : metadata.getView(viewUUID).getColumns()) {
      selectionModel.setSelected(viewerColumn, result);
    }
  }

  private void toggleSelectionViews(final String toggleBtnKey, final String schemaUUID) {
    final boolean result = toggleSelectionButton(toggleBtnKey, toggleSelectionViewsMap, schemaUUID);
    MultiSelectionModel<ViewerView> selectionModel = getView(schemaUUID).getSelectionModel();
    for (ViewerView viewerView : metadata.getSchema(schemaUUID).getViews()) {
      selectionModel.setSelected(viewerView, result);
    }
  }

  private boolean toggleSelectionButton(final String toggleBtnKey, Map<String, Boolean> map, final String key) {
    final Button button = btnToggleSelectionMap.get(toggleBtnKey);
    if (map.get(key) == null) {
      button.setText(messages.basicActionSelectAll());
      button.removeStyleName("btn-select-none");
      button.addStyleName("btn-select-all");
      map.put(key, false);

      return false;
    } else {
      boolean value = map.get(key);
      if (value) {
        button.setText(messages.basicActionSelectAll());
        button.removeStyleName("btn-select-none");
        button.addStyleName("btn-select-all");
      } else {
        button.setText(messages.basicActionSelectNone());
        button.removeStyleName("btn-select-all");
        button.addStyleName("btn-select-none");
      }
      map.put(key, !value);
      btnToggleSelectionMap.put(toggleBtnKey, button);

      return !value;
    }
  }

  private boolean cellTableSelectionColumnHelper(ViewerView viewerView,
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel, ViewerColumn column,
    MultipleSelectionTablePanel<ViewerView> viewerTableMultipleSelectionTablePanel, Map<String, Boolean> map,
    String key) {
    if (viewerView.getColumns().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
      map.put(key, false);
      toggleSelectionButton(SELECT_COLUMNS_VIEW + viewerView.getSchemaUUID() + viewerView.getUuid(), map, key);
    }
    if (selectionTablePanel.getSelectionModel().isSelected(column)) {
      viewSelectedStatus.put(viewerView.getUuid(), true);
      viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, true);
    } else {
      if (initialLoading.get(viewerView.getUuid() + column.getDisplayName())) {
        viewSelectedStatus.put(viewerView.getUuid(), true);
        viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, true);
        selectionTablePanel.getSelectionModel().setSelected(column, true);
        initialLoading.put(viewerView.getUuid() + column.getDisplayName(), false);
      }
    }

    if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
      map.put(key, true);
      toggleSelectionButton(SELECT_COLUMNS_VIEW + viewerView.getSchemaUUID() + viewerView.getUuid(), map, key);
      viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, false);
      viewSelectedStatus.put(viewerView.getUuid(), false);
    }

    return selectionTablePanel.getSelectionModel().isSelected(column);
  }

  private void populateViewColumns(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    final ViewerView viewerView) {
    Label header = new Label(viewerView.getSchemaName() + "." + viewerView.getName());
    header.addStyleName("h1");

    selectionTablePanel.createTable(header,
      getSelectPanel(SELECT_COLUMNS_VIEW, viewerView.getSchemaUUID(), viewerView.getUuid()),
      Collections.singletonList(2), viewerView.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForSelect(), 4,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn viewerColumn) {
            MultipleSelectionTablePanel<ViewerView> viewerTableMultipleSelectionTablePanel = getView(
              viewerView.getSchemaUUID());
            return cellTableSelectionColumnHelper(viewerView, selectionTablePanel, viewerColumn,
              viewerTableMultipleSelectionTablePanel, toggleSelectionColumnsMap, viewerView.getUuid());
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForColumnName(), 10,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn obj) {
            return obj.getDisplayName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForDescription(), 0,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn obj) {
            return obj.getDescription();
          }
        }),
        new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForMerkleOption(), 5,
        getMerkleTreeColumn(viewerView.getUuid())));
  }

  private void populateViews(MultipleSelectionTablePanel<ViewerView> selectionTablePanel,
    final ViewerSchema viewerSchema) {

    final Column<ViewerView, CheckboxData> column = new Column<ViewerView, CheckboxData>(
      new DisableableCheckboxCell(false, false)) {
      @Override
      public CheckboxData getValue(ViewerView viewerView) {
        viewMaterializationStatus.put(viewerView.getUuid(), false);
        final CheckboxData checkboxData = new CheckboxData();
        checkboxData.setChecked(false);
        return checkboxData;
      }
    };

    column.setFieldUpdater((i, viewerView, value) -> {
      viewMaterializationStatus.put(viewerView.getUuid(), value.isChecked());
    });

    selectionTablePanel.createTable(getSelectPanel(SELECT_VIEWS, viewerSchema.getUuid()), Collections.singletonList(1),
      viewerSchema.getViews().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForSelect(), 5,
        new Column<ViewerView, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerView viewerView) {
            MultipleSelectionTablePanel<ViewerColumn> viewerColumnTablePanel = getColumns(viewerView.getUuid());
            if (viewerSchema.getViews().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
              // toggleSelectionButton(SELECT_VIEWS + viewerSchema.getUUID(),
              // toggleSelectionViewsMap, viewerView.getUUID());
            }
            if (selectionTablePanel.getSelectionModel().isSelected(viewerView)) {
              if (viewSelectedStatus.get(viewerView.getUuid()) != null
                && !viewSelectedStatus.get(viewerView.getUuid())) {
                for (ViewerColumn column : viewerView.getColumns()) {
                  viewerColumnTablePanel.getSelectionModel().setSelected(column, true);
                }
              }
            } else {
              for (ViewerColumn column : viewerView.getColumns()) {
                viewerColumnTablePanel.getSelectionModel().setSelected(column, false);
              }
            }
            return selectionTablePanel.getSelectionModel().isSelected(viewerView);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(
        messages.tableAndColumnsPageTableHeaderTextForMaterializeViewOption(), 8, column),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForViewName(), 20,
        new TextColumn<ViewerView>() {
          @Override
          public String getValue(ViewerView obj) {
            return obj.getName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForDescription(), 50,
        new TextColumn<ViewerView>() {
          @Override
          public String getValue(ViewerView obj) {
            return obj.getDescription();
          }
        }));
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    selectionTablePanel.createTable(getSelectPanel(SELECT_TABLES, viewerSchema.getUuid()), new ArrayList<>(),
      viewerSchema.getTables().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForSelect(), 4,
        new Column<ViewerTable, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerTable viewerTable) {
            MultipleSelectionTablePanel<ViewerColumn> viewerColumnTablePanel = getColumns(viewerTable.getUuid());
            if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
              // NONE SELECTED
            }

            if (viewerSchema.getTables().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
              // SAME SIZE
            }
            if (selectionTablePanel.getSelectionModel().isSelected(viewerTable)) {
              if (tableSelectedStatus.get(viewerTable.getUuid()) != null
                && !tableSelectedStatus.get(viewerTable.getUuid())) {
                for (ViewerColumn column : viewerTable.getColumns()) {
                  viewerColumnTablePanel.getSelectionModel().setSelected(column, true);
                }
              }
            } else {
              for (ViewerColumn column : viewerTable.getColumns()) {
                viewerColumnTablePanel.getSelectionModel().setSelected(column, false);
              }
            }
            return selectionTablePanel.getSelectionModel().isSelected(viewerTable);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForTableName(), 10,
        new TextColumn<ViewerTable>() {
          @Override
          public String getValue(ViewerTable table) {
            return table.getName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForDescription(), 15,
        new TextColumn<ViewerTable>() {
          @Override
          public String getValue(ViewerTable table) {
            return table.getDescription();
          }
        }));
  }

  private Column<ViewerColumn, Boolean> getMerkleTreeColumn(String uuid) {
    final Column<ViewerColumn, Boolean> merkleCheckboxOption = new Column<ViewerColumn, Boolean>(
      new CheckboxCell(false, false)) {
      @Override
      public Boolean getValue(ViewerColumn column) {
        merkleColumnStatus.put(WizardUtils.generateMerkleTreeMapKey(uuid, column.getSolrName()), true);
        return true;
      }
    };

    merkleCheckboxOption.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    merkleCheckboxOption.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    merkleCheckboxOption.setFieldUpdater((i, column, value) -> {
      String key = uuid + "_" + column.getSolrName();
      merkleColumnStatus.put(key, value);
    });

    return merkleCheckboxOption;
  }

  private void populateTableColumns(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    final ViewerTable viewerTable) {
    Label header = new Label(viewerTable.getSchemaName() + "." + viewerTable.getName());
    header.addStyleName("h1");

    final ButtonDatabaseColumn buttonDatabaseColumn = new ButtonDatabaseColumn() {
      @Override
      public String getValue(ViewerColumn viewerColumn) {
        return messages.tableAndColumnsPageTextForExternalLOBConfigure();
      }
    };

    buttonDatabaseColumn.setFieldUpdater((index, object, value) -> {
      String id = object.getDisplayName() + "_" + viewerTable.getUuid();

      final ComboBoxField referenceType = ComboBoxField
        .createInstance(messages.tableAndColumnsPageLabelForReferenceType());
      referenceType.setComboBoxValue("File System", "file-system");
      if (doSSH) {
        referenceType.setComboBoxValue("Remote File System", "remote-file-system");
      }
      referenceType.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");

      FlowPanel helperReferenceType = new FlowPanel();
      helperReferenceType.addStyleName("form-helper");
      InlineHTML spanReferenceType = new InlineHTML();
      spanReferenceType.addStyleName("form-text-helper text-muted");
      spanReferenceType.setText(messages.tableAndColumnsPageDescriptionForExternalLOBReferenceType());
      referenceType.addHelperText(spanReferenceType);
      helperReferenceType.add(referenceType);
      helperReferenceType.add(spanReferenceType);

      GenericField genericField;
      if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
        FileUploadField fileUploadField = FileUploadField.createInstance(messages.tableAndColumnsPageLabelForBasePath(),
          messages.tableAndColumnsPageTableHeaderTextForSelect());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");

        FlowPanel helperFileUploadField = new FlowPanel();
        helperFileUploadField.addStyleName("form-helper");
        InlineHTML spanFileUploadField = new InlineHTML();
        spanFileUploadField.addStyleName("form-text-helper text-muted");
        spanFileUploadField.setText(messages.tableAndColumnsPageDescriptionForExternalLOBBasePath());
        fileUploadField.addHelperText(spanFileUploadField);
        helperFileUploadField.add(fileUploadField);
        helperFileUploadField.add(spanFileUploadField);

        if (externalLOBsParameters.get(id) != null) {
          if (externalLOBsParameters.get(id).getBasePath() != null) {
            String displayPath = PathUtils.getFileName(externalLOBsParameters.get(id).getBasePath());
            fileUploadField.setPathLocation(displayPath, externalLOBsParameters.get(id).getBasePath());
          }
          externalLOBsDelete = true;
          externalLOBsBtnText = messages.basicActionUpdate();
        }

        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openDirectory"),
              Collections.emptyList());

            String path = JavascriptUtils.openFileDialog(options);
            if (path != null) {
              currentBasePath = path;
              String displayPath = PathUtils.getFileName(path);
              fileUploadField.setPathLocation(displayPath, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });

        Dialogs.showExternalLobsSetupDialog(messages.tableAndColumnsPageDialogTitleForExternalLOBDialog(),
          helperReferenceType, helperFileUploadField, messages.basicActionCancel(), externalLOBsBtnText,
          externalLOBsDelete, new DefaultAsyncCallback<ExternalLobsDialogBoxResult>() {
            @Override
            public void onSuccess(ExternalLobsDialogBoxResult result) {
              if (result.getOption().equals("add") && result.isResult()) {
                ExternalLobParameter externalLobParameter = new ExternalLobParameter();
                externalLobParameter.setBasePath(currentBasePath);
                externalLobParameter.setReferenceType(referenceType.getSelectedValue());

                MultipleSelectionTablePanel<ViewerColumn> viewerColumnMultipleSelectionTablePanel = getColumns(
                  viewerTable.getUuid());
                viewerColumnMultipleSelectionTablePanel.getDisplay().redrawRow(index);
                externalLOBsParameters.put(id, externalLobParameter);
              }
              if (result.getOption().equals("delete") && result.isResult()) {
                String id = object.getDisplayName() + "_" + viewerTable.getUuid();
                externalLOBsDelete = false;
                externalLOBsParameters.remove(id);
                externalLOBsBtnText = messages.basicActionAdd();
                MultipleSelectionTablePanel<ViewerColumn> viewerColumnMultipleSelectionTablePanel = getColumns(
                  viewerTable.getUuid());
                viewerColumnMultipleSelectionTablePanel.getDisplay().redrawRow(index);
              }
            }
          });
      } else {
        TextBox textBox = new TextBox();
        textBox.addStyleName("form-textbox");
        if (externalLOBsParameters.get(id) != null) {
          textBox.setText(externalLOBsParameters.get(id).getBasePath());
          externalLOBsDelete = true;
          externalLOBsBtnText = messages.basicActionUpdate();
        }
        genericField = GenericField.createInstance(messages.tableAndColumnsPageLabelForBasePath(), textBox);
        genericField.setCSSMetadata("form-row", "form-label-spaced");

        Dialogs.showExternalLobsSetupDialog(messages.tableAndColumnsPageDialogTitleForExternalLOBDialog(),
          helperReferenceType, genericField, messages.basicActionCancel(), externalLOBsBtnText, externalLOBsDelete,
          new DefaultAsyncCallback<ExternalLobsDialogBoxResult>() {
            @Override
            public void onSuccess(ExternalLobsDialogBoxResult result) {
              if (result.getOption().equals("add") && result.isResult()) {
                ExternalLobParameter externalLOBsParameter = new ExternalLobParameter();
                externalLOBsParameter.setBasePath(textBox.getText());
                externalLOBsParameter.setReferenceType(referenceType.getSelectedValue());

                MultipleSelectionTablePanel<ViewerColumn> viewerColumnMultipleSelectionTablePanel = getColumns(
                  viewerTable.getUuid());
                viewerColumnMultipleSelectionTablePanel.getDisplay().redrawRow(index);
                externalLOBsParameters.put(id, externalLOBsParameter);
              }
              if (result.getOption().equals("delete") && result.isResult()) {
                String id = object.getDisplayName() + "_" + viewerTable.getUuid();
                externalLOBsParameters.remove(id);
                MultipleSelectionTablePanel<ViewerColumn> viewerColumnMultipleSelectionTablePanel = getColumns(
                  viewerTable.getUuid());
                viewerColumnMultipleSelectionTablePanel.getDisplay().redrawRow(index);
              }
            }
          });
      }
    });

    selectionTablePanel.createTable(header,
      getSelectPanel(SELECT_COLUMNS_TABLE, viewerTable.getSchemaUUID(), viewerTable.getUuid()), Arrays.asList(4, 6),
      viewerTable.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForSelect(), 4,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn viewerColumn) {
            MultipleSelectionTablePanel<ViewerTable> viewerTableMultipleSelectionTablePanel = getTable(
              viewerTable.getSchemaUUID());

            if (viewerTable.getColumns().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
              toggleSelectionColumnsMap.put(viewerTable.getUuid(), false);
              toggleSelectionButton(SELECT_COLUMNS_TABLE + viewerTable.getSchemaUUID() + viewerTable.getUuid(),
                toggleSelectionColumnsMap, viewerTable.getUuid());
            }
            if (selectionTablePanel.getSelectionModel().isSelected(viewerColumn)) {
              tableSelectedStatus.put(viewerTable.getUuid(), true);
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, true);
            } else {
              if (initialLoading.get(viewerTable.getUuid() + viewerColumn.getDisplayName())) {
                viewSelectedStatus.put(viewerTable.getUuid(), true);
                viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, true);
                selectionTablePanel.getSelectionModel().setSelected(viewerColumn, true);
                initialLoading.put(viewerTable.getUuid() + viewerColumn.getDisplayName(), false);
              }
            }

            if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
              toggleSelectionColumnsMap.put(viewerTable.getUuid(), true);
              toggleSelectionButton(SELECT_COLUMNS_TABLE + viewerTable.getSchemaUUID() + viewerTable.getUuid(),
                toggleSelectionColumnsMap, viewerTable.getUuid());
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, false);
              tableSelectedStatus.put(viewerTable.getUuid(), false);
            }
            return selectionTablePanel.getSelectionModel().isSelected(viewerColumn);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForColumnName(), 10,
        new TextColumn<ViewerColumn>() {

          @Override
          public String getValue(ViewerColumn column) {
            return column.getDisplayName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForOriginalTypeName(), 15,
        new TextColumn<ViewerColumn>() {

          @Override
          public String getValue(ViewerColumn column) {
            return column.getType().getOriginalTypeName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForDescription(), 20,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn column) {
            return column.getDescription();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForColumnFilters(), 10,
        new TooltipDatabaseColumn() {
          @Override
          public SafeHtml getValue(ViewerColumn object) {
            String id = object.getDisplayName() + "_" + viewerTable.getUuid();
            if (externalLOBsParameters.get(id) != null) {
              StringBuilder sb = new StringBuilder();

              final ExternalLobParameter externalLOBsParameter = externalLOBsParameters.get(id);
              sb.append(messages.tableAndColumnsPageLabelForReferenceType()).append(": ")
                .append(externalLOBsParameter.getReferenceType()).append("\n")
                .append(messages.tableAndColumnsPageLabelForBasePath()).append(": ");
              if (externalLOBsParameter.getBasePath() == null) {
                sb.append(messages.emptyBasePath());
              } else {
                sb.append(externalLOBsParameter.getBasePath());
              }
              return SafeHtmlUtils.fromString(sb.toString());
            }
            return null;
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForOptions(), 10,
        buttonDatabaseColumn),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForMerkleOption(), 5,
        getMerkleTreeColumn(viewerTable.getUuid())));
  }

  private abstract static class ButtonDatabaseColumn extends Column<ViewerColumn, String> {
    public ButtonDatabaseColumn() {
      super(new ButtonCell());
    }

    @Override
    public void render(Cell.Context context, ViewerColumn object, SafeHtmlBuilder sb) {
      String value = getValue(object);
      sb.appendHtmlConstant("<button class=\"btn btn-link-info\" type=\"button\" tabindex=\"-1\">");
      if (value != null) {
        sb.append(SafeHtmlUtils.fromString(value));
      }
      sb.appendHtmlConstant("</button>");
    }
  }

  private abstract static class TooltipDatabaseColumn extends Column<ViewerColumn, SafeHtml> {
    public TooltipDatabaseColumn() {
      super(new SafeHtmlCell());
    }

    @Override
    public void render(Cell.Context context, ViewerColumn object, SafeHtmlBuilder sb) {
      SafeHtml value = getValue(object);
      if (value != null) {
        sb.appendHtmlConstant("<div title=\"" + SafeHtmlUtils.htmlEscape(value.asString()) + "\">");
        sb.appendHtmlConstant("External Lobs");
        sb.appendHtmlConstant("</div");
      }
    }
  }
}