package com.databasepreservation.main.desktop.client.dbptk.wizard.upload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.desktop.ComboBoxField;
import com.databasepreservation.main.common.shared.client.common.desktop.FileUploadField;
import com.databasepreservation.main.common.shared.client.common.desktop.GenericField;
import com.databasepreservation.main.common.shared.client.common.lists.MultipleSelectionTablePanel;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.common.shared.client.tools.PathUtils;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.common.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.common.shared.models.wizardParameters.ExternalLOBsParameter;
import com.databasepreservation.main.common.shared.models.wizardParameters.TableAndColumnsParameters;
import com.databasepreservation.main.desktop.client.common.dialogs.Dialogs;
import com.databasepreservation.main.desktop.client.common.sidebar.TableAndColumnsSendToSidebar;
import com.databasepreservation.main.desktop.client.common.sidebar.TableAndColumnsSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.diagram.ErDiagram;
import com.databasepreservation.main.desktop.shared.models.ExternalLobsDialogBoxResult;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
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

  interface ViewerMetadataMapper extends ObjectMapper<ViewerMetadata> {
  }

  private static TableAndColumnsUiBinder binder = GWT.create(TableAndColumnsUiBinder.class);

  @UiField
  FlowPanel content, tableAndColumnsList, panel;

  private static HashMap<String, TableAndColumns> instances = new HashMap<>();
  private TableAndColumnsSidebar tableAndColumnsSidebar;
  private TableAndColumnsSendToSidebar tableAndColumnsSendToSidebar;
  private ViewerMetadata metadata;
  private HashMap<String, MultipleSelectionTablePanel<ViewerColumn>> columns = new HashMap<>();
  private HashMap<String, MultipleSelectionTablePanel<ViewerTable>> tables = new HashMap<>();
  private HashMap<String, MultipleSelectionTablePanel<ViewerView>> views = new HashMap<>();
  private HashMap<String, Boolean> tableSelectedStatus = new HashMap<>();
  private HashMap<String, Boolean> viewSelectedStatus = new HashMap<>();
  private HashMap<String, ExternalLOBsParameter> externalLOBsParameters = new HashMap<>();
  private String currentTableUUID = null;
  private String currentBasePath = null;
  private String databaseUUID;
  // false: "SELECT ALL"; true: "SELECT NONE";
  private HashMap<String, Boolean> toggleSelectionTablesMap = new HashMap<>();
  private HashMap<String, Boolean> toggleSelectionViewsMap = new HashMap<>();
  private HashMap<String, Boolean> toggleSelectionColumnsMap = new HashMap<>();
  private HashMap<String, Button> btnToggleSelectionMap = new HashMap<>();
  private HashMap<String, Boolean> initialLoading = new HashMap<>();

  public static TableAndColumns getInstance(String databaseUUID, ConnectionParameters values) {
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new TableAndColumns(values));
    }
    return instances.get(databaseUUID);
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

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID, new DefaultAsyncCallback<IsIndexed>() {
      @Override
      public void onSuccess(IsIndexed result) {
        ViewerDatabase database = (ViewerDatabase) result;
        metadata = database.getMetadata();
        tableAndColumnsSendToSidebar = TableAndColumnsSendToSidebar.newInstance(databaseUUID, metadata);
        tableAndColumnsList.add(tableAndColumnsSendToSidebar);
        initTables();

        content.remove(spinner);
        sideBarHighlighter(TableAndColumnsSendToSidebar.DATABASE_LINK,null,null);
      }

      @Override
      public void onFailure(Throwable caught) {
          Toast.showError(messages.tableAndColumnsPageTitle(), caught.getMessage());
        content.remove(spinner);
      }
    });
  }

  private TableAndColumns(ConnectionParameters values) {
    initWidget(binder.createAndBindUi(this));

    // databaseUUID = values.getURLConnection();
    final DialogBox dialogBox = Dialogs.showWaitResponse(messages.tableAndColumnsPageDialogTitleForRetrievingInformation(), messages.tableAndColumnsPageDialogMessageForRetrievingInformation());

    BrowserService.Util.getInstance().getSchemaInformation(databaseUUID, values,
      new DefaultAsyncCallback<String>() {
        @Override
        public void onSuccess(String result) {
          ViewerMetadataMapper mapper = GWT.create(ViewerMetadataMapper.class);
          metadata = mapper.read(result);
          tableAndColumnsSidebar = TableAndColumnsSidebar.newInstance(metadata);
          tableAndColumnsList.add(tableAndColumnsSidebar);

          initTables();

          sideBarHighlighter(TableAndColumnsSidebar.DATABASE_LINK,null,null);
          dialogBox.hide();
        }

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(messages.tableAndColumnsPageTitle(), caught.getMessage());
          dialogBox.hide();
          //content.remove(spinner);
        }
      });
  }

  @Override
  public void clear() {
    instances.clear();
    if (columns != null) columns.clear();
    if (tables != null) tables.clear();
    if (views != null) views.clear();
    columns = null;
    tables = null;
    views = null;
    instances = new HashMap<>();
    if (tableAndColumnsSidebar != null) tableAndColumnsSidebar.selectNone();
    if (tableAndColumnsSendToSidebar != null) tableAndColumnsSendToSidebar.selectNone();
  }

  @Override
  public boolean validate() {
    boolean tablesEmpty = false;
    boolean viewsEmpty = false;

    for (MultipleSelectionTablePanel<ViewerTable> cellTable : tables.values()) {
      if (cellTable.getSelectionModel().getSelectedSet().isEmpty()) tablesEmpty = true;
    }

    for (MultipleSelectionTablePanel<ViewerView> cellTable : views.values()) {
      if (cellTable.getSelectionModel().getSelectedSet().isEmpty()) viewsEmpty = true;
    }

    if (views.values().isEmpty()) {
      return !tablesEmpty;
    }

    return !tablesEmpty || !viewsEmpty;
  }

  @Override
  public TableAndColumnsParameters getValues() {
    TableAndColumnsParameters parameters = new TableAndColumnsParameters();
    List<String> schemas = new ArrayList<>();
    HashMap<String, ArrayList<ViewerColumn>> values = new HashMap<>();
    for (Map.Entry<String, MultipleSelectionTablePanel<ViewerColumn>> cellTables : columns.entrySet()) {
      String tableUUID = cellTables.getKey();
      ViewerTable table = metadata.getTable(tableUUID);
      if (table != null) {
        schemas.add(table.getSchemaName());
        ArrayList<ViewerColumn> selectedColumns = new ArrayList<>(cellTables.getValue().getSelectionModel().getSelectedSet());
        String key = table.getSchemaName() + "." + table.getName();
        values.put(key, selectedColumns);
      } else {
        ViewerView view = metadata.getView(tableUUID);
        if (view != null) {
          ArrayList<ViewerColumn> selectedColumns = new ArrayList<>(cellTables.getValue().getSelectionModel().getSelectedSet());
          String key = view.getSchemaName() + "." + view.getName();
          values.put(key, selectedColumns);
        }
      }
    }

    parameters.setColumns(values);
    parameters.setExternalLOBsParameters(new ArrayList<>(externalLOBsParameters.values()));
    parameters.setSelectedSchemas(schemas);

    return parameters;
  }

  @Override
  public void error() {
    Toast.showError(messages.tableAndColumnsPageTitle(), messages.tableAndColumnsPageErrorMessageFor(1));
  }

  public void sideBarHighlighter(String toSelect, String schemaUUID, String tableUUID) {
    panel.clear();

    if (tableUUID != null) {
      panel.add(getColumns(tableUUID));
      if (toSelect.equals(TableAndColumnsSidebar.VIEW_LINK)) {
        toSelect = metadata.getView(tableUUID).getName();
      } else if (toSelect.equals(TableAndColumnsSidebar.TABLE_LINK)) {
        toSelect = metadata.getTable(tableUUID).getName();
      }
      currentTableUUID = tableUUID;
    } else if (schemaUUID != null) {
        Label title = new Label();
        title.setText(metadata.getSchema(schemaUUID).getName());
        title.addStyleName("h1");

        FlowPanel tables = new FlowPanel();
      if (getTable(schemaUUID) != null) {
        tables.add(getTable(schemaUUID));
      }
        FlowPanel views = new FlowPanel();
      if (getView(schemaUUID) != null) {
        views.add(getView(schemaUUID));
      }

        TabPanel tabPanel = new TabPanel();
        tabPanel.addStyleName("browseItemMetadata connection-panel");
        tabPanel.add(tables, messages.sidebarMenuTextForTables());
        tabPanel.add(views, messages.sidebarMenuTextForViews());
        tabPanel.selectTab(0);

        panel.add(title);
        panel.add(tabPanel);
    } else {
      panel.add(ErDiagram.getInstance(databaseUUID, metadata, HistoryManager.getCurrentHistoryPath().get(0)));
    }

    if (tableAndColumnsSidebar != null) tableAndColumnsSidebar.select(toSelect);
    if (tableAndColumnsSendToSidebar != null) tableAndColumnsSendToSidebar.select(toSelect);
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
        populateTable(schemaTable, metadata.getSchema(schema.getUUID()));
        tables.put(schema.getUUID(), schemaTable);
      }

      if (!schema.getViews().isEmpty()) {
        MultipleSelectionTablePanel<ViewerView> schemaViews = createCellTableForViewerView();
        populateViews(schemaViews, metadata.getSchema(schema.getUUID()));
        metadata.getSchema(schema.getUUID()).setViewsSchemaUUID();
        views.put(schema.getUUID(), schemaViews);
      }

      for (ViewerTable vTable : schema.getTables()) {
        MultipleSelectionTablePanel<ViewerColumn> tableColumns = createCellTableForViewerColumn();
        populateTableColumns(tableColumns, metadata.getTable(vTable.getUUID()));
        columns.put(vTable.getUUID(), tableColumns);
      }
      for (ViewerView vView : schema.getViews()) {
        MultipleSelectionTablePanel<ViewerColumn> viewColumns = createCellTableForViewerColumn();
        populateViewColumns(viewColumns, metadata.getView(vView.getUUID()));
        columns.put(vView.getUUID(), viewColumns);
      }
    }
  }

  private void defaultSetSelectAll() {
    for (ViewerSchema schema : metadata.getSchemas()) {
      for (ViewerTable vTable : schema.getTables()) {
        for (ViewerColumn column : vTable.getColumns()) {
          initialLoading.put(vTable.getUUID() + column.getDisplayName(), true);
        }
      }
      for (ViewerView vView : schema.getViews()) {
        for (ViewerColumn column : vView.getColumns()) {
          initialLoading.put(vView.getUUID() + column.getDisplayName(), true);
        }
      }
    }
  }

  private MultipleSelectionTablePanel<ViewerColumn> createCellTableForViewerColumn() {
    return new MultipleSelectionTablePanel<>();
  }

  private MultipleSelectionTablePanel<ViewerTable> createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>();
  }

  private MultipleSelectionTablePanel<ViewerView> createCellTableForViewerView() {
    return new MultipleSelectionTablePanel<>();
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
      }
    });

    FlowPanel panel = new FlowPanel();
    panel.addStyleName("select-buttons-container");
    panel.add(btnSelectToggle);

    return panel;
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

  private boolean CellTableSelectionColumnHelper(ViewerView viewerView,
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel, ViewerColumn column,
    MultipleSelectionTablePanel<ViewerView> viewerTableMultipleSelectionTablePanel, Map<String, Boolean> map,
    String key) {
    if (viewerView.getColumns().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
      map.put(key, false);
      toggleSelectionButton(SELECT_COLUMNS_VIEW + viewerView.getSchemaUUID() + viewerView.getUUID(), map, key);
    }
    if (selectionTablePanel.getSelectionModel().isSelected(column)) {
      viewSelectedStatus.put(viewerView.getUUID(), true);
      viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, true);
    } else {
      if (initialLoading.get(viewerView.getUUID() + column.getDisplayName())) {
        viewSelectedStatus.put(viewerView.getUUID(), true);
        viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, true);
        selectionTablePanel.getSelectionModel().setSelected(column, true);
        initialLoading.put(viewerView.getUUID() + column.getDisplayName(), false);
      }
    }

    if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
      map.put(key, true);
      toggleSelectionButton(SELECT_COLUMNS_VIEW + viewerView.getSchemaUUID() + viewerView.getUUID(), map, key);
      viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, false);
      viewSelectedStatus.put(viewerView.getUUID(), false);
    }

    return selectionTablePanel.getSelectionModel().isSelected(column);
  }

  private void populateViewColumns(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    final ViewerView viewerView) {
    Label header = new Label(viewerView.getSchemaName() + "." + viewerView.getName());
    header.addStyleName("h1");

    selectionTablePanel.createTable(header,
      getSelectPanel(SELECT_COLUMNS_VIEW, viewerView.getSchemaUUID(), viewerView.getUUID()),
      viewerView.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 4,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn viewerColumn) {
            MultipleSelectionTablePanel<ViewerView> viewerTableMultipleSelectionTablePanel = getView(
              viewerView.getSchemaUUID());
            return CellTableSelectionColumnHelper(viewerView, selectionTablePanel, viewerColumn,
              viewerTableMultipleSelectionTablePanel, toggleSelectionColumnsMap, viewerView.getUUID());
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForColumnName(), 10,
        new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn obj) {
          return obj.getDisplayName();
        }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForDescription(), 15,
        new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn obj) {
          return obj.getDescription();
        }
      }));
  }

  private void populateViews(MultipleSelectionTablePanel<ViewerView> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    selectionTablePanel.createTable(getSelectPanel(SELECT_VIEWS, viewerSchema.getUUID()),
      viewerSchema.getViews().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 4,
        new Column<ViewerView, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerView viewerView) {
            MultipleSelectionTablePanel<ViewerColumn> viewerColumnTablePanel = getColumns(viewerView.getUUID());
            if (viewerSchema.getViews().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
              //toggleSelectionButton(SELECT_VIEWS + viewerSchema.getUUID(), toggleSelectionViewsMap, viewerView.getUUID());
            }
            if (selectionTablePanel.getSelectionModel().isSelected(viewerView)) {
              if (viewSelectedStatus.get(viewerView.getUUID()) != null
                && !viewSelectedStatus.get(viewerView.getUUID())) {
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
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForViewName(), 10,
        new TextColumn<ViewerView>() {
        @Override
        public String getValue(ViewerView obj) {
          return obj.getName();
        }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForDescription(), 15,
        new TextColumn<ViewerView>() {
        @Override
        public String getValue(ViewerView obj) {
          return obj.getDescription();
        }
      }));
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    selectionTablePanel.createTable(getSelectPanel(SELECT_TABLES, viewerSchema.getUUID()), viewerSchema.getTables().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 4,
        new Column<ViewerTable, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerTable viewerTable) {
            MultipleSelectionTablePanel<ViewerColumn> viewerColumnTablePanel = getColumns(viewerTable.getUUID());
            if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
              // NONE SELECTED
            }

            if (viewerSchema.getTables().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
             // SAME SIZE
            }
            if (selectionTablePanel.getSelectionModel().isSelected(viewerTable)) {
              if (tableSelectedStatus.get(viewerTable.getUUID()) != null
                && !tableSelectedStatus.get(viewerTable.getUUID())) {
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
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForNumberOfRows(), 4,
        new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return String.valueOf(table.getCountRows());
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
      String btnText = messages.basicActionAdd();
      boolean delete = false;
      String id = object.getDisplayName() + "_" + viewerTable.getUUID();

      final ComboBoxField referenceType = ComboBoxField
        .createInstance(messages.tableAndColumnsPageLabelForReferenceType());
      referenceType.setComboBoxValue("File System", "file-system");
      referenceType.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");

      GenericField genericField;
      if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
        FileUploadField fileUploadField = FileUploadField.createInstance(messages.tableAndColumnsPageLabelForBasePath(),
          messages.tableAndColumnsPageTableHeaderTextForSelect());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
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
          referenceType, fileUploadField, messages.basicActionCancel(), messages.basicActionAdd(), delete,
          new DefaultAsyncCallback<ExternalLobsDialogBoxResult>() {
              @Override
            public void onSuccess(ExternalLobsDialogBoxResult result) {
              if (result.isResult()) {
                String id = object.getDisplayName() + "_" + viewerTable.getUUID();
                ExternalLOBsParameter externalLOBsParameter = new ExternalLOBsParameter();
                externalLOBsParameter.setBasePath(currentBasePath);
                externalLOBsParameter.setReferenceType(referenceType.getSelectedValue());
                externalLOBsParameter.setTable(viewerTable);
                externalLOBsParameter.setColumnName(object.getDisplayName());
                externalLOBsParameters.put(id, externalLOBsParameter);
              }
              }
            });
      } else {
        TextBox textBox = new TextBox();
        textBox.addStyleName("form-textbox");
        if (externalLOBsParameters.get(id) != null) {
          textBox.setText(externalLOBsParameters.get(id).getBasePath());
          delete = true;
          btnText = messages.basicActionUpdate();
        }
        genericField = GenericField.createInstance(messages.tableAndColumnsPageLabelForBasePath(), textBox);
        genericField.setCSSMetadata("form-row", "form-label-spaced");

        Dialogs.showExternalLobsSetupDialog(messages.tableAndColumnsPageDialogTitleForExternalLOBDialog(),
          referenceType, genericField, "Cancel", btnText, delete,
          new DefaultAsyncCallback<ExternalLobsDialogBoxResult>() {
            @Override
            public void onSuccess(ExternalLobsDialogBoxResult result) {
              if (result.getOption().equals("add") && result.isResult()) {
                ExternalLOBsParameter externalLOBsParameter = new ExternalLOBsParameter();
                externalLOBsParameter.setBasePath(textBox.getText());
                externalLOBsParameter.setReferenceType(referenceType.getSelectedValue());
                externalLOBsParameter.setTable(metadata.getTable(currentTableUUID));
                externalLOBsParameter.setColumnName(object.getDisplayName());

                MultipleSelectionTablePanel<ViewerColumn> viewerColumnMultipleSelectionTablePanel = getColumns(
                  viewerTable.getUUID());
                viewerColumnMultipleSelectionTablePanel.getDisplay().redrawRow(index);
                externalLOBsParameters.put(id, externalLOBsParameter);
              }
              if (result.getOption().equals("delete") && result.isResult()) {
                String id = object.getDisplayName() + "_" + viewerTable.getUUID();
                externalLOBsParameters.remove(id);
                MultipleSelectionTablePanel<ViewerColumn> viewerColumnMultipleSelectionTablePanel = getColumns(
                  viewerTable.getUUID());
                viewerColumnMultipleSelectionTablePanel.getDisplay().redrawRow(index);
              }
            }
          });
      }
    });

    selectionTablePanel.createTable(header,
      getSelectPanel(SELECT_COLUMNS_TABLE, viewerTable.getSchemaUUID(), viewerTable.getUUID()),
      viewerTable.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 4,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn viewerColumn) {
            MultipleSelectionTablePanel<ViewerTable> viewerTableMultipleSelectionTablePanel = getTable(
              viewerTable.getSchemaUUID());

            if (viewerTable.getColumns().size() == selectionTablePanel.getSelectionModel().getSelectedSet().size()) {
              toggleSelectionColumnsMap.put(viewerTable.getUUID(), false);
              toggleSelectionButton(SELECT_COLUMNS_TABLE + viewerTable.getSchemaUUID() + viewerTable.getUUID(),
                toggleSelectionColumnsMap, viewerTable.getUUID());
            }
            if (selectionTablePanel.getSelectionModel().isSelected(viewerColumn)) {
              tableSelectedStatus.put(viewerTable.getUUID(), true);
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, true);
            } else {
              if (initialLoading.get(viewerTable.getUUID() + viewerColumn.getDisplayName())) {
                viewSelectedStatus.put(viewerTable.getUUID(), true);
                viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, true);
                selectionTablePanel.getSelectionModel().setSelected(viewerColumn, true);
                initialLoading.put(viewerTable.getUUID() + viewerColumn.getDisplayName(), false);
              }
            }

            if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
              toggleSelectionColumnsMap.put(viewerTable.getUUID(), true);
              toggleSelectionButton(SELECT_COLUMNS_TABLE + viewerTable.getSchemaUUID() + viewerTable.getUUID(),
                toggleSelectionColumnsMap, viewerTable.getUUID());
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, false);
              tableSelectedStatus.put(viewerTable.getUUID(), false);
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
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForOriginalTypeName(), 10,
        new TextColumn<ViewerColumn>() {

        @Override
        public String getValue(ViewerColumn column) {
          return column.getType().getOriginalTypeName();
        }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForDescription(), 15,
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
          String id = object.getDisplayName() + "_" + viewerTable.getUUID();
          if (externalLOBsParameters.get(id) != null) {
            StringBuilder sb = new StringBuilder();
            final ExternalLOBsParameter externalLOBsParameter = externalLOBsParameters.get(id);
              sb.append(messages.tableAndColumnsPageLabelForReferenceType()).append(": ")
                .append(externalLOBsParameter.getReferenceType()).append("\n")
                .append(messages.tableAndColumnsPageLabelForBasePath()).append(": ")
                .append(externalLOBsParameter.getBasePath());
            return SafeHtmlUtils.fromString(sb.toString());
          }
          return null;
        }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForOptions(), 10,
        buttonDatabaseColumn));
  }

  private List<ViewerTable> filtered(List<ViewerTable> tableList) {
    List<ViewerTable> finalList = new ArrayList<>();
    for (ViewerTable viewerTable : tableList) {
      if (!viewerTable.getName().startsWith("VIEW_")) {
        finalList.add(viewerTable);
      }
    }

    return finalList;
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