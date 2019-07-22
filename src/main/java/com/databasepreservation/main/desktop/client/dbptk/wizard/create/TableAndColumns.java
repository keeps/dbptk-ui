package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.lists.MultipleSelectionTablePanel;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.sidebar.TableAndColumnsSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.diagram.ErDiagram;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.TableAndColumnsParameters;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumns extends WizardPanel<TableAndColumnsParameters> {
  private static final String SELECT_TABLES = "select_tables";
  private static final String SELECT_COLUMNS_TABLE = "select_columns_table";
  private static final String SELECT_COLUMNS_VIEW = "select_columns_view";
  private static final String SELECT_VIEWS = "select_views";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface TableAndColumnsUiBinder extends UiBinder<Widget, TableAndColumns> {
  }

  private static TableAndColumnsUiBinder binder = GWT.create(TableAndColumnsUiBinder.class);

  @UiField
  FlowPanel content, tableAndColumnsList, panel;

  private static HashMap<String, TableAndColumns> instances = new HashMap<>();
  private TableAndColumnsSidebar tableAndColumnsSidebar;
  private ViewerMetadata metadata;
  private HashMap<String, MultipleSelectionTablePanel<ViewerColumn>> columns = new HashMap<>();
  private HashMap<String, MultipleSelectionTablePanel<ViewerTable>> tables = new HashMap<>();
  private HashMap<String, MultipleSelectionTablePanel<ViewerView>> views = new HashMap<>();
  private HashMap<String, Boolean> tableSelectedStatus = new HashMap<>();
  private HashMap<String, Boolean> viewSelectedStatus = new HashMap<>();
  private String currentTableUUID = null;
  private String currentSchemaUUID = null;

  public static TableAndColumns getInstance(ConnectionParameters values) {
    final String urlConnection = values.getURLConnection();
    if (instances.get(urlConnection) == null) {
      instances.put(urlConnection, new TableAndColumns(values.getModuleName(), values));
    }
    return instances.get(urlConnection);
  }

  private TableAndColumns(String moduleName, ConnectionParameters values) {
    initWidget(binder.createAndBindUi(this));

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);

    BrowserService.Util.getInstance().getSchemaInformation("test", values,
      new DefaultAsyncCallback<ViewerMetadata>() {
        @Override
        public void onSuccess(ViewerMetadata result) {
          metadata = result;
          tableAndColumnsSidebar = TableAndColumnsSidebar.newInstance(result);
          tableAndColumnsList.add(tableAndColumnsSidebar);

          initTables();

          content.remove(spinner);
          sideBarHighlighter(TableAndColumnsSidebar.DATABASE_LINK,null,null);
        }
      });
  }

  @Override
  public void clear() {
    instances.clear();
    columns.clear();
    tables.clear();
    views.clear();
    columns = null;
    tables = null;
    views = null;
    instances = new HashMap<>();
    tableAndColumnsSidebar.selectNone();
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

    return !tablesEmpty || !viewsEmpty;
  }

  @Override
  public TableAndColumnsParameters getValues() {
    HashMap<String, ArrayList<ViewerColumn>> values = new HashMap<>();
    for (Map.Entry<String, MultipleSelectionTablePanel<ViewerColumn>> cellTables : columns.entrySet()) {
      String tableUUID = cellTables.getKey();
      ViewerTable table = metadata.getTable(tableUUID);
      if (table != null) {
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

    return  new TableAndColumnsParameters(values);
  }

  @Override
  public void error() {
    Toast.showError("Select tables"); //TODO: Improve error message, add electron option to display notification
  }

  public void sideBarHighlighter(String toSelect, String schemaUUID, String tableUUID) {
    panel.clear();

//    GWT.log(toSelect);

    if (tableUUID != null) {
      panel.add(getColumns(tableUUID));
      if (toSelect.equals(TableAndColumnsSidebar.VIEW_LINK)) {
        toSelect = metadata.getView(tableUUID).getName();
      } else if (toSelect.equals(TableAndColumnsSidebar.TABLE_LINK)) {
        toSelect = metadata.getTable(tableUUID).getName();
      }
      currentTableUUID = tableUUID;
    } else if (schemaUUID != null) {
        FlowPanel tables = new FlowPanel();
        tables.add(getTable(schemaUUID));
        FlowPanel views = new FlowPanel();
        views.add(getView(schemaUUID));

        TabPanel tabPanel = new TabPanel();
        tabPanel.addStyleName("browseItemMetadata connection-panel");
        tabPanel.add(tables, messages.sidebarTables());
        tabPanel.add(views, messages.sidebarViews());
        tabPanel.selectTab(0);

        panel.add(tabPanel);
        currentSchemaUUID = schemaUUID;
    } else {
      panel.add(ErDiagram.getInstance(metadata));
    }
    tableAndColumnsSidebar.select(toSelect);
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
    for (ViewerSchema schema : metadata.getSchemas()) {
      MultipleSelectionTablePanel<ViewerTable> schemaTables = createCellTableViewerTable();
      populateTables(schemaTables, metadata.getSchema(schema.getUUID()));
      tables.put(schema.getUUID(), schemaTables);
      MultipleSelectionTablePanel<ViewerView> schemaViews = createCellTableViewerView();
      populateViews(schemaViews, metadata.getSchema(schema.getUUID()));
      metadata.getSchema(schema.getUUID()).setViewsSchemaUUID();
      views.put(schema.getUUID(), schemaViews);
      for (ViewerTable vTable : schema.getTables()) {
        MultipleSelectionTablePanel<ViewerColumn> tableColumns = createCellTableViewerColumn();
        populateTableColumns(tableColumns, metadata.getTable(vTable.getUUID()));
        columns.put(vTable.getUUID(), tableColumns);
      }
      for (ViewerView vView : schema.getViews()) {
        MultipleSelectionTablePanel<ViewerColumn> viewColumns = createCellTableViewerColumn();
        populateViewColumns(viewColumns, metadata.getView(vView.getUUID()));
        columns.put(vView.getUUID(), viewColumns);
      }

    }
  }

  private MultipleSelectionTablePanel<ViewerColumn> createCellTableViewerColumn() {
    return new MultipleSelectionTablePanel<>();
  }

  private MultipleSelectionTablePanel<ViewerTable> createCellTableViewerTable() {
    return new MultipleSelectionTablePanel<>();
  }

  private MultipleSelectionTablePanel<ViewerView> createCellTableViewerView() {
    return new MultipleSelectionTablePanel<>();
  }

  private FlowPanel getSelectPanel(String id) {
    Button btnSelectAll = new Button();
    btnSelectAll.setText(messages.selectAll());
    btnSelectAll.addStyleName("btn btn-primary btn-select-all");
    btnSelectAll.getElement().setId(id + "_all");

    btnSelectAll.addClickHandler(event -> {
      switch (btnSelectAll.getElement().getId()) {
        case SELECT_TABLES + "_all":
          if (currentSchemaUUID != null) {
            MultiSelectionModel<ViewerTable> selectionModel = getTable(currentSchemaUUID).getSelectionModel();
            for (ViewerTable viewerTable : metadata.getSchema(currentSchemaUUID).getTables()) {
              selectionModel.setSelected(viewerTable, true);
            }
          }
          break;
        case SELECT_COLUMNS_TABLE + "_all":
          if (currentTableUUID != null) {
            MultiSelectionModel<ViewerColumn> selectionModel = getColumns(currentTableUUID).getSelectionModel();
            for (ViewerColumn viewerColumn : metadata.getTable(currentTableUUID).getColumns()) {
              selectionModel.setSelected(viewerColumn, true);
            }
          }
          break;
        case SELECT_COLUMNS_VIEW + "_all":
          if (currentTableUUID != null) {
            MultiSelectionModel<ViewerColumn> selectionModel = getColumns(currentTableUUID).getSelectionModel();
            for (ViewerColumn viewerColumn : metadata.getView(currentTableUUID).getColumns()) {
              selectionModel.setSelected(viewerColumn, true);
            }
          }
          break;
        case SELECT_VIEWS + "_all":
          if (currentSchemaUUID != null) {
            MultiSelectionModel<ViewerView> selectionModel = getView(currentSchemaUUID).getSelectionModel();
            for (ViewerView viewerView : metadata.getSchema(currentSchemaUUID).getViews()) {
              selectionModel.setSelected(viewerView, true);
            }
          }
          break;
      }
    });

    Button btnSelectNone = new Button();
    btnSelectNone.setText(messages.selectNone());
    btnSelectNone.addStyleName("btn btn-primary btn-select-none");
    btnSelectNone.getElement().setId(id + "_none");

    btnSelectNone.addClickHandler(event -> {
      switch (btnSelectNone.getElement().getId()) {
        case SELECT_TABLES + "_none":
          if (currentSchemaUUID != null) {
            MultiSelectionModel<ViewerTable> selectionModel = getTable(currentSchemaUUID).getSelectionModel();
            for (ViewerTable viewerTable : metadata.getSchema(currentSchemaUUID).getTables()) {
              selectionModel.setSelected(viewerTable, false);
            }
          }
          break;
        case SELECT_COLUMNS_TABLE + "_none":
          if (currentTableUUID != null) {
            MultiSelectionModel<ViewerColumn> selectionModel = getColumns(currentTableUUID).getSelectionModel();
            for (ViewerColumn viewerColumn : metadata.getTable(currentTableUUID).getColumns()) {
              selectionModel.setSelected(viewerColumn, false);
            }
          }
          break;
        case SELECT_VIEWS + "_none":
          if (currentSchemaUUID != null) {
            MultiSelectionModel<ViewerView> selectionModel = getView(currentSchemaUUID).getSelectionModel();
            for (ViewerView viewerView : metadata.getSchema(currentSchemaUUID).getViews()) {
              selectionModel.setSelected(viewerView, false);
            }
          }
          break;
        case SELECT_COLUMNS_VIEW + "_none":
          if (currentTableUUID != null) {
            MultiSelectionModel<ViewerColumn> selectionModel = getColumns(currentTableUUID).getSelectionModel();
            for (ViewerColumn viewerColumn : metadata.getView(currentTableUUID).getColumns()) {
              selectionModel.setSelected(viewerColumn, false);
            }
          }
          break;
      }
    });

    FlowPanel panel = new FlowPanel();
    panel.addStyleName("select-buttons-container");
    panel.add(btnSelectAll);
    panel.add(btnSelectNone);

    return panel;
  }

  private void populateViewColumns(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    final ViewerView viewerView) {
    Label header = new Label(viewerView.getName());
    header.addStyleName("h4");

    selectionTablePanel.createTable(header, getSelectPanel(SELECT_COLUMNS_VIEW), viewerView.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 15,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn object) {
            MultipleSelectionTablePanel<ViewerView> viewerTableMultipleSelectionTablePanel = getView(
              viewerView.getSchemaUUID());
            if (selectionTablePanel.getSelectionModel().isSelected(object)) {
              viewSelectedStatus.put(viewerView.getUUID(), true);
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, true);
            }

            if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerView, false);
              viewSelectedStatus.put(viewerView.getUUID(), false);
            }
            return selectionTablePanel.getSelectionModel().isSelected(object);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn obj) {
          return obj.getDisplayName();
        }
      }), new MultipleSelectionTablePanel.ColumnInfo<>(messages.description(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn obj) {
          return obj.getDescription();
        }
      }));
  }

  private void populateViews(MultipleSelectionTablePanel<ViewerView> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    Label header = new Label(viewerSchema.getName());
    header.addStyleName("h4");

    selectionTablePanel.createTable(header, getSelectPanel(SELECT_VIEWS), viewerSchema.getViews().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 15,
        new Column<ViewerView, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerView object) {
            MultipleSelectionTablePanel<ViewerColumn> viewerColumnTablePanel = getColumns(object.getUUID());
            if (selectionTablePanel.getSelectionModel().isSelected(object)) {
              if (!viewSelectedStatus.get(object.getUUID())) {
                for (ViewerColumn column : object.getColumns()) {
                  viewerColumnTablePanel.getSelectionModel().setSelected(column, true);
                }
              }
            } else {
              for (ViewerColumn column : object.getColumns()) {
                viewerColumnTablePanel.getSelectionModel().setSelected(column, false);
              }
            }
            return selectionTablePanel.getSelectionModel().isSelected(object);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.viewName(), 15, new TextColumn<ViewerView>() {
        @Override
        public String getValue(ViewerView obj) {
          return obj.getName();
        }
      }), new MultipleSelectionTablePanel.ColumnInfo<>(messages.description(), 15, new TextColumn<ViewerView>() {
        @Override
        public String getValue(ViewerView obj) {
          return obj.getDescription();
        }
      }));
  }

  private Iterator<ViewerTable> filtered(List<ViewerTable> tableList) {

    List<ViewerTable> finalList = new ArrayList<>();

    for (ViewerTable viewerTable : tableList) {
      if (!viewerTable.getName().startsWith("VIEW_")) {
//        GWT.log(viewerTable.getName());
        finalList.add(viewerTable);
      }
    }

    return finalList.iterator();
  }

  private void populateTables(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    Label header = new Label(viewerSchema.getName());
    header.addStyleName("h4");

    selectionTablePanel.createTable(header, getSelectPanel(SELECT_TABLES),
        filtered(viewerSchema.getTables()), new MultipleSelectionTablePanel.ColumnInfo<>("", 15,
        new Column<ViewerTable, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerTable object) {
            MultipleSelectionTablePanel<ViewerColumn> viewerColumnTablePanel = getColumns(object.getUUID());
            if (selectionTablePanel.getSelectionModel().isSelected(object)) {
              if (!tableSelectedStatus.get(object.getUUID())) {
                for (ViewerColumn column : object.getColumns()) {
                  viewerColumnTablePanel.getSelectionModel().setSelected(column, true);
                }
              }
            } else {
              for (ViewerColumn column : object.getColumns()) {
                viewerColumnTablePanel.getSelectionModel().setSelected(column, false);
              }
            }
            return selectionTablePanel.getSelectionModel().isSelected(object);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.schema_tableName(), 15, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return table.getName();
        }
      }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.schema_numberOfRows(), 15, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return String.valueOf(table.getCountRows());
        }
      }), new MultipleSelectionTablePanel.ColumnInfo<>(messages.description(), 15, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return table.getDescription();
        }
      }));
  }

  private void populateTableColumns(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    final ViewerTable viewerTable) {
    Label header = new Label(viewerTable.getName());
    header.addStyleName("h4");

    selectionTablePanel.createTable(header, getSelectPanel(SELECT_COLUMNS_TABLE), viewerTable.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 15,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn object) {
            MultipleSelectionTablePanel<ViewerTable> viewerTableMultipleSelectionTablePanel = getTable(
              viewerTable.getSchemaUUID());
            if (selectionTablePanel.getSelectionModel().isSelected(object)) {
              tableSelectedStatus.put(viewerTable.getUUID(), true);
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, true);
            }

            if (selectionTablePanel.getSelectionModel().getSelectedSet().size() == 0) {
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(viewerTable, false);
              tableSelectedStatus.put(viewerTable.getUUID(), false);
            }
            return selectionTablePanel.getSelectionModel().isSelected(object);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerColumn>() {

        @Override
        public String getValue(ViewerColumn column) {
          return column.getDisplayName();
        }
      }), new MultipleSelectionTablePanel.ColumnInfo<>(messages.originalTypeName(), 15, new TextColumn<ViewerColumn>() {

        @Override
        public String getValue(ViewerColumn column) {
          return column.getType().getOriginalTypeName();
        }
      }), new MultipleSelectionTablePanel.ColumnInfo<>(messages.description(), 15, new TextColumn<ViewerColumn>() {

        @Override
        public String getValue(ViewerColumn column) {
          return column.getDescription();
        }
      }));
  }
}