package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
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
  interface MetadataTableUiBinder extends UiBinder<Widget, MetadataTable> {
  }

  private static MetadataTableUiBinder uiBinder = GWT.create(MetadataTableUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  TextArea description;

  @UiField
  TabPanel tabPanel;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataTable> instances = new HashMap<>();
  private ViewerSIARDBundle SIARDbundle;
  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerTable table;

  public static MetadataTable getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String tableUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + tableUUID;

    MetadataTable instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataTable(database, tableUUID, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  private MetadataTable(ViewerDatabase database, String tableUUID, ViewerSIARDBundle bundle) {
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
    Label tableName = new Label();
    tableName.setText(table.getName());
    mainHeader.setWidget(tableName);

    description.setText(
      table.getDescription() == null ? messages.siardMetadata_DescriptionUnavailable() : table.getDescription());
    description.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        table.setDescription(description.getText());
        SIARDbundle.setTable(schema.getName(), table.getName(), description.getText());
      }
    });

    tabPanel.add(new MetadataColumns(SIARDbundle).createTable(table, schema), messages.columns());
    tabPanel.add(new MetadataPrimaryKey(SIARDbundle, database).createTable(table, schema), messages.primaryKey());
    tabPanel.add(new MetadataForeignKeys(SIARDbundle, database).createTable(table, schema), messages.foreignKeys());
    tabPanel.add(new MetadataCandidateKeys(SIARDbundle, database).createTable(table, schema), messages.candidateKeys());
    tabPanel.add(new MetadataConstraints(SIARDbundle, database).createTable(table, schema),
      messages.menusidebar_checkConstraints());
    tabPanel.add(new MetadataTriggers(SIARDbundle, database).createTable(table, schema),
      messages.menusidebar_triggers());

    tabPanel.selectTab(0);
  }
}