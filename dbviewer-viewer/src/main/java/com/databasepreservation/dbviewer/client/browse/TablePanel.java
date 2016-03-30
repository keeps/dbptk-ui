package com.databasepreservation.dbviewer.client.browse;

import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.common.lists.TableRowList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TablePanel extends Composite {
  interface TablePanelUiBinder extends UiBinder<HTMLPanel, TablePanel> {
  }

  @UiField(provided = true)
  TableRowList tableRowList;

  private ViewerDatabase database;
  private ViewerTable table;

  private TablePanel self;

  private static TablePanelUiBinder ourUiBinder = GWT.create(TablePanelUiBinder.class);

  public TablePanel(final String databaseUUID, final String tableUUID) {
    self = this;

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

          tableRowList = new TableRowList(database, table);
          initWidget(ourUiBinder.createAndBindUi(self));
        }
      });
  }
}
