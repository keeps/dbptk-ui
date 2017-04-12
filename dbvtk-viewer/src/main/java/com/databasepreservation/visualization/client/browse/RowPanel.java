package com.databasepreservation.visualization.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import com.databasepreservation.visualization.client.ViewerStructure.ViewerType;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.utils.CommonClientUtils;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RowPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, RowPanel> instances = new HashMap<>();

  public static RowPanel createInstance(ViewerDatabase database, String tableUUID, String rowUUID) {
    return new RowPanel(database, tableUUID, rowUUID);
  }

  public static RowPanel createInstance(ViewerDatabase database, ViewerTable table, ViewerRow row) {
    return new RowPanel(database, table, row);
  }

  interface RowPanelUiBinder extends UiBinder<Widget, RowPanel> {
  }

  private static RowPanelUiBinder uiBinder = GWT.create(RowPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private final String rowUUID;
  private ViewerRow row;

  @UiField
  HTML content;

  @UiField
  SimplePanel recordHeader;

  @UiField
  HTML rowID;

  private RowPanel(ViewerDatabase database, ViewerTable table, ViewerRow row) {
    this.rowUUID = row.getUUID();
    this.database = database;
    this.table = table;
    this.row = row;

    initWidget(uiBinder.createAndBindUi(this));

    rowID.setHTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD) + " "
      + SafeHtmlUtils.htmlEscape(rowUUID)));
    recordHeader.setWidget(CommonClientUtils.getSchemaAndTableHeader(database.getUUID(), table, "h1"));

    init();
  }

  private RowPanel(ViewerDatabase viewerDatabase, final String tableUUID, final String rowUUID) {
    this.rowUUID = rowUUID;
    this.database = viewerDatabase;
    this.table = database.getMetadata().getTable(tableUUID);

    initWidget(uiBinder.createAndBindUi(this));

    rowID.setHTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD) + " "
      + SafeHtmlUtils.htmlEscape(rowUUID)));
    recordHeader.setWidget(CommonClientUtils.getSchemaAndTableHeader(database.getUUID(), table, "h1"));

    BrowserService.Util.getInstance().retrieveRows(database.getUUID(), tableUUID, rowUUID,
      new DefaultAsyncCallback<ViewerRow>() {
        @Override
        public void onSuccess(ViewerRow result) {
          row = result;
          init();
        }
      });
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(
      breadcrumb,
      BreadcrumbManager.forRecord(database.getMetadata().getName(), database.getUUID(), table.getSchemaName(),
        table.getSchemaUUID(), table.getName(), table.getUUID(), rowUUID));
  }

  private void init() {
    Set<Ref> recordRelatedTo = new TreeSet<>();
    Set<Ref> recordReferencedBy = new TreeSet<>();

    Map<String, Set<Ref>> colIndexRelatedTo = new HashMap<>();
    Map<String, Set<Ref>> colIndexReferencedBy = new HashMap<>();

    ViewerMetadata metadata = database.getMetadata();

    // get references where this column is source in foreign keys
    for (ViewerForeignKey fk : table.getForeignKeys()) {
      Ref ref = new Ref(table, metadata.getTable(fk.getReferencedTableUUID()), fk);
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
            Ref ref = new Ref(table, viewerTable, fk);
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
        b.append(getForeignKeyHTML(messages.references_thisRecordIsRelatedTo(), recordRelatedTo, row));
      }
      if (!recordReferencedBy.isEmpty()) {
        b.append(getForeignKeyHTML(messages.references_thisRecordIsReferencedBy(), recordReferencedBy, row));
      }
      b.appendHtmlConstant("</div>");
    }

    for (ViewerColumn column : table.getColumns()) {
      boolean isPrimaryKeyColumn = table.getPrimaryKey() != null
        && table.getPrimaryKey().getColumnIndexesInViewerTable().contains(column.getColumnIndexInEnclosingTable());
      b.append(getCellHTML(column, colIndexRelatedTo.get(column.getSolrName()),
        colIndexReferencedBy.get(column.getSolrName()), isPrimaryKeyColumn));
    }

    content.setHTML(b.toSafeHtml());
  }

  private SafeHtml getForeignKeyHTML(String prefix, Set<Ref> refs, ViewerRow row) {
    boolean firstRef = true;
    int nonNullReferenceCounter = 0;

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.appendHtmlConstant("<div class=\"value related-records\">");
    b.appendEscaped(prefix);
    b.appendEscaped(" ");

    for (Ref ref : refs) {
      List<String> columnNamesAndValues = ref.getColumnNamesAndValues(row);

      if (!columnNamesAndValues.isEmpty()) {
        if (!firstRef) {
          b.appendHtmlConstant(", ");
        }

        Hyperlink hyperlink = new Hyperlink(ref.getSchemaAndTableName(), HistoryManager.linkToForeignKey(
          database.getUUID(), ref.refTable.getUUID(), columnNamesAndValues));
        hyperlink.addStyleName("related-records-link");
        b.appendHtmlConstant(hyperlink.toString());
        firstRef = false;
        nonNullReferenceCounter++;
      }
    }
    b.appendHtmlConstant("</div>");

    if (nonNullReferenceCounter > 0) {
      return b.toSafeHtml();
    } else {
      return SafeHtmlUtils.EMPTY_SAFE_HTML;
    }
  }

  private SafeHtml getCellHTML(ViewerColumn column, Set<Ref> relatedTo, Set<Ref> referencedBy,
    boolean isPrimaryKeyColumn) {
    String label = column.getDisplayName();

    String value = null;
    ViewerCell cell = row.getCells().get(column.getSolrName());
    if (cell != null) {
      if (cell.getValue() != null) {
        value = cell.getValue();
      }
    }

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.appendHtmlConstant("<div class=\"field field-margin\">");
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
      if (column.getType().getDbType().equals(ViewerType.dbTypes.BINARY)) {
        StringBuilder urlBuilder = new StringBuilder();
        String base = com.google.gwt.core.client.GWT.getHostPageBaseURL();
        String servlet = ViewerSafeConstants.API_SERVLET;
        String resource = ViewerSafeConstants.API_V1_LOBS_RESOURCE;
        String databaseUUID = database.getUUID();
        String tableUUID = table.getUUID();
        urlBuilder.append(base).append(servlet).append(resource).append("/").append(databaseUUID).append("/")
          .append(tableUUID).append("/").append(row.getUUID()).append("/")
          .append(column.getColumnIndexInEnclosingTable());
        b.appendHtmlConstant("<a href=\"" + urlBuilder.toString() + "\">");
        b.appendEscaped(messages.row_downloadLOB());
        b.appendHtmlConstant("</a>");
      } else {
        b.appendEscaped(value);
      }
    }
    b.appendHtmlConstant("</div>");

    if (relatedTo != null && !relatedTo.isEmpty()) {
      b.append(getForeignKeyHTML(messages.references_isRelatedTo(), relatedTo, row));
    }

    if (referencedBy != null && !referencedBy.isEmpty()) {
      b.append(getForeignKeyHTML(messages.references_isReferencedBy(), referencedBy, row));
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
    Map<String, String> foreignSolrColumnToRowSolrColumn;

    Ref(ViewerTable currentTable, ViewerTable otherTable, ViewerForeignKey foreignKey) {
      refTable = otherTable;
      foreignSolrColumnToRowSolrColumn = new TreeMap<>();

      // tableUUID to use in URL is always otherTable.getUUID()
      if (foreignKey.getReferencedTableUUID().equals(otherTable.getUUID())) {
        // related to
        // currentTable -> otherTable
        // fk belongs to current table, fk target is otherTable
        // get column names from fk target (use otherTable to map indexes to
        // names)
        // get column indexes from fk source
        for (ViewerReference viewerReference : foreignKey.getReferences()) {
          String solrColumnName = otherTable.getColumns().get(viewerReference.getReferencedColumnIndex()).getSolrName();
          Integer columnIndexToGetValue = viewerReference.getSourceColumnIndex();
          String solrColumnNameToGetValue = currentTable.getColumns().get(columnIndexToGetValue).getSolrName();
          foreignSolrColumnToRowSolrColumn.put(solrColumnName, solrColumnNameToGetValue);
        }
      } else {
        // referenced by
        // currentTable <- otherTable
        // fk belongs to otherTable, fk target is current table
        // get column names from source (use otherTable to map indexes to names)
        // get column indexes from fk target
        for (ViewerReference viewerReference : foreignKey.getReferences()) {
          String solrColumnName = otherTable.getColumns().get(viewerReference.getSourceColumnIndex()).getSolrName();
          Integer columnIndexToGetValue = viewerReference.getReferencedColumnIndex();
          String solrColumnNameToGetValue = currentTable.getColumns().get(columnIndexToGetValue).getSolrName();
          foreignSolrColumnToRowSolrColumn.put(solrColumnName, solrColumnNameToGetValue);
        }
      }
    }

    public List<String> getColumnNamesAndValues(ViewerRow row) {
      List<String> params = new ArrayList<>();
      for (String colName : foreignSolrColumnToRowSolrColumn.keySet()) {
        String rowColName = foreignSolrColumnToRowSolrColumn.get(colName);
        ViewerCell viewerCell = row.getCells().get(rowColName);
        if (viewerCell != null) {
          String value = viewerCell.getValue();
          params.add(colName);
          params.add(value);
        }
      }
      return params;
    }

    public String getSchemaAndTableName() {
      return refTable.getSchemaName() + "." + refTable.getName();
    }

    public String getSingleColumnIndex() {
      for (String colName : foreignSolrColumnToRowSolrColumn.values()) {
        return colName;
      }
      return null;
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
        && Objects.equals(foreignSolrColumnToRowSolrColumn, ref.foreignSolrColumnToRowSolrColumn);
    }

    @Override
    public int hashCode() {
      return Objects.hash(refTable.getUUID(), foreignSolrColumnToRowSolrColumn);
    }
  }
}
