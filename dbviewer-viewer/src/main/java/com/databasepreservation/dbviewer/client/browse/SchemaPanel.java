package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.dbviewer.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
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

    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forSchema("Database (loading)", databaseUUID, "Schema (loading)", schemaUUID));

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
    } else {
      contentItems.add(new HTMLPanel(getFieldHTML("Schema description",
        "A description for this schema is not available.")));
    }

    Label tablesHeader = new Label("Tables");
    tablesHeader.addStyleName("h2");
    contentItems.add(tablesHeader);

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    for (ViewerTable viewerTable : schema.getTables()) {
      b.append(getTableDescriptionItemHTML(viewerTable));
    }

    contentItems.add(new HTMLPanel(b.toSafeHtml()));
  }

  private SafeHtml getTableDescriptionItemHTML(ViewerTable table) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    Hyperlink hyperlink = new Hyperlink(table.getName(),
      HistoryManager.linkToTable(database.getUUID(), table.getUUID()));

    b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
    b.append(SafeHtmlUtils.fromSafeConstant(hyperlink.toString()));
    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(SafeHtmlUtils.fromString(table.getDescription()));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    } else {
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(SafeHtmlUtils.fromString("A description for this table is not available"));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    }

    b.append(getInfoTable(table));

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

  private SafeHtml getInfoTable(ViewerTable viewerTable) {
    // Create a CellTable.
    CellTable<ViewerColumn> table = new CellTable<>();

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
          return "A description for this type is not available.";
        }
      }
    };

    // nameColumn.setSortable(true);
    // typeColumn.setSortable(true);
    // typeOriginalColumn.setSortable(true);
    // nullableColumn.setSortable(true);
    // descriptionColumn.setSortable(true);

    // Add the columns.
    table.addColumn(nameColumn, "Column name");
    table.addColumn(typeColumn, "Type name");
    table.addColumn(typeOriginalColumn, "Type name (original)");
    table.addColumn(nullableColumn, "Nullable");
    table.addColumn(descriptionColumn, "Description");

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

    return SafeHtmlUtils.fromSafeConstant(table.toString());
  }
}
