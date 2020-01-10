package com.databasepreservation.common.client.common.visualization.browse.foreignKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.common.visualization.browse.RowPanel;
import com.databasepreservation.common.client.common.visualization.browse.table.TablePanel;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ForeignKeyPanel extends RightPanel {
  public static ForeignKeyPanel createInstance(ViewerDatabase database, String tableUUID,
                                               List<String> columnsAndValues) {
    return new ForeignKeyPanel(database, tableUUID, columnsAndValues, false);
  }

  public static ForeignKeyPanel createInstance(ViewerDatabase database, String tableUUID,
    List<String> columnsAndValues, boolean update) {
    return new ForeignKeyPanel(database, tableUUID, columnsAndValues, update);
  }

  interface ForeignKeyPanelUiBinder extends UiBinder<Widget, ForeignKeyPanel> {
  }

  private static ForeignKeyPanelUiBinder uiBinder = GWT.create(ForeignKeyPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private Map<String, String> columnAndValueMapping;
  private List<String> columnsAndValues;
  private Long rowCount;
  private ViewerRow row;
  private boolean toUpdate;

  private RightPanel innerRightPanel = null;
  private BreadcrumbPanel breadcrumb = null;

  @UiField
  SimplePanel panel;

  private ForeignKeyPanel(ViewerDatabase viewerDatabase, final String tableUUID, List<String> columnsAndValues, boolean update) {
    database = viewerDatabase;
    table = database.getMetadata().getTable(tableUUID);
    toUpdate = update;
    this.columnsAndValues = columnsAndValues;
    initWidget(uiBinder.createAndBindUi(this));

    // prepare search
    columnAndValueMapping = new HashMap<>();
    List<FilterParameter> filterParameters = new ArrayList<>();
    String solrColumnName = null;
    for (String columnOrValue : columnsAndValues) {
      if (solrColumnName == null) {
        solrColumnName = columnOrValue;
      } else {
        columnAndValueMapping.put(solrColumnName, columnOrValue);
        filterParameters.add(new SimpleFilterParameter(solrColumnName, columnOrValue));
        solrColumnName = null;
      }
    }
    Filter filter = new Filter(filterParameters);

    // search (count)
    FindRequest findRequest = new FindRequest(ViewerRow.class.getName(), filter, null, new Sublist(0, 1), null);
    DatabaseService.Util.call((IndexResult<ViewerRow> result)->{
      rowCount = result.getTotalCount();
      if (rowCount >= 1) {
        row = result.getResults().get(0);
      }
      init();
    }).findRows(database.getUuid(), findRequest, null);
  }

  /**
   * Delegates the method to the innerRightPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    // set this in case handleBreadcrumb happens before init
    if (breadcrumb != null) {
      this.breadcrumb = breadcrumb;
    }

    // this will be true if init has already run when handleBreadcrumb is called
    // externally; or handleBreadcrumb was called first and init is finishing up
    if (innerRightPanel != null && breadcrumb != null) {
      innerRightPanel.handleBreadcrumb(breadcrumb);
    }
  }

  /**
   * Choose and display the correct panel: a RowPanel when search returns one
   * result, otherwise show a TablePanel
   */
  private void init() {
    if (rowCount != null) {
      if (rowCount == 1) {
        // display a RowPanel
        innerRightPanel = RowPanel.createInstance(database, table, row);

      } else {
        // display a TablePanel
        SearchInfo searchInfo = new SearchInfo(table, columnAndValueMapping);
        TablePanel tablePanel = TablePanel.createInstance(null, database, table, searchInfo, HistoryManager.ROUTE_FOREIGN_KEY);
        tablePanel.setColumnsAndValues(columnsAndValues);
        if (toUpdate) {
          tablePanel.update();
        }
        innerRightPanel = tablePanel;
      }

      handleBreadcrumb(breadcrumb);
      panel.setWidget(innerRightPanel);
    }
  }
}
