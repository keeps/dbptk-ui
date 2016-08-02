package com.databasepreservation.visualization.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.index.IndexResult;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ForeignKeyPanel extends RightPanel {
  public static ForeignKeyPanel createInstance(ViewerDatabase database, String tableUUID, List<String> columnsAndValues) {
    return new ForeignKeyPanel(database, tableUUID, columnsAndValues);
  }

  interface ForeignKeyPanelUiBinder extends UiBinder<Widget, ForeignKeyPanel> {
  }

  private static ForeignKeyPanelUiBinder uiBinder = GWT.create(ForeignKeyPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private Map<String, String> columnAndValueMapping;
  private Long rowCount;
  private ViewerRow row;

  private RightPanel innerRightPanel = null;
  private BreadcrumbPanel breadcrumb = null;

  @UiField
  SimplePanel panel;

  private ForeignKeyPanel(ViewerDatabase viewerDatabase, final String tableUUID, List<String> columnsAndValues) {
    database = viewerDatabase;
    table = database.getMetadata().getTable(tableUUID);

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
    BrowserService.Util.getInstance().findRows(ViewerRow.class.getName(), tableUUID, filter, null, new Sublist(0, 1),
      null, null, new DefaultAsyncCallback<IndexResult<ViewerRow>>() {
        @Override
        public void onSuccess(IndexResult<ViewerRow> result) {
          rowCount = result.getTotalCount();
          if (rowCount >= 1) {
            row = result.getResults().get(0);
          }
          init();
        }
      });
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
        innerRightPanel = TablePanel.createInstance(database, table, searchInfo);
      }

      handleBreadcrumb(breadcrumb);
      panel.setWidget(innerRightPanel);
    }
  }
}
