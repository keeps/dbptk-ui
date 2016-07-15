package com.databasepreservation.visualization.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerCell;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerReference;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchPanel;
import com.databasepreservation.visualization.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RowPanel extends Composite {
  private static Map<String, RowPanel> instances = new HashMap<>();

  public static RowPanel createInstance(String databaseUUID, String tableUUID, String rowUUID) {
    return new RowPanel(databaseUUID, tableUUID, rowUUID);
  }

  public static RowPanel createInstance(ViewerDatabase database, ViewerTable table, ViewerRow row) {
    return new RowPanel(database, table, row);
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, RowPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private final String rowUUID;
  private ViewerRow row;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;

  @UiField
  HTML content;

  @UiField
  HTML tableName;

  @UiField
  HTML rowID;

  private RowPanel(ViewerDatabase database, ViewerTable table, ViewerRow row) {
    this.rowUUID = row.getUUID();
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(database.getUUID());

    initWidget(uiBinder.createAndBindUi(this));

    rowID.setHTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD) + " "
      + SafeHtmlUtils.htmlEscape(rowUUID)));
    tableName.setHTML(FontAwesomeIconManager.loading(FontAwesomeIconManager.TABLE));

    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.loadingRecord(database.getUUID(), table.getUUID(), rowUUID));

    this.database = database;
    this.table = table;
    this.row = row;

    init();
  }

  private RowPanel(final String databaseUUID, final String tableUUID, final String rowUUID) {
    this.rowUUID = rowUUID;
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    rowID.setHTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD) + " "
      + SafeHtmlUtils.htmlEscape(rowUUID)));
    tableName.setHTML(FontAwesomeIconManager.loading(FontAwesomeIconManager.TABLE));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingRecord(databaseUUID, tableUUID, rowUUID));

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          table = database.getMetadata().getTable(tableUUID);
          init();
        }
      });

    BrowserService.Util.getInstance().retrieveRows(ViewerRow.class.getName(), tableUUID, rowUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          row = (ViewerRow) result;
          init();
        }
      });
  }

  private Hyperlink getHyperlink(String display_text, String database_uuid, String table_uuid) {
    Hyperlink link = new Hyperlink(display_text, HistoryManager.linkToTable(database_uuid, table_uuid));
    return link;
  }

  private void init() {
    if (database == null) {
      return;
    }

    // breadcrumb
    BreadcrumbManager.updateBreadcrumb(
      breadcrumb,
      BreadcrumbManager.forRecord(database.getMetadata().getName(), database.getUUID(), table.getSchemaName(),
        table.getSchemaUUID(), table.getName(), table.getUUID(), rowUUID));

    tableName.setHTML(FontAwesomeIconManager.loaded(FontAwesomeIconManager.TABLE, table.getName()));

    if (row != null) {
      Set<Ref> recordRelatedTo = new TreeSet<>();
      Set<Ref> recordReferencedBy = new TreeSet<>();

      Map<Integer, Set<Ref>> colIndexRelatedTo = new HashMap<>();
      Map<Integer, Set<Ref>> colIndexReferencedBy = new HashMap<>();

      ViewerMetadata metadata = database.getMetadata();

      // get references where this column is source in foreign keys
      for (ViewerForeignKey fk : table.getForeignKeys()) {
        Ref ref = new Ref(metadata.getTable(fk.getReferencedTableUUID()), fk);
        if (fk.getReferences().size() == 1) {
          Set<Ref> refs = colIndexRelatedTo.get(ref.getSingleColumnIndex());
          if (refs == null) {
            refs = new TreeSet<>();
            colIndexRelatedTo.put(ref.getSingleColumnIndex(), refs);
          }
          refs.add(ref);
        } else {
          recordRelatedTo.add(ref);
        }
      }

      // get references where this column is (at least one of) the target of
      // foreign keys
      for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
        for (ViewerTable viewerTable : viewerSchema.getTables()) {
          for (ViewerForeignKey fk : viewerTable.getForeignKeys()) {
            if (fk.getReferencedTableUUID().equals(table.getUUID())) {
              Ref ref = new Ref(viewerTable, fk);
              if (fk.getReferences().size() == 1) {
                Set<Ref> refs = colIndexReferencedBy.get(ref.getSingleColumnIndex());
                if (refs == null) {
                  refs = new TreeSet<>();
                  colIndexReferencedBy.put(ref.getSingleColumnIndex(), refs);
                }
                refs.add(ref);
              } else {
                recordReferencedBy.add(ref);
              }
            }
          }
        }
      }

      // row data
      SafeHtmlBuilder b = new SafeHtmlBuilder();

      // foreign key relations first
      if (!recordRelatedTo.isEmpty() || !recordReferencedBy.isEmpty()) {
        b.appendHtmlConstant("<div class=\"field\">");
        if (!recordRelatedTo.isEmpty()) {
          b.append(getForeignKeyHTML("This record is related to", recordRelatedTo, row));
        }
        if (!recordReferencedBy.isEmpty()) {
          b.append(getForeignKeyHTML("This record is referenced by", recordReferencedBy, row));
        }
        b.appendHtmlConstant("</div>");
      }

      for (ViewerColumn column : table.getColumns()) {
        b.append(getCellHTML(column,
          colIndexRelatedTo.get(column.getColumnIndexInEnclosingTable()), colIndexReferencedBy.get(column.getColumnIndexInEnclosingTable()), table
            .getPrimaryKey().getColumnIndexesInViewerTable().contains(column.getColumnIndexInEnclosingTable())));
      }

      content.setHTML(b.toSafeHtml());
    }
  }

  private SafeHtml getForeignKeyHTML(String prefix, Set<Ref> refs, ViewerRow row) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.appendHtmlConstant("<div class=\"value related-records\">");
    b.appendEscaped(prefix);
    b.appendEscaped(" ");

    Iterator<Ref> iterator = refs.iterator();
    while (iterator.hasNext()) {
      Ref ref = iterator.next();
      Hyperlink hyperlink = new Hyperlink(ref.getSchemaAndTableName(), HistoryManager.linkToForeignKey(
        database.getUUID(), ref.refTable.getUUID(), ref.getColumnNamesAndValues(row)));
      hyperlink.addStyleName("related-records-link");

      b.appendHtmlConstant(hyperlink.toString());

      if (iterator.hasNext()) {
        b.appendHtmlConstant(", ");
      }
    }
    b.appendHtmlConstant("</div>");

    return b.toSafeHtml();
  }

  private SafeHtml getCellHTML(ViewerColumn column, Set<Ref> relatedTo, Set<Ref> referencedBy, boolean isPrimaryKeyColumn) {
    String label = column.getDisplayName();

    String value = null;
    ViewerCell cell = row.getCells().get(column.getSolrName());
    if (cell != null) {
      if (cell.getValue() != null) {
        value = cell.getValue();
      }
    }

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.appendHtmlConstant("<div class=\"field\">");
    if (isPrimaryKeyColumn) {
      b.appendHtmlConstant("<div class=\"label fa-key\">");
    } else {
      b.appendHtmlConstant("<div class=\"label noicon\">");
    }
    b.appendEscaped(label);
    b.appendHtmlConstant("</div>");
    b.appendHtmlConstant("<div class=\"value\">");
    if (value == null) {
      b.appendEscaped("NULL");
    } else {
      b.appendEscaped(value);
    }
    b.appendHtmlConstant("</div>");

    if(relatedTo != null && !relatedTo.isEmpty()){
      b.append(getForeignKeyHTML("Is related to", relatedTo, row));
    }

    if(referencedBy != null && !referencedBy.isEmpty()){
      b.append(getForeignKeyHTML("Is referenced by", referencedBy, row));
    }

    return b.toSafeHtml();
  }

  /**
   * A foreign key reference from which to generate an ordered list of links to
   * foreign key relation values
   *
   * Note: this class has a natural ordering that is inconsistent with equals.
   */
  private static class Ref implements Comparable<Ref> {
    ViewerTable refTable;
    Map<String, Integer> solrColumnToRowColumnIndex;

    Ref(ViewerTable otherTable, ViewerForeignKey foreignKey) {
      refTable = otherTable;
      solrColumnToRowColumnIndex = new TreeMap<>();

      // tableUUID to use in URL is always otherTable.getUUID()
      if (foreignKey.getReferencedTableUUID().equals(otherTable.getUUID())) {
        // related to
        // currentTable -> otherTable
        // fk belongs to current table, fk target is otherTable
        // get column names from fk target (use otherTable to map indexes to
        // names)
        // get column indexes from fk source
        for (ViewerReference viewerReference : foreignKey.getReferences()) {
          String solrColumnName = otherTable.getColumns().get(viewerReference.getReferencedColumnIndex())
            .getSolrName();
          Integer columnIndexToGetValue = viewerReference.getSourceColumnIndex();
          solrColumnToRowColumnIndex.put(solrColumnName, columnIndexToGetValue);
        }
      } else {
        // referenced by
        // currentTable <- otherTable
        // fk belongs to otherTable, fk target is current table
        // get column names from source (use otherTable to map indexes to names)
        // get column indexes from fk target
        for (ViewerReference viewerReference : foreignKey.getReferences()) {
          solrColumnToRowColumnIndex.put(otherTable.getColumns().get(viewerReference.getSourceColumnIndex())
            .getSolrName(), viewerReference.getReferencedColumnIndex());
        }
      }
    }

    public List<String> getColumnNamesAndValues(ViewerRow row) {
      List<String> params = new ArrayList<>();
      for (String colName : solrColumnToRowColumnIndex.keySet()) {
        String value = row.getCells().get(colName).getValue();
        params.add(colName);
        params.add(value);
      }
      return params;
    }

    public String getSchemaAndTableName() {
      return refTable.getSchemaName() + "." + refTable.getName();
    }

    public Integer getSingleColumnIndex() {
      for (Integer index : solrColumnToRowColumnIndex.values()) {
        return index;
      }
      return 0;
    }

    /**
     * Uses schema name and table name to compare instances (for ordering)
     *
     * @param o
     *          the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Ref o) {
      int schemaCompare = refTable.getSchemaName().compareTo(o.refTable.getSchemaName());
      if (schemaCompare == 0) {
        return refTable.getName().compareTo(o.refTable.getName());
      } else {
        return schemaCompare;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Ref ref = (Ref) o;
      return Objects.equals(refTable.getUUID(), ref.refTable.getUUID())
        && Objects.equals(solrColumnToRowColumnIndex, ref.solrColumnToRowColumnIndex);
    }

    @Override
    public int hashCode() {
      return Objects.hash(refTable.getUUID(), solrColumnToRowColumnIndex);
    }
  }
}
