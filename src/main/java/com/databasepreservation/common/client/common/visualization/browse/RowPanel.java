package com.databasepreservation.common.client.common.visualization.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.fields.RowField;
import com.databasepreservation.common.client.common.helpers.HelperExportTableData;
import com.databasepreservation.common.client.common.search.TableSearchPanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.InnerJoinFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.tools.RestUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RowPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static RowPanel createInstance(ViewerDatabase database, String tableId, String rowIndex,
    CollectionStatus status) {
    return new RowPanel(database, tableId, rowIndex, status);
  }

  public static RowPanel createInstance(ViewerDatabase database, ViewerTable table, ViewerRow row,
    CollectionStatus status) {
    return new RowPanel(database, table, row, status);
  }

  interface RowPanelUiBinder extends UiBinder<Widget, RowPanel> {
  }

  private static RowPanelUiBinder uiBinder = GWT.create(RowPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private final String rowUUID;
  private ViewerRow row;
  private CollectionStatus status;

  @UiField
  SimplePanel recordHeader;

  @UiField
  Button btnExport;

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel description;

  private RowPanel(ViewerDatabase database, ViewerTable table, ViewerRow row, CollectionStatus status) {
    this.rowUUID = row.getUuid();
    this.database = database;
    this.table = table;
    this.row = row;
    this.status = status;

    initWidget(uiBinder.createAndBindUi(this));

    setTitle();
    init();
  }

  private RowPanel(ViewerDatabase viewerDatabase, final String tableId, final String rowIndex,
    CollectionStatus status) {
    this.rowUUID = rowIndex;
    this.database = viewerDatabase;
    this.table = database.getMetadata().getTableById(tableId);
    this.status = status;

    initWidget(uiBinder.createAndBindUi(this));

    setTitle();

    CollectionService.Util.call((ViewerRow result) -> {
      row = result;
      init();
    }).retrieveRow(database.getUuid(), database.getUuid(), table.getSchemaName(), table.getName(), rowUUID);
  }

  private void setTitle() {
    recordHeader.setWidget(CommonClientUtils.getHeader(table, "h1", database.getMetadata().getSchemas().size() > 1));
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forRecord(database.getMetadata().getName(),
      database.getUuid(), table.getNameWithoutPrefix(), table.getId(), rowUUID));
  }

  private void init() {
    if (ViewerStringUtils.isNotBlank(status.getTableStatusByTableId(table.getId()).getCustomDescription())) {
      MetadataField instance = MetadataField
        .createInstance(status.getTableStatusByTableId(table.getId()).getCustomDescription());
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

    for (ColumnStatus columnStatus : status.getTableStatus(table.getUuid()).getColumns()) {
      if (columnStatus.getDetailsStatus().isShow()) {
        if (columnStatus.getNestedColumns() == null) {
          ViewerColumn column = table.getColumnBySolrName(columnStatus.getId());
          boolean isPrimaryKeyColumn = table.getPrimaryKey() != null
            && table.getPrimaryKey().getColumnIndexesInViewerTable().contains(column.getColumnIndexInEnclosingTable());
          getCellHTML(column, colIndexRelatedTo.get(column.getSolrName()),
            colIndexReferencedBy.get(column.getSolrName()), isPrimaryKeyColumn, columnStatus);
        } else {
          getNestedHTML(columnStatus);
        }
      }
    }

    btnExport.setText(messages.rowPanelTextForButtonExportSingleRow());
    btnExport.addClickHandler(event -> {
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
          HistoryManager.linkToForeignKey(database.getUuid(), ref.refTable.getId(), columnNamesAndValues));
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

  private void getCellHTML(ViewerColumn column, Set<Ref> relatedTo, Set<Ref> referencedBy, boolean isPrimaryKeyColumn,
    ColumnStatus columnStatus) {
    String label = columnStatus.getCustomName();

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
        rowField = RowField.createInstance(label,
          CommonClientUtils.wrapOnAnchor(messages.row_downloadLOB(),
            RestUtils.createExportLobUri(database.getUuid(), table.getSchemaName(), table.getName(), row.getUuid(),
              column.getColumnIndexInEnclosingTable(), cell.getValue())));
      } else {
        rowField = RowField.createInstance(label, new HTML(value));
      }
    }

    if (ViewerStringUtils.isNotBlank(columnStatus.getCustomDescription())) {
      rowField.addColumnDescription(columnStatus.getCustomDescription());
    }

    if (relatedTo != null && !relatedTo.isEmpty()) {
      rowField.addRelatedTo(getForeignKeyHTML(messages.references_isRelatedTo(), relatedTo, row), "field");
    }

    if (referencedBy != null && !referencedBy.isEmpty()) {
      rowField.addReferencedBy(getForeignKeyHTML(messages.references_isReferencedBy(), referencedBy, row), "field");
    }

    content.add(rowField);
  }

  private void getNestedHTML(ColumnStatus columnStatus) {
    NestedColumnStatus nestedColumns = columnStatus.getNestedColumns();

    if (nestedColumns != null) {
      ViewerTable nestedTable = database.getMetadata().getTableById(nestedColumns.getOriginalTable());

      List<FilterParameter> filterParameterList = new ArrayList<>();
      filterParameterList.add(new InnerJoinFilterParameter(rowUUID, columnStatus.getId()));
      Filter filter = new Filter();
      filter.add(filterParameterList);

      if (columnStatus.getNestedColumns().getMultiValue()) {
        FlowPanel card = new FlowPanel();
        card.setStyleName("card");

        final TableSearchPanel tablePanel = new TableSearchPanel(status);
        tablePanel.provideSource(database, nestedTable, filter, true);
        card.add(tablePanel);

        content.add(card);
      } else {
        String template = columnStatus.getDetailsStatus().getTemplateStatus().getTemplate();
        FlowPanel panel = new FlowPanel();
        content.add(panel);
        if (template != null) {
          FindRequest findRequest = new FindRequest(ViewerDatabase.class.getName(), filter, new Sorter(), new Sublist(),
            null, false, new ArrayList<>());
          CollectionService.Util.call((IndexResult<ViewerRow> result) -> {
            if (result.getTotalCount() >= 1) {
              String json = JSOUtils.cellsToJson(result.getResults().get(0).getCells(), nestedTable);
              String s = JavascriptUtils.compileTemplate(template, json);
              RowField rowField = RowField.createInstance(columnStatus.getCustomName(), new Label(s));
              rowField.addColumnDescription(columnStatus.getCustomDescription());

              panel.add(rowField);
            }
          }).findRows(database.getUuid(), database.getUuid(), nestedTable.getSchemaName(), nestedTable.getName(),
            findRequest, LocaleInfo.getCurrentLocale().getLocaleName());
        }
      }
    }
  }

  private String getExportURL(String zipFilename, String filename, boolean description, boolean exportLOBs) {
    return RestUtils.createExportRowUri(database.getUuid(), table.getSchemaName(), table.getName(), row.getUuid(),
      zipFilename, filename, description, exportLOBs);
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
