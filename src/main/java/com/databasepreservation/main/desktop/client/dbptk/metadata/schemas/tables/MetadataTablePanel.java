package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
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
public class MetadataTablePanel extends MetadataPanel {
  interface MetadataTableUiBinder extends UiBinder<Widget, MetadataTablePanel> {
  }

  private static MetadataTableUiBinder uiBinder = GWT.create(MetadataTableUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  TextArea description;

  @UiField
  TabPanel tabPanel;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataTablePanel> instances = new HashMap<>();
  private ViewerSIARDBundle SIARDbundle;
  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerTable table;

  public static MetadataTablePanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String tableUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + tableUUID;

    MetadataTablePanel instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataTablePanel(database, tableUUID, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  private MetadataTablePanel(ViewerDatabase database, String tableUUID, ViewerSIARDBundle bundle) {
    this.database = database;
    this.SIARDbundle = bundle;
    table = database.getMetadata().getTable(tableUUID);
    schema = database.getMetadata().getSchemaFromTableUUID(tableUUID);

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

  }

  private void init() {
    Label tableName = new Label();
    tableName.setText(schema.getName() + "." + table.getName());
    mainHeader.setWidget(tableName);

    description.getElement().setAttribute("placeholder", messages.siardMetadata_DescriptionUnavailable());
    description.setText(table.getDescription());
    description.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        table.setDescription(description.getText());
        SIARDbundle.setTable(schema.getName(), table.getName(), description.getText());
        JavascriptUtils.alertUpdatedMetadata();
      }
    });

    tabPanel.add(new MetadataColumns(SIARDbundle, schema, table).createTable(), messages.columns());
    tabPanel.add(new MetadataPrimaryKey(SIARDbundle, schema, table).createTable(), messages.primaryKey());
    tabPanel.add(new MetadataForeignKeys(SIARDbundle, database, schema, table).createTable(), messages.foreignKeys());
    tabPanel.add(new MetadataCandidateKeys(SIARDbundle, schema, table).createTable(), messages.candidateKeys());
    tabPanel.add(new MetadataConstraints(SIARDbundle, schema, table).createTable(),
      messages.menusidebar_checkConstraints());
    tabPanel.add(new MetadataTriggers(SIARDbundle, schema, table).createTable(),
      messages.menusidebar_triggers());

    tabPanel.selectTab(0);
  }
}