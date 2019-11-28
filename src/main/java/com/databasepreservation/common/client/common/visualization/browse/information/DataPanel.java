package com.databasepreservation.common.client.common.visualization.browse.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.common.lists.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DataPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DataPanel> instances = new HashMap<>();

  public static DataPanel getInstance(ViewerDatabase database, String schemaUUID) {
    String separator = "/";
    String code = database.getUuid() + separator + schemaUUID;
    return instances.computeIfAbsent(code, k -> new DataPanel(database, schemaUUID));
  }

  interface SchemaDataPanelUiBinder extends UiBinder<Widget, DataPanel> {
  }

  private static SchemaDataPanelUiBinder uiBinder = GWT.create(SchemaDataPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;
  private boolean advancedMode;

  @UiField
  FlowPanel contentItems;

  @UiField
  FlowPanel tableContent;

  @UiField
  SimplePanel schemaDescription;

  private DataPanel(ViewerDatabase viewerDatabase, final String schemaUUID) {
    database = viewerDatabase;
    schema = database.getMetadata().getSchema(schemaUUID);

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  public void reload(boolean advancedMode) {
    if (this.advancedMode != advancedMode) {
      this.advancedMode = advancedMode;
      tableContent.clear();
      initTableContent();
    }
  }

  private void init() {
    advancedMode = false;

    if (schema.getDescription() != null) {
      schemaDescription.addStyleName("multi-schema-description");
      schemaDescription.setWidget(CommonClientUtils.getPanelInformation(messages.description(), schema.getDescription(), "metadata-information-element-value"));
    }

    contentItems.add(ErDiagram.getInstance(database, schema));
    initTableContent();
  }

  private void initTableContent() {
    final BasicTablePanel<ViewerTable> table = getBasicTablePanelForTableInfo(database.getMetadata(), schema);
    table.getSelectionModel().addSelectionChangeHandler(event -> {
      ViewerTable item = table.getSelectionModel().getSelectedObject();
      if (item != null) {
        HistoryManager.gotoTable(database.getUuid(), item.getUUID());
      }
    });
    tableContent.add(table);
  }

  private BasicTablePanel<ViewerTable> getBasicTablePanelForTableInfo(final ViewerMetadata metadata,
    final ViewerSchema schema) {

    List<ViewerTable> tables = new ArrayList<>(schema.getTables());

    tables.sort((o1, o2) -> {
      Long r1 = o1.getCountRows();
      Long r2 = o2.getCountRows();
      return r2.compareTo(r1);
    });

    return new BasicTablePanel<ViewerTable>(new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML),
      new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML), tables.iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.schema_tableName(), false, 17, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return table.getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), false, 35, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          if (table.getDescription() != null) {
            return table.getDescription();
          } else {
            return "";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.schema_numberOfRows(), !advancedMode, 7, new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return String.valueOf(table.getCountRows());
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.schema_numberOfColumns(), !advancedMode, 8,
        new TextColumn<ViewerTable>() {
        @Override
        public String getValue(ViewerTable table) {
          return String.valueOf(table.getColumns().size());
        }
      }),


      new BasicTablePanel.ColumnInfo<>(messages.schema_relationsOut(), !advancedMode, 7, new TextColumn<ViewerTable>() {
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

      new BasicTablePanel.ColumnInfo<>(messages.schema_relationsIn(), !advancedMode, 7, new TextColumn<ViewerTable>() {
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
