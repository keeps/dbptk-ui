package com.databasepreservation.main.common.shared.client.common.visualization.browse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrimaryKey;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerReference;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.common.lists.BasicTablePanel;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaStructurePanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SchemaStructurePanel> instances = new HashMap<>();

  public static SchemaStructurePanel getInstance(ViewerDatabase database, String schemaUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + schemaUUID;
    instances.computeIfAbsent(code, k -> new SchemaStructurePanel(database, schemaUUID));
    return instances.get(code);
  }

  interface SchemaStructurePanelUiBinder extends UiBinder<Widget, SchemaStructurePanel> {
  }

  private static SchemaStructurePanelUiBinder uiBinder = GWT.create(SchemaStructurePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  FlowPanel contentItems;

  private SchemaStructurePanel(ViewerDatabase database, final String schemaUUID) {
    this.database = database;
    this.schema = database.getMetadata().getSchema(schemaUUID);

    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager
          .forDesktopSchemaStructure(database.getMetadata().getName(), database.getUUID(), schema.getName(), schema.getUUID()));
    } else {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager
          .forSchemaStructure(database.getMetadata().getName(), database.getUUID(), schema.getName(), schema.getUUID()));
    }
  }

  private void init() {
    CommonClientUtils.addSchemaInfoToFlowPanel(contentItems, schema);

    for (ViewerTable viewerTable : schema.getTables()) {
      BasicTablePanel<ViewerColumn> basicTablePanelForTableColumns = getBasicTablePanelForTableColumns(viewerTable);
      contentItems.add(basicTablePanelForTableColumns);
      basicTablePanelForTableColumns.handleScrollChanges();
      if (viewerTable.getForeignKeys() != null && !viewerTable.getForeignKeys().isEmpty()) {
        BasicTablePanel<ViewerForeignKey> basicTablePanelForTableForeignKeys = getBasicTablePanelForTableForeignKeys(
          viewerTable);
        contentItems.add(basicTablePanelForTableForeignKeys);
        basicTablePanelForTableForeignKeys.handleScrollChanges();
      }
    }
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    for (Widget widget : contentItems) {
      if (widget instanceof BasicTablePanel) {
        BasicTablePanel basicTablePanel = (BasicTablePanel) widget;
        basicTablePanel.setVisible(true);
      }
    }
  }

  private BasicTablePanel<ViewerForeignKey> getBasicTablePanelForTableForeignKeys(final ViewerTable table) {
    Label header = new Label(messages.foreignKeys());
    header.addStyleName("h5");

    return new BasicTablePanel<>(header, SafeHtmlUtils.EMPTY_SAFE_HTML, table.getForeignKeys().iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey foreignKey) {
          return foreignKey.getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.foreignKeys_referencedSchema(), 15, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey foreignKey) {
          return database.getMetadata().getTable(foreignKey.getReferencedTableUUID()).getSchemaName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.foreignKeys_referencedTable(), 15, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey foreignKey) {
          return database.getMetadata().getTable(foreignKey.getReferencedTableUUID()).getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(
        SafeHtmlUtils.fromSafeConstant(messages.mappingSourceToReferenced("<i class=\"fa fa-long-arrow-right\"></i>")),
        20, new Column<ViewerForeignKey, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ViewerForeignKey foreignKey) {
            ViewerTable referencedTable = database.getMetadata().getTable(foreignKey.getReferencedTableUUID());

            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            for (Iterator<ViewerReference> i = foreignKey.getReferences().iterator(); i.hasNext();) {
              ViewerReference reference = i.next();

              builder.appendEscaped(table.getColumns().get(reference.getSourceColumnIndex()).getDisplayName())
                .appendHtmlConstant(" <i class='fa fa-long-arrow-right'></i> ")
                .appendEscaped(referencedTable.getColumns().get(reference.getReferencedColumnIndex()).getDisplayName());

              if (i.hasNext()) {
                builder.appendHtmlConstant("<br/>");
              }
            }
            return builder.toSafeHtml();
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.foreignKeys_matchType(), 10, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey foreignKey) {
          return foreignKey.getMatchType();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.foreignKeys_updateAction(), 9, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey foreignKey) {
          return foreignKey.getUpdateAction();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.foreignKeys_deleteAction(), 9, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey foreignKey) {
          return foreignKey.getDeleteAction();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey foreignKey) {
          if (ViewerStringUtils.isNotBlank(foreignKey.getDescription())) {
            return foreignKey.getDescription();
          } else {
            return "";
          }
        }
      })

    );
  }

  private BasicTablePanel<ViewerColumn> getBasicTablePanelForTableColumns(ViewerTable table) {
    FlowPanel header = CommonClientUtils.getSchemaAndTableHeader(database.getUUID(), table, "h4");

    SafeHtmlBuilder infoBuilder = new SafeHtmlBuilder();
    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      infoBuilder.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
      infoBuilder.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">" + messages.description() + ": "));
      infoBuilder.append(SafeHtmlUtils.fromString(table.getDescription()));
      infoBuilder.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      infoBuilder.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    }

    // auxiliary
    final ViewerPrimaryKey pk = table.getPrimaryKey();
    final HashSet<Integer> columnIndexesWithForeignKeys = new HashSet<>();
    for (ViewerForeignKey viewerForeignKey : table.getForeignKeys()) {
      for (ViewerReference viewerReference : viewerForeignKey.getReferences()) {
        columnIndexesWithForeignKeys.add(viewerReference.getSourceColumnIndex());
      }
    }

    // create and return the table panel
    return new BasicTablePanel<>(header, infoBuilder.toSafeHtml(), table.getColumns().iterator(),

      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.EMPTY_SAFE_HTML, 2.2,
        new Column<ViewerColumn, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ViewerColumn column) {
            if (pk != null && pk.getColumnIndexesInViewerTable().contains(column.getColumnIndexInEnclosingTable())) {
              return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-key' title='" + messages.primaryKey() + "'></i>");
            } else if (columnIndexesWithForeignKeys.contains(column.getColumnIndexInEnclosingTable())) {
              return SafeHtmlUtils.fromSafeConstant(
                "<i class='fa fa-exchange' title='" + messages.foreignKeys_usedByAForeignKeyRelation() + "'></i>");
            } else {
              return SafeHtmlUtils.EMPTY_SAFE_HTML;
            }
          }
        }, "primary-key-col"),

      new BasicTablePanel.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          return column.getDisplayName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.typeName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          return column.getType().getTypeName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.originalTypeName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          return column.getType().getOriginalTypeName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.nullable(), 8, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          if (column.getNillable()) {
            return "Yes";
          } else {
            return "No";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          if (ViewerStringUtils.isNotBlank(column.getDescription())) {
            return column.getDescription();
          } else {
            return "";
          }
        }
      })

    );
  }
}
