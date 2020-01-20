package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.lists.widgets.MetadataTableList;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.models.status.denormalization.ColumnWrapper;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
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
      originalColumns.add(new ColumnWrapper(table.getName(), column));
    }
    drawTable(originalColumns);
  }

  /**
   *
   */
  public void redrawTable() {
    List<ColumnWrapper> columns = new ArrayList<>(originalColumns);
    for (RelatedTablesConfiguration relatedTable : denormalizeConfiguration.getRelatedTables()) {
      setColumnsToInclude(relatedTable, columns);
    }

    drawTable(columns);
  }

  private void setColumnsToInclude(RelatedTablesConfiguration relatedTable, List<ColumnWrapper> columns) {
    for (RelatedColumnConfiguration columnToInclude : relatedTable.getColumnsIncluded()) {
      ViewerTable referencedTable = database.getMetadata().getTable(relatedTable.getTableUUID());
      ViewerColumn col = referencedTable.getColumns().get(columnToInclude.getIndex());
      columns.add(new ColumnWrapper(relatedTable.getUuid(), referencedTable.getName(), col));
    }

    for (RelatedTablesConfiguration innerTable : relatedTable.getRelatedTables()) {
      setColumnsToInclude(innerTable, columns);
    }
  }

  private void drawTable(List<ColumnWrapper> columns) {
    MetadataTableList<ColumnWrapper> tablePanel;
    if (columns.isEmpty()) {
      tablePanel = new MetadataTableList<>(messages.tableDoesNotContainColumns());
    } else {
      tablePanel = new MetadataTableList<ColumnWrapper>(columns.iterator(),
        new MetadataTableList.ColumnInfo<>("", 1.2, new Column<ColumnWrapper, SafeHtml>(new SafeHtmlCell()) {

          @Override
          public SafeHtml getValue(ColumnWrapper columnWrapper) {
            if (columnWrapper.getReferencedTableName().equals(table.getName())) {
              return SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
            }
            return SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE));
          }
        }), new MetadataTableList.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ColumnWrapper>() {

          @Override
          public void render(Cell.Context context, ColumnWrapper object, SafeHtmlBuilder sb) {
            if (object.getReferencedTableName().equals(table.getName())) {
              super.render(context, object, sb);
            } else {
              String value = getValue(object);
              sb.appendHtmlConstant("<span class=\"table-item-link\">");
              if (value != null) {
                sb.append(SafeHtmlUtils.fromString(value));
              }
              sb.appendHtmlConstant("</span>");
              sb.appendHtmlConstant(" <i class=\"fas fa-caret-right table-item-link\"></i> ");
              sb.appendHtmlConstant("<span class=\"table-item-link\">");
              sb.append(SafeHtmlUtils.fromString( object.getReferencedTableName()));
              sb.appendHtmlConstant("</span>");
            }
          }

          @Override
          public String getValue(ColumnWrapper object) {
            return object.getColumn().getDisplayName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 25, new TextColumn<ColumnWrapper>() {
          @Override
          public String getValue(ColumnWrapper object) {
            return object.getColumn().getDescription();
          }
        }));
    }
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
