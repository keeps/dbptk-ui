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
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ForeignKeyPanel extends Composite {
  public static ForeignKeyPanel createInstance(String databaseUUID, String tableUUID, List<String> columnsAndValues) {
    return new ForeignKeyPanel(databaseUUID, tableUUID, columnsAndValues);
  }

  interface ForeignKeyPanelUiBinder extends UiBinder<Widget, ForeignKeyPanel> {
  }

  private static ForeignKeyPanelUiBinder uiBinder = GWT.create(ForeignKeyPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private Map<String, String> columnAndValueMapping;
  private Long rowCount;
  private ViewerRow row;

  @UiField
  SimplePanel panel;

  private ForeignKeyPanel(final String databaseUUID, final String tableUUID, List<String> columnsAndValues) {
    initWidget(uiBinder.createAndBindUi(this));

    // get database and table info early on
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
      null, null, new AsyncCallback<IndexResult<ViewerRow>>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

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
   * Choose and display the correct panel: a RowPanel when search returns one
   * result, otherwise show a TablePanel
   */
  private void init() {
    if (database != null && rowCount != null) {
      if (rowCount == 1) {
        // display a RowPanel
        panel.setWidget(RowPanel.createInstance(database.getUUID(), table.getUUID(), row.getUUID()));
      } else {
        // display a TablePanel
        SearchInfo searchInfo = new SearchInfo(table, columnAndValueMapping);
        panel.setWidget(TablePanel.createInstance(database, table, searchInfo));
      }
    }
  }
}
