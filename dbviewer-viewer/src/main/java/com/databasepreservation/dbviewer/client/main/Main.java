package com.databasepreservation.dbviewer.client.main;

import java.util.List;

import com.databasepreservation.dbviewer.client.browse.DatabaseListPanel;
import com.databasepreservation.dbviewer.client.browse.DatabasePanel;
import com.databasepreservation.dbviewer.client.browse.TablePanel;
import com.databasepreservation.dbviewer.shared.client.ClientLogger;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Main implements EntryPoint {
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private SimplePanel content;
  private DatabaseListPanel databaseListPanel = null;

  /**
   * Create a new main
   */
  public Main() {

  }

  public DatabaseListPanel getDatabaseListPanel() {
    if (databaseListPanel == null) {
      databaseListPanel = new DatabaseListPanel();
    }
    return databaseListPanel;
  }

  /**
   * The entry point method, called automatically by loading a module that
   * declares an implementing class as an entry point.
   */
  @Override
  public void onModuleLoad() {
    content = new SimplePanel();
    RootPanel.get().add(content);

    onHistoryChanged(History.getToken());
    History.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        onHistoryChanged(event.getValue());
      }
    });
  }

  private void onHistoryChanged(String token) {
    List<String> currentHistoryPath = HistoryManager.getCurrentHistoryPath();
    if (currentHistoryPath.isEmpty()) {
      // #
      content.setWidget(getDatabaseListPanel());
    } else if (HistoryManager.ROUTE_DATABASE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 1) {
        // #database
        content.setWidget(getDatabaseListPanel());
      } else if (currentHistoryPath.size() == 2) {
        // #database/<id>
        DatabasePanel panel = new DatabasePanel(currentHistoryPath.get(1));
        content.setWidget(panel);
      } else {
        // #database/<id>/...
        HistoryManager.gotoRoot();
      }
    } else if (HistoryManager.ROUTE_TABLE.equals(currentHistoryPath.get(0))){
      if(currentHistoryPath.size() == 3){
        // #table/<database_uuid>/<table_uuid>
        String database_uuid = currentHistoryPath.get(1);
        String table_uuid = currentHistoryPath.get(2);
        TablePanel panel = new TablePanel(database_uuid, table_uuid);
        content.setWidget(panel);
      }else{
        // #table/...
        // (except the case above)
        HistoryManager.gotoRoot();
      }
    }
  }
}
