package com.databasepreservation.main.desktop.client.dbptk.metadata;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrimaryKey;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class EditMetadataTable extends MetadataRightPanel {
  interface EditMetadataSchemeUiBinder extends UiBinder<Widget, EditMetadataTable> {
  }

  private static EditMetadataSchemeUiBinder uiBinder = GWT.create(EditMetadataSchemeUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  HTML description;

  @UiField
  TabPanel tabPanel;

  // @UiField
  // SimplePanel tableContainer;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, EditMetadataTable> instances = new HashMap<>();
  private Map<String, String> SIARDbundle;
  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerTable table;

  public static EditMetadataTable getInstance(ViewerDatabase database, Map<String, String> SIARDbundle,
    String tableUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + tableUUID;

    EditMetadataTable instance = instances.get(code);
    if (instance == null) {
      instance = new EditMetadataTable(database, tableUUID, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  private EditMetadataTable(ViewerDatabase database, String tableUUID, Map<String, String> bundle) {
    this.database = database;
    this.SIARDbundle = bundle;
    table = database.getMetadata().getTable(tableUUID);
    schema = database.getMetadata().getSchemaFromTableUUID(tableUUID);

    GWT.log("EditMetadataTable::" + table.getName());

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

  }

  private void init() {
    GWT.log("databaseUUID: " + database.getUUID());
    mainHeader.setWidget(CommonClientUtils.getSchemaAndTableHeader(database.getUUID(), table, "h1"));

    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      description.setHTML(CommonClientUtils.getFieldHTML(messages.description(), table.getDescription()));
    }

    tabPanel.add(getMetadataEditTableForColumns(table.getColumns()), messages.columnName());
    tabPanel.add(getMetadataEditTableForPrimaryKeys(table.getPrimaryKey()), messages.primaryKey());
    tabPanel.selectTab(0);
  }

  private MetadataTableList getMetadataEditTableForColumns(List<ViewerColumn> columns) {

    Label header = new Label(messages.titleUsers());
    HTMLPanel info = new HTMLPanel("");

    Column<ViewerColumn, String> description = new Column<ViewerColumn, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerColumn object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), object.getDisplayName(), "column", value);
    });

    MetadataTableList columnsMetadata = new MetadataTableList<>(header, info, columns.iterator(),
      new MetadataTableList.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getDisplayName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.typeName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getType().getTypeName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.originalTypeName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getType().getOriginalTypeName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.nullable(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getNillable() ? "YES" : "NO";
        }
      }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, description));

    return columnsMetadata;
  }

  private MetadataTableList getMetadataEditTableForPrimaryKeys(ViewerPrimaryKey item) {

    Label header = new Label(messages.titleUsers());
    HTMLPanel info = new HTMLPanel("");

    Column<ViewerPrimaryKey, String> description = new Column<ViewerPrimaryKey, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerPrimaryKey object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), object.getName(), "primaryKey", value);
    });

    MetadataTableList columnsMetadata = new MetadataTableList<>(header, info, Arrays.asList(item).iterator(),
      new MetadataTableList.ColumnInfo<>(messages.primaryKey(), 15, new TextColumn<ViewerPrimaryKey>() {
        @Override
        public String getValue(ViewerPrimaryKey object) {
          Integer columnIndex = object.getColumnIndexesInViewerTable().get(0);
          return table.getColumns().get(columnIndex).getDisplayName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, description));

    return columnsMetadata;
  }

  private void updateSIARDbundle(String schemaName, String tableName, String displayName, String type,  String value) {
    SIARDbundle.put("schema:" + schemaName + "---" + "table:" + tableName + "---" + type + ":" + displayName,
      "description---" + value);
  }
}