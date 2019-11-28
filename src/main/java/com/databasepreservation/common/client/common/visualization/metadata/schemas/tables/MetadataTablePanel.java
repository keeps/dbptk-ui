package com.databasepreservation.common.client.common.visualization.metadata.schemas.tables;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
  private MetadataControlPanel controls;
  private ViewerSchema schema;
  private ViewerTable table;

  public static MetadataTablePanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String tableUUID) {
    String separator = "/";
    String code = database.getUuid() + separator + tableUUID;

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
    controls = MetadataControlPanel.getInstance(database.getUuid());
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
    description.setText( (table.getDescription()));
    description.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        table.setDescription( description.getText() );
        SIARDbundle.setTable(schema.getName(), table.getName(),description.getText());
        controls.validate();
      }
    });
    description.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        description.selectAll();
      }
    });

    tabPanel.add(new MetadataColumns(SIARDbundle, schema, table, controls).createTable(), messages.columns());
    tabPanel.add(new MetadataPrimaryKey(SIARDbundle, schema, table, controls).createTable(), messages.primaryKey());
    tabPanel.add(new MetadataForeignKeys(SIARDbundle, database, schema, table, controls).createTable(), messages.foreignKeys());
    tabPanel.add(new MetadataCandidateKeys(SIARDbundle, schema, table, controls).createTable(), messages.candidateKeys());
    tabPanel.add(new MetadataConstraints(SIARDbundle, schema, table, controls).createTable(),
      messages.menusidebar_checkConstraints());
    tabPanel.add(new MetadataTriggers(SIARDbundle, schema, table, controls).createTable(),
      messages.menusidebar_triggers());

    tabPanel.selectTab(0);
  }
}