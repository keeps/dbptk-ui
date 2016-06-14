package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.dbviewer.shared.client.Tools.ViewerStringUtils;
import com.databasepreservation.dbviewer.shared.client.widgets.MyCellTableResources;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaPanel extends Composite {
  private static Map<String, SchemaPanel> instances = new HashMap<>();

  public static SchemaPanel getInstance(String databaseUUID, String schemaUUID) {
    String separator = "/";
    String code = databaseUUID + separator + schemaUUID;

    SchemaPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaPanel(databaseUUID, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, SchemaPanel> {
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

  private SchemaPanel(final String databaseUUID, final String schemaUUID) {
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

  private Hyperlink getHyperlink(String display_text, String database_uuid, String table_uuid) {
    Hyperlink link = new Hyperlink(display_text, HistoryManager.linkToTable(database_uuid, table_uuid));
    return link;
  }

  private void init() {
    // breadcrumb
    BreadcrumbManager.updateBreadcrumb(
      breadcrumb,
      BreadcrumbManager.forSchema(database.getMetadata().getName(), database.getUUID(), schema.getName(),
        schema.getUUID()));

    // Tables and their information
    contentItems.add(new HTMLPanel(getFieldHTML("Schema name", schema.getName())));
    if (ViewerStringUtils.isNotBlank(schema.getDescription())) {
      contentItems.add(new HTMLPanel(getFieldHTML("Schema description", schema.getDescription())));
    }

    Label tablesHeader = new Label("Tables");
    tablesHeader.addStyleName("h2");
    contentItems.add(tablesHeader);

    for (ViewerTable viewerTable : schema.getTables()) {
      contentItems.add(new HTMLPanel(getTableDescriptionItemHTML(viewerTable)));

      contentItems.add(new ScrollPanel(getColumnsInfoTable(viewerTable)));

      if (viewerTable.getForeignKeys() != null && viewerTable.getForeignKeys().size() > 0) {
        Label infoForeignKeysHeader = new Label("Foreign Keys");
        infoForeignKeysHeader.addStyleName("h4");
        contentItems.add(infoForeignKeysHeader);
        contentItems.add(new ScrollPanel(getForeignKeysInfoTable(viewerTable)));
      }
    }

  }

  private SafeHtml getTableDescriptionItemHTML(ViewerTable table) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    Hyperlink hyperlink = new Hyperlink("Table `" + table.getName() + "`", HistoryManager.linkToTable(
      database.getUUID(), table.getUUID()));
    hyperlink.addStyleName("h3");

    b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
    b.append(SafeHtmlUtils.fromSafeConstant(hyperlink.toString()));
    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">Description: "));
      b.append(SafeHtmlUtils.fromString(table.getDescription()));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    }

    b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

    return b.toSafeHtml();
  }

  private SafeHtml getFieldHTML(String label, String value) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    if (value != null) {
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"label\">"));
      b.append(SafeHtmlUtils.fromString(label));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(SafeHtmlUtils.fromString(value));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    }
    return b.toSafeHtml();
  }

  private CellTable getColumnsInfoTable(final ViewerTable viewerTable) {
    final ViewerPrimaryKey pk = viewerTable.getPrimaryKey();

    // Create a CellTable.
    CellTable<ViewerColumn> table = new CellTable<ViewerColumn>(Integer.MAX_VALUE,
      (MyCellTableResources) GWT.create(MyCellTableResources.class));
    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    table
      .setLoadingIndicator(new HTML(
        SafeHtmlUtils
          .fromSafeConstant("<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>")));

    final HashSet<Integer> columnIndexesWithForeignKeys = new HashSet<>();
    for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {
      for (ViewerReference viewerReference : viewerForeignKey.getReferences()) {
        columnIndexesWithForeignKeys.add(viewerReference.getSourceColumnIndex());
      }
    }

    SafeHtmlCell primaryKeyColumnCell = new SafeHtmlCell();
    Column<ViewerColumn, SafeHtml> primaryKeyColumn = new Column<ViewerColumn, SafeHtml>(primaryKeyColumnCell) {
      @Override
      public SafeHtml getValue(ViewerColumn column) {
        if (pk.getColumnIndexesInViewerTable().contains(column.getColumnIndexInEnclosingTable())) {
          return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-key' title='Primary Key'></i>");
        } else if(columnIndexesWithForeignKeys.contains(column.getColumnIndexInEnclosingTable())) {
          return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-reply' title='Part of one or more Foreign Keys'></i>");
        }else{
          return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
      }
    };
    primaryKeyColumn.setCellStyleNames("primary-key-col");

    // Create name column.
    TextColumn<ViewerColumn> nameColumn = new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getDisplayName();
      }
    };

    TextColumn<ViewerColumn> typeColumn = new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getType().getTypeName();
      }
    };

    TextColumn<ViewerColumn> typeOriginalColumn = new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getType().getOriginalTypeName();
      }
    };

    TextColumn<ViewerColumn> nullableColumn = new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        if (column.getNillable()) {
          return "Yes";
        } else {
          return "No";
        }
      }
    };

    TextColumn<ViewerColumn> descriptionColumn = new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        if (ViewerStringUtils.isNotBlank(column.getDescription())) {
          return column.getDescription();
        } else {
          return "";
        }
      }
    };

    // nameColumn.setSortable(true);
    // typeColumn.setSortable(true);
    // typeOriginalColumn.setSortable(true);
    // nullableColumn.setSortable(true);
    // descriptionColumn.setSortable(true);

    // Add the columns.
    addColumnToTable(table, primaryKeyColumn, 2.2, new SafeHtmlBuilder().toSafeHtml());
    addColumnToTable(table, nameColumn, 15, "Column name");
    addColumnToTable(table, typeColumn, 15, "Type name");
    addColumnToTable(table, typeOriginalColumn, 15, "Original type name");
    addColumnToTable(table, nullableColumn, 8, "Nullable");
    addColumnToTable(table, descriptionColumn, 35, "Description");

    // Create a data provider.
    ListDataProvider<ViewerColumn> dataProvider = new ListDataProvider<ViewerColumn>();

    // Connect the table to the data provider.
    dataProvider.addDataDisplay(table);

    // Add the data to the data provider, which automatically pushes it to the
    // widget.
    List<ViewerColumn> list = dataProvider.getList();
    for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
      list.add(viewerColumn);
    }

    // Add a ColumnSortEvent.ListHandler to connect sorting to the
    // java.util.List.
    // ListHandler<Contact> columnSortHandler = new ListHandler<Tester.Contact>(
    // list);
    // columnSortHandler.setComparator(nameColumn,
    // new Comparator<Tester.Contact>() {
    // public int compare(Contact o1, Contact o2) {
    // if (o1 == o2) {
    // return 0;
    // }
    //
    // // Compare the name columns.
    // if (o1 != null) {
    // return (o2 != null) ? o1.name.compareTo(o2.name) : 1;
    // }
    // return -1;
    // }
    // });
    // table.addColumnSortHandler(columnSortHandler);

    // We know that the data is sorted alphabetically by default.
    // table.getColumnSortList().push(nameColumn);

    table.addStyleName("table-info my-asyncdatagrid-display");

    return table;
  }

  private CellTable getForeignKeysInfoTable(final ViewerTable viewerTable) {
    // Create a CellTable.
    CellTable<ViewerForeignKey> table = new CellTable<ViewerForeignKey>(Integer.MAX_VALUE,
      (MyCellTableResources) GWT.create(MyCellTableResources.class));
    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    table
      .setLoadingIndicator(new HTML(
        SafeHtmlUtils
          .fromSafeConstant("<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>")));

    TextColumn<ViewerForeignKey> nameColumn = new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getName();
      }
    };

    TextColumn<ViewerForeignKey> referencedSchemaColumn = new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return database.getMetadata().getTable(foreignKey.getReferencedTableUUID()).getSchemaName();
      }
    };

    TextColumn<ViewerForeignKey> referencedTableColumn = new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return database.getMetadata().getTable(foreignKey.getReferencedTableUUID()).getName();
      }
    };

    SafeHtmlCell referencedColumnsColumnCell = new SafeHtmlCell();
    Column<ViewerForeignKey, SafeHtml> referencedColumnsColumn = new Column<ViewerForeignKey, SafeHtml>(
      referencedColumnsColumnCell) {
      @Override
      public SafeHtml getValue(ViewerForeignKey foreignKey) {
        ViewerTable referencedTable = database.getMetadata().getTable(foreignKey.getReferencedTableUUID());

        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        for (Iterator<ViewerReference> i = foreignKey.getReferences().iterator(); i.hasNext();) {
          ViewerReference reference = i.next();

          builder.appendEscaped(viewerTable.getColumns().get(reference.getSourceColumnIndex()).getDisplayName())
            .appendHtmlConstant(" <i class='fa fa-arrow-right'></i> ")
            .appendEscaped(referencedTable.getColumns().get(reference.getReferencedColumnIndex()).getDisplayName());

          if (i.hasNext()) {
            builder.appendHtmlConstant("<br/>");
          }
        }
        return builder.toSafeHtml();
      }
    };

    TextColumn<ViewerForeignKey> matchTypeColumn = new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getMatchType();
      }
    };

    TextColumn<ViewerForeignKey> updateActionColumn = new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getUpdateAction();
      }
    };

    TextColumn<ViewerForeignKey> deleteActionColumn = new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        return foreignKey.getDeleteAction();
      }
    };

    TextColumn<ViewerForeignKey> descriptionColumn = new TextColumn<ViewerForeignKey>() {
      @Override
      public String getValue(ViewerForeignKey foreignKey) {
        if (ViewerStringUtils.isNotBlank(foreignKey.getDescription())) {
          return foreignKey.getDescription();
        } else {
          return "";
        }
      }
    };

    // Add the columns.
    addColumnToTable(table, nameColumn, 15, "Name");
    addColumnToTable(table, referencedSchemaColumn, 15, "Referenced Schema");
    addColumnToTable(table, referencedTableColumn, 15, "Referenced Table");
    addColumnToTable(table, referencedColumnsColumn, 20,
      SafeHtmlUtils.fromSafeConstant("Mapping (Source <i class=\"fa fa-arrow-right\"></i> Referenced)"));
    addColumnToTable(table, matchTypeColumn, 10, "Match type");
    addColumnToTable(table, updateActionColumn, 9, "Update action");
    addColumnToTable(table, deleteActionColumn, 9, "Delete action");
    addColumnToTable(table, descriptionColumn, 35, "Description");

    // Create a data provider.
    ListDataProvider<ViewerForeignKey> dataProvider = new ListDataProvider<ViewerForeignKey>();

    // Connect the table to the data provider.
    dataProvider.addDataDisplay(table);

    // Add the data to the data provider, which automatically pushes it to the
    // widget.
    List<ViewerForeignKey> list = dataProvider.getList();
    for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {
      list.add(viewerForeignKey);
    }

    table.addStyleName("table-info my-asyncdatagrid-display");

    return table;
  }

  private void addColumnToTable(CellTable table, Column column, double size, SafeHtml headerHTML) {
    SafeHtmlHeader header = new SafeHtmlHeader(headerHTML);
    table.addColumn(column, header);
    // header.setHeaderStyleNames("cellTableFadeOut");
    // column.setCellStyleNames("cellTableFadeOut");

    // if(size != 0) {
    table.setColumnWidth(column, size, Style.Unit.EM);
    // }else{
    // header.setHeaderStyleNames("filler-column");
    // column.setCellStyleNames("filler-column");
    // }
  }

  private void addColumnToTable(CellTable table, Column column, double size, String headerString) {
    addColumnToTable(table, column, size, SafeHtmlUtils.fromString(headerString));
  }
}
