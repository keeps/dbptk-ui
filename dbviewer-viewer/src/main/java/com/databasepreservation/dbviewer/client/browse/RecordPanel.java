package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerCell;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerReference;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRow;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.dbviewer.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
public class RecordPanel extends Composite {
  private static Map<String, RecordPanel> instances = new HashMap<>();

  public static RecordPanel getInstance(String databaseUUID, String tableUUID, String recordUUID) {
    return new RecordPanel(databaseUUID, tableUUID, recordUUID);
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, RecordPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private final String recordUUID;
  private ViewerRow record;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;
  @UiField
  HTML content;

  private RecordPanel(final String databaseUUID, final String tableUUID, final String recordUUID) {
    this.recordUUID = recordUUID;
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager
      .updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingRecord(databaseUUID, tableUUID, recordUUID));

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

    BrowserService.Util.getInstance().retrieveRows(ViewerRow.class.getName(), tableUUID, recordUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          record = (ViewerRow) result;
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
        table.getSchemaUUID(), table.getName(), table.getUUID(), recordUUID));

    if (record != null) {
      Set<Integer> columnIndexesContainingForeignKeyRelations = new HashSet<>();

      // get references where this column is source in foreign keys
      for (ViewerForeignKey fk : table.getForeignKeys()) {
        for (ViewerReference viewerReference : fk.getReferences()) {
          columnIndexesContainingForeignKeyRelations.add(viewerReference.getSourceColumnIndex());
        }
      }

      // get references where this column is (at least one of) the target of
      // foreign keys
      for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
        for (ViewerTable viewerTable : viewerSchema.getTables()) {
          for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {
            if (viewerForeignKey.getReferencedTableUUID().equals(table.getUUID())) {
              for (ViewerReference viewerReference : viewerForeignKey.getReferences()) {
                columnIndexesContainingForeignKeyRelations.add(viewerReference.getReferencedColumnIndex());
              }
            }
          }
        }
      }

      // record data
      SafeHtmlBuilder b = new SafeHtmlBuilder();

      for (ViewerColumn column : table.getColumns()) {
        b.append(getCellHTML(column,
          columnIndexesContainingForeignKeyRelations.contains(column.getColumnIndexInEnclosingTable())));
      }

      content.setHTML(b.toSafeHtml());
    }
  }

  private SafeHtml getCellHTML(ViewerColumn column, boolean hasForeignKeyRelations) {
    String label = column.getDisplayName();

    String value = null;
    ViewerCell cell = record.getCells().get(column.getSolrName());
    if (cell != null) {
      if (cell.getValue() != null) {
        value = cell.getValue();
      }
    }

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.appendHtmlConstant("<div class=\"field\">");
    b.appendHtmlConstant("<div class=\"label\">");
    b.appendEscaped(label);
    b.appendHtmlConstant("</div>");
    b.appendHtmlConstant("<div class=\"value\">");
    if (value == null) {
      b.appendEscaped("NULL");
    } else {
      b.appendEscaped(value);
    }
    if (hasForeignKeyRelations && value != null) {
      Hyperlink hyperlink = new Hyperlink("Explore related records", HistoryManager.linkToReferences(
        database.getUUID(), table.getUUID(), recordUUID, String.valueOf(column.getColumnIndexInEnclosingTable())));
      hyperlink.addStyleName("related-records-link");
      b.appendHtmlConstant(hyperlink.toString());
    }
    b.appendHtmlConstant("</div>");
    b.appendHtmlConstant("</div>");
    return b.toSafeHtml();
  }
}
