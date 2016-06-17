package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerPrimaryKey;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerReference;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.common.lists.BasicTablePanel;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.dbviewer.client.common.utils.CommonClientUtils;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.dbviewer.shared.client.Tools.HistoryManager;
import com.databasepreservation.dbviewer.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaStructurePanel extends Composite {
  private static Map<String, SchemaStructurePanel> instances = new HashMap<>();

  public static SchemaStructurePanel getInstance(String databaseUUID, String schemaUUID) {
    String separator = "/";
    String code = databaseUUID + separator + schemaUUID;

    SchemaStructurePanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaStructurePanel(databaseUUID, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, SchemaStructurePanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;
  @UiField
  FlowPanel contentItems;

  private SchemaStructurePanel(final String databaseUUID, final String schemaUUID) {
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingSchema(databaseUUID, schemaUUID));

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          schema = database.getMetadata().getSchema(schemaUUID);
          init();
        }
      });
  }

  private void init() {
    // breadcrumb
    BreadcrumbManager.updateBreadcrumb(
      breadcrumb,
      BreadcrumbManager.forSchema(database.getMetadata().getName(), database.getUUID(), schema.getName(),
        schema.getUUID()));

    CommonClientUtils.addSchemaInfoToFlowPanel(contentItems, schema);

    // Tables and their information
    Label tablesHeader = new Label("Tables");
    tablesHeader.addStyleName("h2");
    contentItems.add(tablesHeader);

    for (ViewerTable viewerTable : schema.getTables()) {
      contentItems.add(getBasicTablePanelForTableColumns(viewerTable));
      if (viewerTable.getForeignKeys() != null && viewerTable.getForeignKeys().size() > 0) {
        contentItems.add(getBasicTablePanelForTableForeignKeys(viewerTable));
      }
    }

  }

  private BasicTablePanel<ViewerForeignKey> getBasicTablePanelForTableForeignKeys(final ViewerTable table) {
    Label header = new Label("Foreign Keys");
    header.addStyleName("h4");

    return new BasicTablePanel<>(header, SafeHtmlUtils.EMPTY_SAFE_HTML, table.getForeignKeys().iterator(),

    new BasicTablePanel.ColumnInfo<>("Name", 15, new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Referenced Schema", 15, new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return database.getMetadata().getTable(foreignKey.getReferencedTableUUID()).getSchemaName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Referenced Table", 15, new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return database.getMetadata().getTable(foreignKey.getReferencedTableUUID()).getName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>(
      SafeHtmlUtils.fromSafeConstant("Mapping (Source <i class=\"fa fa-arrow-right\"></i> Referenced)"), 20,
      new Column<ViewerForeignKey, SafeHtml>(new SafeHtmlCell()) {
        @Override
        public SafeHtml getValue(ViewerForeignKey foreignKey) {
          ViewerTable referencedTable = database.getMetadata().getTable(foreignKey.getReferencedTableUUID());

          SafeHtmlBuilder builder = new SafeHtmlBuilder();
          for (Iterator<ViewerReference> i = foreignKey.getReferences().iterator(); i.hasNext();) {
            ViewerReference reference = i.next();

            builder.appendEscaped(table.getColumns().get(reference.getSourceColumnIndex()).getDisplayName())
              .appendHtmlConstant(" <i class='fa fa-arrow-right'></i> ")
              .appendEscaped(referencedTable.getColumns().get(reference.getReferencedColumnIndex()).getDisplayName());

            if (i.hasNext()) {
              builder.appendHtmlConstant("<br/>");
            }
          }
          return builder.toSafeHtml();
        }
      }),

    new BasicTablePanel.ColumnInfo<>("Match type", 10, new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getMatchType();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Update action", 9, new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getUpdateAction();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Delete action", 9, new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getDeleteAction();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerForeignKey>() {
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
    Hyperlink header = new Hyperlink("Table: " + table.getName(), HistoryManager.linkToTable(database.getUUID(),
      table.getUUID()));
    header.addStyleName("h3");

    SafeHtmlBuilder infoBuilder = new SafeHtmlBuilder();
    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      infoBuilder.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
      infoBuilder.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">Description: "));
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

    new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.EMPTY_SAFE_HTML, 2.2, new Column<ViewerColumn, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerColumn column) {
        if (pk.getColumnIndexesInViewerTable().contains(column.getColumnIndexInEnclosingTable())) {
          return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-key' title='Primary Key'></i>");
        } else if (columnIndexesWithForeignKeys.contains(column.getColumnIndexInEnclosingTable())) {
          return SafeHtmlUtils
            .fromSafeConstant("<i class='fa fa-exchange' title='Used by a Foreign Key relation'></i>");
        } else {
          return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
      }
    }, "primary-key-col"),

    new BasicTablePanel.ColumnInfo<>("column name", 15, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getDisplayName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Type name", 15, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getType().getTypeName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Original type name", 15, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getType().getOriginalTypeName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Nullable", 8, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        if (column.getNillable()) {
          return "Yes";
        } else {
          return "No";
        }
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerColumn>() {
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
