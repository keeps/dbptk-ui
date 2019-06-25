package com.databasepreservation.main.visualization.client.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.visualization.client.common.lists.BasicTablePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaDataPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SchemaDataPanel> instances = new HashMap<>();

  public static SchemaDataPanel getInstance(ViewerDatabase database, String schemaUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + schemaUUID;

    SchemaDataPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaDataPanel(database, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface SchemaDataPanelUiBinder extends UiBinder<Widget, SchemaDataPanel> {
  }

  private static SchemaDataPanelUiBinder uiBinder = GWT.create(SchemaDataPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  FlowPanel contentItems;

  private SchemaDataPanel(ViewerDatabase viewerDatabase, final String schemaUUID) {
    database = viewerDatabase;
    schema = database.getMetadata().getSchema(schemaUUID);

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSchemaData(database.getMetadata().getName(),
      database.getUUID(), schema.getName(), schema.getUUID()));
  }

  private void init() {
    CommonClientUtils.addSchemaInfoToFlowPanel(contentItems, schema);

    contentItems.add(ErDiagram.getInstance(database, schema));

    final BasicTablePanel<ViewerTable> table = getBasicTablePanelForTableInfo(database.getMetadata(), schema);
    table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        ViewerTable item = table.getSelectionModel().getSelectedObject();
        if (item != null) {
          HistoryManager.gotoTable(database.getUUID(), item.getUUID());
        }
      }
    });
    contentItems.add(table);
  }

  private BasicTablePanel<ViewerTable> getBasicTablePanelForTableInfo(final ViewerMetadata metadata,
    final ViewerSchema schema) {

    List<ViewerTable> tables = new ArrayList<>(schema.getTables());

    Collections.sort(tables, new Comparator<ViewerTable>() {
      @Override
      public int compare(ViewerTable o1, ViewerTable o2) {
        Long r1 = o1.getCountRows();
        Long r2 = o2.getCountRows();
        return r2.compareTo(r1);
      }
    });

    return new BasicTablePanel<>(new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML),
      new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML), tables.iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.schema_tableName(), 17, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return table.getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.schema_numberOfRows(), 7, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return String.valueOf(table.getCountRows());
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.schema_numberOfColumns(), 8, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return String.valueOf(table.getColumns().size());
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          if (table.getDescription() != null) {
            return table.getDescription();
          } else {
            return "";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.schema_relationsOut(), 7, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          int outboundForeignKeys = table.getForeignKeys().size();
          if (outboundForeignKeys > 0) {
            return String.valueOf(outboundForeignKeys);
          } else {
            return "none";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.schema_relationsIn(), 7, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          int inboundForeignKeys = 0;
          for (ViewerSchema viewerSchema : metadata.getSchemas()) {
            for (ViewerTable viewerTable : viewerSchema.getTables()) {
              for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {
                if (viewerForeignKey.getReferencedTableUUID().equals(table.getUUID())) {
                  inboundForeignKeys++;
                }
              }
            }
          }

          if (inboundForeignKeys > 0) {
            return String.valueOf(inboundForeignKeys);
          } else {
            return "none";
          }
        }
      })

    );
  }
}
