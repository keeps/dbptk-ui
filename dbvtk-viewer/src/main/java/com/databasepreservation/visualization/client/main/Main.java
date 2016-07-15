package com.databasepreservation.visualization.client.main;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.visualization.client.browse.DatabaseListPanel;
import com.databasepreservation.visualization.client.browse.DatabasePanel;
import com.databasepreservation.visualization.client.browse.DatabaseUsersPanel;
import com.databasepreservation.visualization.client.browse.ForeignKeyPanel;
import com.databasepreservation.visualization.client.browse.ReferencesPanel;
import com.databasepreservation.visualization.client.browse.RowPanel;
import com.databasepreservation.visualization.client.browse.SchemaCheckConstraintsPanel;
import com.databasepreservation.visualization.client.browse.SchemaRoutinesPanel;
import com.databasepreservation.visualization.client.browse.SchemaStructurePanel;
import com.databasepreservation.visualization.client.browse.SchemaTriggersPanel;
import com.databasepreservation.visualization.client.browse.SchemaViewsPanel;
import com.databasepreservation.visualization.client.browse.TablePanel;
import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Main implements EntryPoint {
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private MainPanel mainPanel;
  private Footer footer;

  private DatabaseListPanel databaseListPanel = null;

  private void setContent(Widget w) {
    mainPanel.contentPanel.setWidget(w);
  }

  /**
   * Create a new main
   */
  public Main() {
    mainPanel = new MainPanel();
    footer = new Footer();

    // header = new HTMLPanel(new SafeHtml() {
    // @Override public String asString() {
    // return
    // "<h2 style=\"padding-left:2em;padding-right:2em;border-bottom:2px solid black;margin-bottom:0px\">Database Viewer (prototype)</h2>";
    // }
    // });
    //
    // contentRow = new FlowPanel();
    // contentRow.addStyleName("row full_width skip_padding");
    //
    // contentCol = new FlowPanel();
    // contentCol.addStyleName("col_12 last");
    //
    //
    // footer = new HTMLPanel("div",
    // "<p style=\"text-align:center\">footer</p>");
    // footer.addStyleName("footer");
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

    RootPanel.get().add(mainPanel);
    RootPanel.get().add(footer);
    RootPanel.get().addStyleName("roda");

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
    List<BreadcrumbItem> breadcrumbItemList = new ArrayList<>();

    changeHeader(currentHistoryPath);
    if (currentHistoryPath.isEmpty()) {
      // #
      setContent(getDatabaseListPanel());

    } else if (HistoryManager.ROUTE_DATABASE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 1) {
        // #database
        setContent(getDatabaseListPanel());

      } else if (currentHistoryPath.size() == 2) {
        // #database/<id>
        DatabasePanel panel = DatabasePanel.getInstance(currentHistoryPath.get(1));
        setContent(panel);

      } else if (currentHistoryPath.size() == 3
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_USERS)) {
        // #database/<id>/users
        DatabaseUsersPanel panel = DatabaseUsersPanel.getInstance(currentHistoryPath.get(1));
        setContent(panel);

      } else {
        // #database/...
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_SCHEMA.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        // #schema/<database_uuid>/<schema_uuid>
        String database_uuid = currentHistoryPath.get(1);
        String schema_uuid = currentHistoryPath.get(2);
        SchemaStructurePanel panel = SchemaStructurePanel.getInstance(database_uuid, schema_uuid);
        setContent(panel);

      } else if (currentHistoryPath.size() == 4) {
        // #schema/<database_uuid>/<schema_uuid>/structure
        // #schema/<database_uuid>/<schema_uuid>/routines
        // #schema/<database_uuid>/<schema_uuid>/triggers
        // #schema/<database_uuid>/<schema_uuid>/views
        String database_uuid = currentHistoryPath.get(1);
        String schema_uuid = currentHistoryPath.get(2);
        String pageSpec = currentHistoryPath.get(3);

        Widget pageWidget = null;
        switch (pageSpec) {
          case HistoryManager.ROUTE_SCHEMA_STRUCTURE:
            pageWidget = SchemaStructurePanel.getInstance(database_uuid, schema_uuid);
            break;
          case HistoryManager.ROUTE_SCHEMA_ROUTINES:
            pageWidget = SchemaRoutinesPanel.getInstance(database_uuid, schema_uuid);
            break;
          case HistoryManager.ROUTE_SCHEMA_TRIGGERS:
            pageWidget = SchemaTriggersPanel.getInstance(database_uuid, schema_uuid);
            break;
          case HistoryManager.ROUTE_SCHEMA_VIEWS:
            pageWidget = SchemaViewsPanel.getInstance(database_uuid, schema_uuid);
            break;
          case HistoryManager.ROUTE_SCHEMA_CHECK_CONSTRAINTS:
            pageWidget = SchemaCheckConstraintsPanel.getInstance(database_uuid, schema_uuid);
            break;
        }
        if (pageWidget != null) {
          setContent(pageWidget);
        } else {
          // #schema/<database_uuid>/<schema_uuid>/*invalid-page*
          handleErrorPath(currentHistoryPath);
        }

      } else {
        // #schema/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_TABLE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        // #table/<database_uuid>/<table_uuid>
        String database_uuid = currentHistoryPath.get(1);
        String table_uuid = currentHistoryPath.get(2);
        TablePanel panel = TablePanel.getInstance(database_uuid, table_uuid);
        setContent(panel);

      } else if (currentHistoryPath.size() == 4) {
        // #table/<database_uuid>/<table_uuid>/<searchInfoJSON>
        String database_uuid = currentHistoryPath.get(1);
        String table_uuid = currentHistoryPath.get(2);
        String searchInfo = currentHistoryPath.get(3);
        TablePanel panel = TablePanel.getInstance(database_uuid, table_uuid, searchInfo);
        setContent(panel);

      } else {
        // #table/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_RECORD.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 4) {
        // #record/<database_uuid>/<table_uuid>/<record_uuid>
        String database_uuid = currentHistoryPath.get(1);
        String table_uuid = currentHistoryPath.get(2);
        String record_uuid = currentHistoryPath.get(3);
        RowPanel panel = RowPanel.createInstance(database_uuid, table_uuid, record_uuid);
        setContent(panel);

      } else {
        // #record/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_REFERENCES.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 5) {
        // #references/<database_uuid>/<table_uuid>/<record_uuid>/<columnIndex>
        String database_uuid = currentHistoryPath.get(1);
        String table_uuid = currentHistoryPath.get(2);
        String record_uuid = currentHistoryPath.get(3);
        String columnIndex = currentHistoryPath.get(4);
        ReferencesPanel panel = ReferencesPanel.getInstance(database_uuid, table_uuid, record_uuid, columnIndex);
        setContent(panel);

      } else {
        // #references/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_FOREIGN_KEY.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() >= 5) {
        // #foreignkey/<database_uuid>/<table_uuid>/<col1>/<val1>/<col2>/<val2>/<colN>/<valN>/...
        // minimum: #foreignkey/<database_uuid>/<table_uuid>/<col1>/<val1>
        String database_uuid = currentHistoryPath.get(1);
        String table_uuid = currentHistoryPath.get(2);
        List<String> columnsAndValues = currentHistoryPath.subList(3, currentHistoryPath.size());

        ForeignKeyPanel panel = ForeignKeyPanel.createInstance(database_uuid, table_uuid, columnsAndValues);
        setContent(panel);
      } else {
        handleErrorPath(currentHistoryPath);
      }
    }
  }

  private void changeHeader(String databaseUUID) {
    mainPanel.reSetHeader(databaseUUID);
  }

  private void changeHeader(List<String> currentHistoryPath) {
    if (currentHistoryPath.size() >= 2) {
      changeHeader(currentHistoryPath.get(1));
    } else {
      changeHeader((String) null);
    }
  }

  private void handleErrorPath(List<String> currentHistoryPath) {
    if (currentHistoryPath.size() >= 2) {
      String database_uuid = currentHistoryPath.get(1);
      changeHeader(database_uuid);
      HistoryManager.gotoDatabase(database_uuid);
    } else {
      HistoryManager.gotoRoot();
    }
  }
}
