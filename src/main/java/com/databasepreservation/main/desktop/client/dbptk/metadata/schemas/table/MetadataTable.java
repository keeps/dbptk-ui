package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.table;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataTable extends MetadataPanel {
  interface EditMetadataSchemeUiBinder extends UiBinder<Widget, MetadataTable> {
  }

  private static EditMetadataSchemeUiBinder uiBinder = GWT.create(EditMetadataSchemeUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  TextArea description;

  @UiField
  TabPanel tabPanel;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataTable> instances = new HashMap<>();
  private Map<String, String> SIARDbundle;
  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerTable table;

  public static MetadataTable getInstance(ViewerDatabase database, Map<String, String> SIARDbundle, String tableUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + tableUUID;

    MetadataTable instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataTable(database, tableUUID, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  private MetadataTable(ViewerDatabase database, String tableUUID, Map<String, String> bundle) {
    this.database = database;
    this.SIARDbundle = bundle;
    table = database.getMetadata().getTable(tableUUID);
    schema = database.getMetadata().getSchemaFromTableUUID(tableUUID);

    GWT.log("MetadataTable::" + table.getName());

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

  }

  private void init() {
    GWT.log("databaseUUID: " + database.getUUID());
    Label tableName = new Label();
    tableName.setText(table.getName());
    mainHeader.setWidget(tableName);

    description.setText(table.getDescription());
    description.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        table.setDescription(description.getText());
        SIARDbundle.put("schema:" + schema.getName() + "---" + "table:" + table.getName(),
          "description---" + description.getText());
      }
    });

    tabPanel.add(new MetadataColumns(SIARDbundle).createTable(table, schema), messages.columnName());

    if (table.getPrimaryKey() != null) {
      tabPanel.add(new MetadataPrimaryKey(SIARDbundle, database).createTable(table, schema), messages.primaryKey());
    }

    if (!table.getForeignKeys().isEmpty()) {
      tabPanel.add(new MetadataForeignKeys(SIARDbundle, database).createTable(table, schema), messages.foreignKeys());
    }

    if (!table.getCheckConstraints().isEmpty()) {
      tabPanel.add(new MetadataConstraints(SIARDbundle, database).createTable(table, schema),
        messages.menusidebar_checkConstraints());
    }

    if (!table.getTriggers().isEmpty()) {
      tabPanel.add(new MetadataTriggers(SIARDbundle, database).createTable(table, schema),
        messages.menusidebar_triggers());
    }

    tabPanel.selectTab(0);
  }
}