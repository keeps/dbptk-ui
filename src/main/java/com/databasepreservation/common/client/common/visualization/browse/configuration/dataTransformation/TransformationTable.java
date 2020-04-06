package com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.models.configuration.denormalization.ColumnWrapper;
import com.databasepreservation.common.client.models.configuration.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransformationTable extends Composite {
  interface TransformationTableUiBinder extends UiBinder<Widget, TransformationTable> {

  }

  private static TransformationTableUiBinder binder = GWT.create(TransformationTableUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, TransformationTable> instances = new HashMap<>();
  private DenormalizeConfiguration denormalizeConfiguration;
  private ViewerDatabase database;
  private ViewerTable table;
  private List<ColumnWrapper> originalColumns = new ArrayList<>();

  @UiField
  FlowPanel content;

  /**
   *
   * @param database
   * @param table
   * @param configuration
   * @return
   */
  public static TransformationTable getInstance(ViewerDatabase database, ViewerTable table,
    DenormalizeConfiguration configuration) {
    return instances.computeIfAbsent(database.getUuid() + table.getUuid(),
      k -> new TransformationTable(database, table, configuration));
  }

  /**
   *
   * @param database
   * @param table
   * @param configuration
   */
  public TransformationTable(ViewerDatabase database, ViewerTable table, DenormalizeConfiguration configuration) {
    initWidget(binder.createAndBindUi(this));
    this.denormalizeConfiguration = configuration;
    this.database = database;
    this.table = table;
    createTable();
  }

  /**
   *
   */
  private void createTable() {
    for (ViewerColumn column : table.getColumns()) {
      ColumnWrapper columnWrapper = new ColumnWrapper(table.getName(), denormalizeConfiguration,
        database.getMetadata());
      columnWrapper.setColumnDisplayName(column.getDisplayName());
      columnWrapper.setColumnDescription(column.getDescription());
      originalColumns.add(columnWrapper);
    }
    drawTable(originalColumns);
  }

  public void redrawTable(DenormalizeConfiguration denormalizeConfiguration) {
    List<ColumnWrapper> columns = new ArrayList<>(originalColumns);
    for (RelatedTablesConfiguration relatedTable : denormalizeConfiguration.getRelatedTables()) {
      setColumnsToInclude(relatedTable, columns);
    }

    drawTable(columns);
  }

  private void setColumnsToInclude(RelatedTablesConfiguration relatedTable, List<ColumnWrapper> columns) {
    ViewerTable referencedTable = database.getMetadata().getTable(relatedTable.getTableUUID());
    ColumnWrapper columnWrapper = new ColumnWrapper(relatedTable.getUuid(), referencedTable.getName(), relatedTable,
      denormalizeConfiguration, database.getMetadata());
    for (RelatedColumnConfiguration columnToInclude : relatedTable.getColumnsIncluded()) {
      ViewerColumn col = referencedTable.getColumns().get(columnToInclude.getIndex());
      columnWrapper.setColumnDisplayName(col.getDisplayName());
      columnWrapper.setColumnDescription(col.getDescription());
    }
    if (columnWrapper.getColumnDisplayName() != null) {
      columns.add(columnWrapper);
    }

    for (RelatedTablesConfiguration innerTable : relatedTable.getRelatedTables()) {
      setColumnsToInclude(innerTable, columns);
    }
  }

  private void drawTable(List<ColumnWrapper> columns) {
    BasicTablePanel<ColumnWrapper> tablePanel = new BasicTablePanel<ColumnWrapper>(new FlowPanel(),
      SafeHtmlUtils.EMPTY_SAFE_HTML, columns.iterator(),
      new BasicTablePanel.ColumnInfo<ColumnWrapper>("", 3, new Column<ColumnWrapper, SafeHtml>(new SafeHtmlCell()) {

        @Override
        public SafeHtml getValue(ColumnWrapper columnWrapper) {
          if (columnWrapper.getReferencedTableName().equals(table.getName())) {
            return SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.COLUMN));
          }
          return SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE));
        }
      }), new BasicTablePanel.ColumnInfo<ColumnWrapper>(messages.columnName(), 35, new TextColumn<ColumnWrapper>() {
        @Override
        public void render(Cell.Context context, ColumnWrapper object, SafeHtmlBuilder sb) {
          if (object.getReferencedTableName().equals(table.getName())) {
            super.render(context, object, sb);
          } else {
            SafeHtml path = object.createPath();
            sb.append(path);
          }
        }

        @Override
        public String getValue(ColumnWrapper object) {
          return object.getColumnDisplayName();
        }
      }), new BasicTablePanel.ColumnInfo<ColumnWrapper>(messages.basicTableHeaderDescription(), 0,
        new TextColumn<ColumnWrapper>() {
          @Override
          public String getValue(ColumnWrapper object) {
            return object.getColumnDescription();
          }
        }));

    content.clear();
    tablePanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
        ColumnWrapper selectedObject = tablePanel.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          if (selectedObject.getUuid() != null) {
            GWT.log(selectedObject.getUuid());
            JavascriptUtils.scrollToElement(Document.get().getElementById(selectedObject.getUuid()));
          }
          tablePanel.getSelectionModel().clear();
        }
      }
    });
    content.add(tablePanel);
  }

  public ViewerTable getTable() {
    return table;
  }

  public static void clear() {
    instances.clear();
  }
}
