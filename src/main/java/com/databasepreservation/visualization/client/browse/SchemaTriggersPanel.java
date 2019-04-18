package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTrigger;
import com.databasepreservation.visualization.client.common.lists.BasicTablePanel;
import com.databasepreservation.visualization.client.common.utils.CommonClientUtils;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaTriggersPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SchemaTriggersPanel> instances = new HashMap<>();

  public static SchemaTriggersPanel getInstance(ViewerDatabase database, String schemaUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + schemaUUID;

    SchemaTriggersPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaTriggersPanel(database, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface SchemaTriggersPanelUiBinder extends UiBinder<Widget, SchemaTriggersPanel> {
  }

  private static SchemaTriggersPanelUiBinder uiBinder = GWT.create(SchemaTriggersPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  FlowPanel contentItems;

  private SchemaTriggersPanel(ViewerDatabase viewerDatabase, final String schemaUUID) {
    database = viewerDatabase;
    schema = database.getMetadata().getSchema(schemaUUID);

    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSchemaTriggers(database.getMetadata().getName(),
      database.getUUID(), schema.getName(), schema.getUUID()));
  }

  private void init() {
    CommonClientUtils.addSchemaInfoToFlowPanel(contentItems, schema);

    boolean atLeastOneTrigger = false;
    for (ViewerTable viewerTable : schema.getTables()) {
      if (!viewerTable.getTriggers().isEmpty()) {
        atLeastOneTrigger = true;
        break;
      }
    }

    if (atLeastOneTrigger) {
      for (ViewerTable viewerTable : schema.getTables()) {
        if (!viewerTable.getTriggers().isEmpty()) {
          contentItems.add(getBasicTablePanelForTableTriggers(viewerTable));
        }
      }
    } else {
      Label noTriggers = new Label(messages.thisSchemaDoesNotHaveAnyTriggers());
      noTriggers.addStyleName("strong");
      contentItems.add(noTriggers);
    }
  }

  private BasicTablePanel<ViewerTrigger> getBasicTablePanelForTableTriggers(final ViewerTable table) {
    Label header = new Label(messages.triggersInTable(table.getName()));
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    return new BasicTablePanel<>(header, info, table.getTriggers().iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          return viewerTrigger.getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.triggeredAction(), 25,
        new Column<ViewerTrigger, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ViewerTrigger viewerTrigger) {
            return SafeHtmlUtils.fromSafeConstant(SafeHtmlUtils
              .htmlEscape(viewerTrigger.getTriggeredAction().replace("\\u0020", " ")).replace("\\n", "<br/>"));
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.actionTime(), 8, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          return viewerTrigger.getActionTime();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.triggerEvent(), 8, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          return viewerTrigger.getTriggerEvent();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.aliasList(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          if (ViewerStringUtils.isNotBlank(viewerTrigger.getAliasList())) {
            return viewerTrigger.getAliasList();
          } else {
            return "";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          if (ViewerStringUtils.isNotBlank(viewerTrigger.getDescription())) {
            return viewerTrigger.getDescription();
          } else {
            return "";
          }
        }
      })

    );
  }
}
