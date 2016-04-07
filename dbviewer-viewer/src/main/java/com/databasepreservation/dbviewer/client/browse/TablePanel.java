package com.databasepreservation.dbviewer.client.browse;

import com.databasepreservation.dbviewer.ViewerConstants;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.common.lists.TableRowList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TablePanel extends Composite {
  interface TablePanelUiBinder extends UiBinder<Widget, TablePanel> {
  }

  @UiField
  SimplePanel tableContainer;

  @UiField
  SimplePanel searchContainer;

  private ViewerDatabase database;
  private ViewerTable table;

  private TableRowList tableRowList;
  private SearchPanel searchPanel;

  private static TablePanelUiBinder ourUiBinder = GWT.create(TablePanelUiBinder.class);

  public TablePanel(final String databaseUUID, final String tableUUID) {


    initWidget(ourUiBinder.createAndBindUi(this));


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
  }

  private void init() {
    tableRowList = new TableRowList(database, table);

    searchPanel = new SearchPanel(new Filter(), ViewerConstants.SOLR_ROW_SEARCH, "search placeholder", false, false);
    searchPanel.setList(tableRowList);
    searchPanel.setDefaultFilterIncremental(true);

    searchContainer.setWidget(searchPanel);
    tableContainer.setWidget(tableRowList);
  }
}
