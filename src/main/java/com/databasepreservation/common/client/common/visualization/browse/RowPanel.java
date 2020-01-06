package com.databasepreservation.common.client.common.visualization.browse;

import java.util.*;

import com.databasepreservation.common.client.common.lists.TableRowList;
import com.databasepreservation.common.client.common.search.TableSearchPanel;
import org.roda.core.data.v2.index.filter.*;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.fields.RowField;
import com.databasepreservation.common.client.common.helpers.HelperExportTableData;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.ExportResourcesUtils;
import com.databasepreservation.common.client.index.ExportRequest;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.*;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RowPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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
  SimplePanel recordHeader;

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel description;

  private RowPanel(ViewerDatabase database, ViewerTable table, ViewerRow row) {
    this.rowUUID = row.getUuid();
    this.database = database;
    this.table = table;
    this.row = row;

    initWidget(uiBinder.createAndBindUi(this));

    setTitle();
    init();
  }

  private RowPanel(ViewerDatabase viewerDatabase, final String tableUUID, final String rowUUID) {
    this.rowUUID = rowUUID;
    this.database = viewerDatabase;
    this.table = database.getMetadata().getTable(tableUUID);

    initWidget(uiBinder.createAndBindUi(this));

    setTitle();

    DatabaseService.Util.call((ViewerRow result) -> {
      row = result;
      init();
    }).retrieveRow(database.getUuid(), rowUUID);
  }

  private void setTitle() {
    recordHeader.setWidget(CommonClientUtils.getHeader(table, "h1", database.getMetadata().getSchemas().size() > 1));
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forRecord(database.getMetadata().getName(),
      database.getUuid(), table.getNameWithoutPrefix(), table.getUuid(), rowUUID));
  }

  private void init() {
    if (ViewerStringUtils.isNotBlank(table.getDescription())) {
      MetadataField instance = MetadataField.createInstance(table.getDescription());
      instance.setCSS("table-row-description");
      description.add(instance);
    }

    Set<Ref> recordRelatedTo = new TreeSet<>();
    Set<Ref> recordReferencedBy = new TreeSet<>();

    Map<String, Set<Ref>> colIndexRelatedTo = new HashMap<>();
    Map<String, Set<Ref>> colIndexReferencedBy = new HashMap<>();

    ViewerMetadata metadata = database.getMetadata();

    // get references where this column is source in foreign keys
    for (ViewerForeignKey fk : table.getForeignKeys()) {
      Ref ref = new Ref(table, metadata.getTable(fk.getReferencedTableUUID()), fk);
      if (fk.getReferences().size() == 1) {
        Set<Ref> refs = colIndexRelatedTo.computeIfAbsent(ref.getSingleColumnIndex(), k -> new TreeSet<>());
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
          if (fk.getReferencedTableUUID().equals(table.getUuid())) {
            Ref ref = new Ref(table, viewerTable, fk);
            if (fk.getReferences().size() == 1) {
              Set<Ref> refs = colIndexReferencedBy.computeIfAbsent(ref.getSingleColumnIndex(), k -> new TreeSet<>());
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
      getCellHTML(column, colIndexRelatedTo.get(column.getSolrName()), colIndexReferencedBy.get(column.getSolrName()),
        isPrimaryKeyColumn);
    }

    getNestedHTML(row.getNestedRowList());

    Button btn = new Button();
    btn.addStyleName("btn btn-primary btn-download");
    btn.setText(messages.rowPanelTextForButtonExportSingleRow());
    btn.addClickHandler(event -> {
      HelperExportTableData helperExportTableData = new HelperExportTableData(table, true);
      Dialogs.showCSVSetupDialog(messages.csvExportDialogTitle(),
        helperExportTableData.getWidget(table.containsBinaryColumns()), messages.basicActionCancel(),
        messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {

          @Override
          public void onSuccess(Boolean result) {
            if (Boolean.TRUE.equals(result)) {
              String filename = helperExportTableData.getFilename();
              boolean exportDescription = helperExportTableData.exportDescription();

              if (helperExportTableData.isZipHelper()) {
                boolean exportLOBs = helperExportTableData.exportLobs();
                String zipFilename = helperExportTableData.getZipFileName();
                Window.Location.assign(getExportURL(zipFilename, filename, exportDescription, exportLOBs));
              } else {
                Window.Location.assign(getExportURL(null, filename, exportDescription, false));
              }
            }
          }
        });
    });

    content.add(btn);
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

        Hyperlink hyperlink = new Hyperlink(ref.getSchemaAndTableName(),
          HistoryManager.linkToForeignKey(database.getUuid(), ref.refTable.getUuid(), columnNamesAndValues));
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

  private void getCellHTML(ViewerColumn column, Set<Ref> relatedTo, Set<Ref> referencedBy, boolean isPrimaryKeyColumn) {
    String label = column.getDisplayName();

    String value = null;
    ViewerCell cell = row.getCells().get(column.getSolrName());
    if (cell != null && cell.getValue() != null) {
      value = cell.getValue();
    }

    RowField rowField;

    if (isPrimaryKeyColumn) {
      String iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.KEY, "Primary Key");
      rowField = RowField.createInstance(iconTag, label, new HTML(value));

    } else if (value == null) {
      rowField = RowField.createInstance(label, new HTML("NULL"));
    } else {
      if (column.getType().getDbType().equals(ViewerType.dbTypes.BINARY)) {
        rowField = RowField.createInstance(label, CommonClientUtils.getAnchorForLOBDownload(database.getUuid(),
          table.getUuid(), row.getUuid(), column.getColumnIndexInEnclosingTable(), cell.getValue()));
      } else {
        rowField = RowField.createInstance(label, new HTML(value));
      }
    }

    if (ViewerStringUtils.isNotBlank(column.getDescription())) {
      rowField.addColumnDescription(column.getDescription());
    }

    if (relatedTo != null && !relatedTo.isEmpty()) {
      rowField.addRelatedTo(getForeignKeyHTML(messages.references_isRelatedTo(), relatedTo, row), "field");
    }

    if (referencedBy != null && !referencedBy.isEmpty()) {
      rowField.addReferencedBy(getForeignKeyHTML(messages.references_isReferencedBy(), referencedBy, row), "field");
    }

    content.add(rowField);
  }

  private void getNestedHTML(List<ViewerRow> nestedRowList) {
    Filter filter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();

    Map<String, List<String>> columnsMap = new HashMap<>();
    for (ViewerRow nestedRow : nestedRowList) {
      filterParameterList.add(new SimpleFilterParameter(ViewerConstants.INDEX_ID, nestedRow.getNestedOriginalUUID()));

      columnsMap.computeIfAbsent(nestedRow.getTableId(), k -> new ArrayList<>());
      Map<String, ViewerCell> cells = nestedRow.getCells();
      for (Map.Entry<String, ViewerCell> entry : cells.entrySet()) {
        columnsMap.get(nestedRow.getTableId()).add(entry.getValue().getValue());
      }
    }

    filter.add(new OrFiltersParameters(filterParameterList));

    for (Map.Entry<String, List<String>> entry : columnsMap.entrySet()) {
      RowField rowField = RowField.createInstance(entry.getKey(), null);
      content.add(rowField);

      ViewerTable table = database.getMetadata().getTableById(entry.getKey());

      final TableSearchPanel tableSearchPanel = new TableSearchPanel();
          tableSearchPanel.provideSource(database, table, filter);

      //TableRowList tableRowList = new TableRowList(database, table, filter, null, null, false, false);

      content.add(tableSearchPanel);
    }
  }

  private String getExportURL(String zipFilename, String filename, boolean description, boolean exportLOBs) {
    List<FilterParameter> filterParameters = new ArrayList<>();
    filterParameters.add(new SimpleFilterParameter(ViewerConstants.INDEX_ID, row.getUuid()));

    // prepare parameter: field list
    List<String> solrColumns = new ArrayList<>();
    for (Map.Entry<String, ViewerCell> entry : row.getCells().entrySet()) {
      solrColumns.add(entry.getKey());
    }

    FindRequest findRequest = new FindRequest(ViewerRow.class.getName(), new Filter(filterParameters), Sorter.NONE,
      new Sublist(), Facets.NONE, false, solrColumns);
    ExportRequest exportRequest = new ExportRequest(filename, zipFilename, description, exportLOBs, true);

    return ExportResourcesUtils.getExportURL(database.getUuid(), table.getUuid(), findRequest, exportRequest);
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
      if (foreignKey.getReferencedTableUUID().equals(otherTable.getUuid())) {
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
      foreignSolrColumnToRowSolrColumn.keySet().forEach(colName -> {
        String rowColName = foreignSolrColumnToRowSolrColumn.get(colName);
        ViewerCell viewerCell = row.getCells().get(rowColName);
        if (viewerCell != null) {
          String value = viewerCell.getValue();
          params.add(colName);
          params.add(value);
        }
      });
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
      return Objects.equals(refTable.getUuid(), ref.refTable.getUuid())
        && Objects.equals(foreignSolrColumnToRowSolrColumn, ref.foreignSolrColumnToRowSolrColumn);
    }

    @Override
    public int hashCode() {
      return Objects.hash(refTable.getUuid(), foreignSolrColumnToRowSolrColumn);
    }
  }
}
