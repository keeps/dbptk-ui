package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.lists.MultipleSelectionTablePanel;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.sidebar.TableAndColumnsSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumns extends WizardPanel<TableAndColumnsParameters> {
  private static final String SELECT_TABLES = "select_tables";
  private static final String SELECT_COLUMNS = "select_columns";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface TableAndColumnsUiBinder extends UiBinder<Widget, TableAndColumns> {
  }

  private static TableAndColumnsUiBinder binder = GWT.create(TableAndColumnsUiBinder.class);

  @UiField
  FlowPanel content, tableAndColumnsList, panel;

  private static TableAndColumns instance = null;
  private TableAndColumnsSidebar tableAndColumnsSidebar;
  private ViewerMetadata metadata;
  private HashMap<String, MultipleSelectionTablePanel<ViewerColumn>> columns = new HashMap<>();
  private HashMap<String, MultipleSelectionTablePanel<ViewerTable>> tables = new HashMap<>();
  private HashMap<String, Boolean> tableSelectedStatus = new HashMap<>();
  private String currentTableUUID = null;
  private String currentSchemaUUID = null;

  public static TableAndColumns getInstance(String moduleName, HashMap<String, String> values) {
    if (instance == null) {
      instance = new TableAndColumns(moduleName, values);
    }
    return instance;
  }

  private TableAndColumns(String moduleName, HashMap<String, String> values) {
    initWidget(binder.createAndBindUi(this));

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);

    BrowserService.Util.getInstance().getSchemaInformation("test", moduleName, values,
      new DefaultAsyncCallback<ViewerMetadata>() {
        @Override
        public void onSuccess(ViewerMetadata result) {
          metadata = result;
          tableAndColumnsSidebar = TableAndColumnsSidebar.newInstance(result);
          tableAndColumnsList.add(tableAndColumnsSidebar);

          initTables();

          content.remove(spinner);
        }
      });
  }

  @Override
  public void clear() {
  }

  @Override
  public boolean validate() {
    boolean empty = false;
    for (MultipleSelectionTablePanel<ViewerTable> cellTable : tables.values()) {
      if (cellTable.getSelectionModel().getSelectedSet().isEmpty()) empty = true;
    }
    return !empty;
  }

  @Override
  public TableAndColumnsParameters getValues() {
    HashMap<String, ArrayList<ViewerColumn>> values = new HashMap<>();
    for (Map.Entry<String, MultipleSelectionTablePanel<ViewerColumn>> cellTables : columns.entrySet()) {
      String tableUUID = cellTables.getKey();
      ViewerTable table = metadata.getTable(tableUUID);
      ArrayList<ViewerColumn> selectedColumns = new ArrayList<>(cellTables.getValue().getSelectionModel().getSelectedSet());
      String key = table.getSchemaName() + "." + table.getName();
      values.put(key, selectedColumns);
    }

    return  new TableAndColumnsParameters(values);
  }

  @Override
  public void error() {
    Toast.showError("Select tables"); //TODO: Improve error message, add electron option to display notification
  }

  public void sideBarHighlighter(String toSelect, String schemaUUID, String tableUUID) {
    panel.clear();

    if (tableUUID != null) {
      panel.add(getColumns(tableUUID));
      toSelect = metadata.getTable(tableUUID).getName();
      currentTableUUID = tableUUID;
    } else if (schemaUUID != null) {
      panel.add(getTables(schemaUUID));
      currentSchemaUUID = schemaUUID;
    } else {
      // TODO: IMAGE
    }
    tableAndColumnsSidebar.select(toSelect);
  }

  private MultipleSelectionTablePanel<ViewerTable> getTables(String schemaUUID) {
    return tables.get(schemaUUID);
  }

  private MultipleSelectionTablePanel<ViewerColumn> getColumns(String tableUUID) {
    return columns.get(tableUUID);
  }

  private void initTables() {
    for (ViewerSchema schema : metadata.getSchemas()) {
      MultipleSelectionTablePanel<ViewerTable> table = createCellTableViewerTable();
      populateRowsForViewerTable(table, metadata.getSchema(schema.getUUID()));
      tables.put(schema.getUUID(), table);

      for (ViewerTable vTable : schema.getTables()) {
        MultipleSelectionTablePanel<ViewerColumn> column = createCellTableViewerColumn();
        populateRowsForViewerColumn(column, metadata.getTable(vTable.getUUID()));
        columns.put(vTable.getUUID(), column);
      }
    }
  }

  private MultipleSelectionTablePanel<ViewerColumn> createCellTableViewerColumn() {
    return new MultipleSelectionTablePanel<>();
  }

  private MultipleSelectionTablePanel<ViewerTable> createCellTableViewerTable() {
    return new MultipleSelectionTablePanel<>();
  }

  private FlowPanel getSelectPanel(String id) {
    Button btnSelectAll = new Button();
    btnSelectAll.setText(messages.selectAll());
    btnSelectAll.addStyleName("btn btn-primary btn-select-all");
    btnSelectAll.getElement().setId(id + "_all");

    btnSelectAll.addClickHandler(event -> {
      if (btnSelectAll.getElement().getId().equals(SELECT_TABLES + "_all")) {
        if (currentSchemaUUID != null) {
          MultiSelectionModel<ViewerTable> selectionModel = getTables(currentSchemaUUID).getSelectionModel();
          for (ViewerTable viewerTable : metadata.getSchema(currentSchemaUUID).getTables()) {
            selectionModel.setSelected(viewerTable, true);
          }
        }
      } else if (btnSelectAll.getElement().getId().equals(SELECT_COLUMNS + "_all")) {
        if (currentTableUUID != null) {
          MultiSelectionModel<ViewerColumn> selectionModel = getColumns(currentTableUUID).getSelectionModel();
          for (ViewerColumn viewerColumn : metadata.getTable(currentTableUUID).getColumns()) {
            selectionModel.setSelected(viewerColumn, true);
          }
        }
      }
    });

    Button btnSelectNone = new Button();
    btnSelectNone.setText(messages.selectNone());
    btnSelectNone.addStyleName("btn btn-primary btn-select-none");
    btnSelectNone.getElement().setId(id + "_none");

    btnSelectNone.addClickHandler(event -> {
      if (btnSelectNone.getElement().getId().equals(SELECT_TABLES + "_none")) {
        if (currentSchemaUUID != null) {
          MultiSelectionModel<ViewerTable> selectionModel = getTables(currentSchemaUUID).getSelectionModel();
          for (ViewerTable viewerTable : metadata.getSchema(currentSchemaUUID).getTables()) {
            selectionModel.setSelected(viewerTable, false);
          }
        }
      } else if (btnSelectNone.getElement().getId().equals(SELECT_COLUMNS + "_none")) {
        if (currentTableUUID != null) {
          MultiSelectionModel<ViewerColumn> selectionModel = getColumns(currentTableUUID).getSelectionModel();
          for (ViewerColumn viewerColumn : metadata.getTable(currentTableUUID).getColumns()) {
            selectionModel.setSelected(viewerColumn, false);
          }
        }
      }
    });

    FlowPanel panel = new FlowPanel();
    panel.addStyleName("select-buttons-container");
    panel.add(btnSelectAll);
    panel.add(btnSelectNone);

    return panel;
  }

  private void populateRowsForViewerTable(MultipleSelectionTablePanel<ViewerTable> viewerTableSelectionTablePanel,
    final ViewerSchema viewerSchema) {
    Label header = new Label(viewerSchema.getName());
    header.addStyleName("h4");

    viewerTableSelectionTablePanel.createTable(header, getSelectPanel(SELECT_TABLES),
      viewerSchema.getTables().iterator(), new MultipleSelectionTablePanel.ColumnInfo<>("", 15,
        new Column<ViewerTable, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerTable object) {
            MultipleSelectionTablePanel<ViewerColumn> viewerColumnTablePanel = getColumns(object.getUUID());
            if (viewerTableSelectionTablePanel.getSelectionModel().isSelected(object)) {
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
            return viewerTableSelectionTablePanel.getSelectionModel().isSelected(object);
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

  private void populateRowsForViewerColumn(MultipleSelectionTablePanel<ViewerColumn> columns, final ViewerTable table) {
    Label header = new Label(table.getName());
    header.addStyleName("h4");

    columns.createTable(header, getSelectPanel(SELECT_COLUMNS), table.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>("", 15,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn object) {
            MultipleSelectionTablePanel<ViewerTable> viewerTableMultipleSelectionTablePanel = getTables(
              table.getSchemaUUID());
            if (columns.getSelectionModel().isSelected(object)) {
              tableSelectedStatus.put(table.getUUID(), true);
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(table, true);
            }

            if (columns.getSelectionModel().getSelectedSet().size() == 0) {
              viewerTableMultipleSelectionTablePanel.getSelectionModel().setSelected(table, false);
              tableSelectedStatus.put(table.getUUID(), false);
            }
            return columns.getSelectionModel().isSelected(object);
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