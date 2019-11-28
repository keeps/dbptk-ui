package com.databasepreservation.common.client.common.visualization.browse.table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.common.lists.BasicTablePanel;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableForeignKeysPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, TableForeignKeysPanel> instances = new HashMap<>();

  public static TableForeignKeysPanel getInstance(ViewerDatabase database, ViewerTable table) {
    TableForeignKeysPanel instance = instances.get(table.getUUID());
    if (instance == null) {
      instance = new TableForeignKeysPanel(database, table);
      instances.put(table.getUUID(), instance);
    }
    return instance;
  }

  interface SchemaTriggersPanelUiBinder extends UiBinder<Widget, TableForeignKeysPanel> {
  }

  private static SchemaTriggersPanelUiBinder uiBinder = GWT.create(SchemaTriggersPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;

  @UiField
  FlowPanel contentItems;

  private TableForeignKeysPanel(ViewerDatabase database, ViewerTable table) {
    this.database = database;
    this.table = table;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  private void init() {
    contentItems.add(getBasicTablePanelForTableForeignKeys(table));
  }

  private BasicTablePanel<ViewerForeignKey> getBasicTablePanelForTableForeignKeys(final ViewerTable table) {
    Label header = new Label(messages.foreignKeys());
    header.addStyleName("h5");

    return new BasicTablePanel<ViewerForeignKey>(header, SafeHtmlUtils.EMPTY_SAFE_HTML,
      table.getForeignKeys().iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.schemaStructurePanelHeaderTextForForeignKeyName(), 15,
        new TextColumn<ViewerForeignKey>() {
          @Override
          public String getValue(ViewerForeignKey foreignKey) {
            return foreignKey.getName();
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
        false,20, new Column<ViewerForeignKey, SafeHtml>(new SafeHtmlCell()) {
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
      }));
  }
}
