package com.databasepreservation.visualization.client.main;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.browse.DatabaseInformationPanel;
import com.databasepreservation.visualization.client.browse.DatabaseListPanel;
import com.databasepreservation.visualization.client.browse.DatabasePanel;
import com.databasepreservation.visualization.client.browse.DatabaseSearchPanel;
import com.databasepreservation.visualization.client.browse.DatabaseSearchesPanel;
import com.databasepreservation.visualization.client.browse.DatabaseUsersPanel;
import com.databasepreservation.visualization.client.browse.ForeignKeyPanel;
import com.databasepreservation.visualization.client.browse.ReferencesPanel;
import com.databasepreservation.visualization.client.browse.RightPanel;
import com.databasepreservation.visualization.client.browse.RowPanel;
import com.databasepreservation.visualization.client.browse.SchemaCheckConstraintsPanel;
import com.databasepreservation.visualization.client.browse.SchemaDataPanel;
import com.databasepreservation.visualization.client.browse.SchemaRoutinesPanel;
import com.databasepreservation.visualization.client.browse.SchemaStructurePanel;
import com.databasepreservation.visualization.client.browse.SchemaTriggersPanel;
import com.databasepreservation.visualization.client.browse.SchemaViewsPanel;
import com.databasepreservation.visualization.client.browse.TablePanel;
import com.databasepreservation.visualization.client.browse.TableSavedSearchEditPanel;
import com.databasepreservation.visualization.client.browse.TableSavedSearchPanel;
import com.databasepreservation.visualization.client.common.utils.RightPanelLoader;
import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
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

  private void setContent(String databaseUUID, RightPanelLoader rightPanelLoader) {
    DatabasePanel databasePanel = DatabasePanel.getInstance(databaseUUID);
    mainPanel.contentPanel.setWidget(databasePanel);
    databasePanel.load(rightPanelLoader);
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

    // Set uncaught exception handler
    ClientLogger.setUncaughtExceptionHandler();

    // Remove loading image
    RootPanel.getBodyElement().removeChild(DOM.getElementById("loading"));

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
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseInformationPanel.getInstance(database);
          }
        });

      } else if (currentHistoryPath.size() == 3
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_USERS)) {
        // #database/<id>/users
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseUsersPanel.getInstance(database);
          }
        });

      } else if (currentHistoryPath.size() == 3
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_SEARCH)) {
        // #database/<id>/search
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseSearchPanel.getInstance(database);
          }
        });

      } else {
        // #database/...
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_SCHEMA.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        // #schema/<databaseUUID>/<schema_uuid>
        String databaseUUID = currentHistoryPath.get(1);
        final String schema_uuid = currentHistoryPath.get(2);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return SchemaStructurePanel.getInstance(database, schema_uuid);
          }
        });

      } else if (currentHistoryPath.size() == 4) {
        // #schema/<databaseUUID>/<schema_uuid>/structure
        // #schema/<databaseUUID>/<schema_uuid>/routines
        // #schema/<databaseUUID>/<schema_uuid>/triggers
        // #schema/<databaseUUID>/<schema_uuid>/views
        String databaseUUID = currentHistoryPath.get(1);
        final String schema_uuid = currentHistoryPath.get(2);
        String pageSpec = currentHistoryPath.get(3);

        switch (pageSpec) {
          case HistoryManager.ROUTE_SCHEMA_STRUCTURE:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaStructurePanel.getInstance(database, schema_uuid);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_ROUTINES:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaRoutinesPanel.getInstance(database, schema_uuid);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_TRIGGERS:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaTriggersPanel.getInstance(database, schema_uuid);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_VIEWS:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaViewsPanel.getInstance(database, schema_uuid);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_CHECK_CONSTRAINTS:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaCheckConstraintsPanel.getInstance(database, schema_uuid);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_DATA:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaDataPanel.getInstance(database, schema_uuid);
              }
            });
            break;
          default:
            // #schema/<databaseUUID>/<schema_uuid>/*invalid-page*
            handleErrorPath(currentHistoryPath);
        }

      } else {
        // #schema/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_TABLE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        // #table/<databaseUUID>/<tableUUID>
        String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return TablePanel.getInstance(database, tableUUID);
          }
        });

      } else if (currentHistoryPath.size() == 4) {
        // #table/<databaseUUID>/<tableUUID>/<searchInfoJSON>
        String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);
        final String searchInfo = currentHistoryPath.get(3);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return TablePanel.getInstance(database, tableUUID, searchInfo);
          }
        });

      } else {
        // #table/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_RECORD.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 4) {
        // #record/<databaseUUID>/<tableUUID>/<recordUUID>
        String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);
        final String recordUUID = currentHistoryPath.get(3);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return RowPanel.createInstance(database, tableUUID, recordUUID);
          }
        });

      } else {
        // #record/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_REFERENCES.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 5) {
        // #references/<databaseUUID>/<tableUUID>/<recordUUID>/<columnIndex>
        String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);
        final String recordUUID = currentHistoryPath.get(3);
        final String columnIndex = currentHistoryPath.get(4);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return ReferencesPanel.getInstance(database, tableUUID, recordUUID, columnIndex);
          }
        });

      } else {
        // #references/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_FOREIGN_KEY.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() >= 5) {
        // #foreignkey/<databaseUUID>/<tableUUID>/<col1>/<val1>/<col2>/<val2>/<colN>/<valN>/...
        // minimum: #foreignkey/<databaseUUID>/<tableUUID>/<col1>/<val1>
        final String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);
        final List<String> columnsAndValues = currentHistoryPath.subList(3, currentHistoryPath.size());
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return ForeignKeyPanel.createInstance(database, tableUUID, columnsAndValues);
          }
        });

      } else {
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_SAVED_SEARCHES.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 2) {
        // #searches/<databaseUUID>
        final String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseSearchesPanel.createInstance(database);
          }
        });

      } else if (currentHistoryPath.size() == 3) {
        // #searches/<databaseUUID>/<searchUUID>
        final String databaseUUID = currentHistoryPath.get(1);
        final String searchUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return TableSavedSearchPanel.createInstance(database, searchUUID);
          }
        });

      } else if (currentHistoryPath.size() == 4
        && HistoryManager.ROUTE_SAVED_SEARCHES_EDIT.equals(currentHistoryPath.get(3))) {
        // #searches/<databaseUUID>/<searchUUID>/edit
        final String databaseUUID = currentHistoryPath.get(1);
        final String searchUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return TableSavedSearchEditPanel.createInstance(database, searchUUID);
          }
        });

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
      String databaseUUID = currentHistoryPath.get(1);
      changeHeader(databaseUUID);
      HistoryManager.gotoDatabase(databaseUUID);
    } else {
      HistoryManager.gotoRoot();
    }
  }
}
