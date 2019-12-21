package com.databasepreservation.common.client.common.visualization.browse.configuration;

import com.databasepreservation.common.client.common.lists.widgets.MetadataTableList;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DenormalizeConfigurationHandler;
import com.databasepreservation.common.client.models.configuration.denormalize.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransformationTable extends Composite {
  interface TransformationTableUiBinder extends UiBinder<Widget, TransformationTable> {

  }
  private static TransformationTableUiBinder binder = GWT.create(TransformationTableUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, TransformationTable> instances = new HashMap<>();
  private DenormalizeConfigurationHandler configuration;
  private ViewerDatabase database;
  private ViewerTable table;

  @UiField
  FlowPanel content;

  /**
   * 
   * @param configuration
   * @return
   */
  public static TransformationTable getInstance(ViewerDatabase database, ViewerTable table, DenormalizeConfigurationHandler configuration) {
    return instances.computeIfAbsent(database.getUuid() + table.getUuid(), k -> new TransformationTable(database, table, configuration));
  }

  /**
   * 
   * @param table
   * @param configuration
   */
  public TransformationTable(ViewerDatabase database, ViewerTable table, DenormalizeConfigurationHandler configuration) {
    initWidget(binder.createAndBindUi(this));
    this.configuration = configuration;
    this.database = database;
    this.table = table;
    createTable();
  }

  /**
   *
   */
  private void createTable() {
    drawTable(table.getColumns());
  }

  /**
   *
   */
  public void redrawTable(){
    List<ViewerColumn> columns = new ArrayList<>(table.getColumns());
    List<RelatedTablesConfiguration> relatadTableList = configuration.getRelatedTableList();

    for(RelatedTablesConfiguration relatadTable : relatadTableList){
      for(RelatedColumnConfiguration columnToInclude : relatadTable.getColumnsIncluded()){
        ViewerTable referencedTable = database.getMetadata().getTable(relatadTable.getTableUUID());
        ViewerColumn col = referencedTable.getColumns().get(columnToInclude.getIndex());
        columns.add(col);
      }
    }

    drawTable(columns);
  }

  /**
   *
   * @param columns
   */
  private void drawTable(List<ViewerColumn> columns) {
    MetadataTableList<ViewerColumn> tablePanel;
    if (columns.isEmpty()) {
      tablePanel = new MetadataTableList<>(messages.tableDoesNotContainColumns());
    } else {
      tablePanel = new MetadataTableList<>(columns.iterator(),
          new MetadataTableList.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ViewerColumn>() {
            @Override
            public String getValue(ViewerColumn object) {
              return object.getDisplayName();
            }
          }), new MetadataTableList.ColumnInfo<>(messages.description(), 25, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getDescription();
        }
      }));
    }
    content.clear();
    content.add(tablePanel);
  }

}
