package com.databasepreservation.desktop.client.main;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.sidebar.ColumnsManagementSidebar;
import com.databasepreservation.common.client.common.sidebar.DataTransformationSidebar;
import com.databasepreservation.common.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.common.client.common.sidebar.Sidebar;
import com.databasepreservation.common.client.common.utils.ContentPanelLoader;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.utils.RightPanelLoader;
import com.databasepreservation.common.client.common.visualization.browse.DatabasePanel;
import com.databasepreservation.common.client.common.visualization.browse.DatabaseSearchPanel;
import com.databasepreservation.common.client.common.visualization.browse.DatabaseSearchesPanel;
import com.databasepreservation.common.client.common.visualization.browse.ReferencesPanel;
import com.databasepreservation.common.client.common.visualization.browse.RowPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.AdvancedConfiguration;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.ColumnsManagementPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation.DataTransformation;
import com.databasepreservation.common.client.common.visualization.browse.configuration.table.TableManagementPanel;
import com.databasepreservation.common.client.common.visualization.browse.foreignKey.ForeignKeyPanel;
import com.databasepreservation.common.client.common.visualization.browse.foreignKey.ForeignKeyPanelOptions;
import com.databasepreservation.common.client.common.visualization.browse.information.DatabaseInformationPanel;
import com.databasepreservation.common.client.common.visualization.browse.table.TablePanel;
import com.databasepreservation.common.client.common.visualization.browse.table.TablePanelOptions;
import com.databasepreservation.common.client.common.visualization.browse.table.TableSavedSearchEditPanel;
import com.databasepreservation.common.client.common.visualization.browse.table.TableSavedSearchPanel;
import com.databasepreservation.common.client.common.visualization.browse.technicalInformation.ReportPanel;
import com.databasepreservation.common.client.common.visualization.browse.technicalInformation.RoutinesPanel;
import com.databasepreservation.common.client.common.visualization.browse.technicalInformation.UsersPanel;
import com.databasepreservation.common.client.common.visualization.browse.view.ViewPanel;
import com.databasepreservation.common.client.common.visualization.browse.view.ViewPanelStructure;
import com.databasepreservation.common.client.common.visualization.ingest.IngestPage;
import com.databasepreservation.common.client.common.visualization.manager.JobPanel.JobManager;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.common.visualization.manager.databasePanel.admin.DatabaseManage;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanelLoad;
import com.databasepreservation.common.client.common.visualization.metadata.SIARDEditMetadataPage;
import com.databasepreservation.common.client.common.visualization.metadata.information.MetadataInformation;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.routines.MetadataRoutinePanel;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.tables.MetadataTablePanel;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.views.MetadataViewPanel;
import com.databasepreservation.common.client.common.visualization.metadata.users.MetadataUsersPanel;
import com.databasepreservation.common.client.common.visualization.validation.ValidatorPage;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.desktop.client.dbptk.HomePage;
import com.databasepreservation.desktop.client.dbptk.wizard.download.DBMSWizardManager;
import com.databasepreservation.desktop.client.dbptk.wizard.download.SIARDWizardManager;
import com.databasepreservation.desktop.client.dbptk.wizard.upload.CreateWizardManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MainPanelDesktop extends Composite {
  interface MainPanelDesktopUiBinder extends UiBinder<Widget, MainPanelDesktop> {
  }

  private static MainPanelDesktopUiBinder binder = GWT.create(MainPanelDesktopUiBinder.class);

  @UiField
  SimplePanel contentPanel;

  MainPanelDesktop() {
    initWidget(binder.createAndBindUi(this));
  }

  void onHistoryChanged(String token) {
    List<String> currentHistoryPath = HistoryManager.getCurrentHistoryPath();
    List<BreadcrumbItem> breadcrumbItemList = new ArrayList<>();

    if (currentHistoryPath.isEmpty() || HistoryManager.ROUTE_HOME.equals(currentHistoryPath.get(0))) {
      contentPanel.setWidget(HomePage.getInstance());

    } else if (HistoryManager.ROUTE_UPLOAD_SIARD_DATA.equals(currentHistoryPath.get(0))) {
      String databaseUUID = currentHistoryPath.get(1);
      setContent(databaseUUID, new ContentPanelLoader() {
        @Override
        public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
          return IngestPage.getInstance(database);
        }
      });

    } else if (HistoryManager.ROUTE_SIARD_INFO.equals(currentHistoryPath.get(0))) {
      String databaseUUID = currentHistoryPath.get(1);
      setContent(databaseUUID, new ContentPanelLoader() {
        @Override
        public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
          return SIARDManagerPage.getInstance(database);
        }
      });

    } else if (HistoryManager.ROUTE_DATABASE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 1) {
        // #database
        setContent(new ContentPanelLoader() {
          @Override
          public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
            return DatabaseManage.getInstance();
          }
        });
      } else if (currentHistoryPath.size() == 2) {
        // #database/<database_uuid>
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(0), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return DatabaseInformationPanel.getInstance(database);
          }
        });
      } else if (currentHistoryPath.size() == 3
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_REPORT)) {
        // #database/<database_uuid>/report
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return ReportPanel.getInstance(database);
          }
        });
      } else if (currentHistoryPath.size() == 3
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_USERS)) {
        // #database/<database_uuid>/users
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return UsersPanel.getInstance(database);
          }
        });
      } else if (currentHistoryPath.size() == 3
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_SEARCH)) {
        // #database/<database_uuid>/search
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return DatabaseSearchPanel.getInstance(database, status);
          }
        });
      } else if (currentHistoryPath.size() == 3
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_SCHEMA_ROUTINES)) {
        // #database/<database_uuid>/routines
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return RoutinesPanel.getInstance(database);
          }
        });
      }
    } else if (HistoryManager.ROUTE_SAVED_SEARCHES.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 2) {
        // #searches/<databaseUUID>
        final String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(0), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return DatabaseSearchesPanel.createInstance(database);
          }
        });

      } else if (currentHistoryPath.size() == 3) {
        // #searches / < databaseUUID >/<searchUUID >
        final String databaseUUID = currentHistoryPath.get(1);
        final String searchUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(0), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return TableSavedSearchPanel.createInstance(database, searchUUID, status);
          }
        });
      } else if (currentHistoryPath.size() == 4
        && HistoryManager.ROUTE_SAVED_SEARCHES_EDIT.equals(currentHistoryPath.get(3))) {
        // #searches/<databaseUUID>/<searchUUID>/edit
        final String databaseUUID = currentHistoryPath.get(1);
        final String searchUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, currentHistoryPath.get(0), currentHistoryPath.get(0), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return TableSavedSearchEditPanel.createInstance(database, searchUUID);
          }
        });
      } else {
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_VIEW.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        // #view/<databaseUUID>/<viewUUID>
        String databaseUUID = currentHistoryPath.get(1);
        String viewUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, currentHistoryPath.get(0), viewUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return ViewPanel.getInstance(database, viewUUID);
          }
        });
      } else if (currentHistoryPath.size() == 4) {
        // #view/<databaseUUID>/<tableUUID>/options
        String databaseUUID = currentHistoryPath.get(1);
        String viewUUID = currentHistoryPath.get(2);
        final String page = currentHistoryPath.get(3);
        if (page.equals(HistoryManager.ROUTE_TABLE_OPTIONS)) {
          setContent(databaseUUID, currentHistoryPath.get(0), viewUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database, CollectionStatus status) {
              return ViewPanelStructure.getInstance(database, viewUUID);
            }
          });
        } else {
          // #table/...
          handleErrorPath(currentHistoryPath);
        }
      } else {
        // #table/...
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_TABLE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 5) {
        // #table/<databaseUUID>/data/<schema>/<table>
        String databaseUUID = currentHistoryPath.get(1);
        final String tableId = currentHistoryPath.get(3) + "." + currentHistoryPath.get(4);
        setContent(databaseUUID, currentHistoryPath.get(0), tableId, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return TablePanel.getInstance(status, database, tableId, currentHistoryPath.get(0));
          }
        });

      } else if (currentHistoryPath.size() == 6) {
        String databaseUUID = currentHistoryPath.get(1);
        final String tableId = currentHistoryPath.get(3) + "." + currentHistoryPath.get(4);
        final String page = currentHistoryPath.get(5);
        if (page.equals(HistoryManager.ROUTE_TABLE_OPTIONS)) {
          // #table/<databaseUUID>/data/<schema>/<table>/options
          setContent(databaseUUID, currentHistoryPath.get(0), tableId, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database, CollectionStatus status) {
              return TablePanelOptions.getInstance(status, database, tableId);
            }
          });
        } else {
          /// #table/<databaseUUID>/data/<schema>/<table>/<searchInfoJSON>
          setContent(databaseUUID, currentHistoryPath.get(0), tableId, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database, CollectionStatus status) {
              return TablePanel.getInstance(status, database, tableId, page);
            }
          });
        }
      } else {
        // #table/...
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_RECORD.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 6) {
        // #record/<databaseUUID>/data/<schema>/<table>/<rowIndex>
        String databaseUUID = currentHistoryPath.get(1);
        final String tableId = currentHistoryPath.get(3) + "." + currentHistoryPath.get(4);
        final String recordUUID = currentHistoryPath.get(5);
        setContent(databaseUUID, currentHistoryPath.get(0), tableId, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return RowPanel.createInstance(database, tableId, recordUUID, status);
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
        setContent(databaseUUID, currentHistoryPath.get(0), tableUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return ReferencesPanel.getInstance(database, tableUUID, recordUUID, columnIndex, status);
          }
        });

      } else {
        // #references/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_FOREIGN_KEY.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() >= 7) {
        // #foreignkey/<databaseUUID>/data/<schema>/<table>/<col1>/<val1>/<col2>/<val2>/<colN>/<valN>/...
        // minimum: #foreignkey/<databaseUUID>/data/<schema>/<table>/<col1>/<val1>
        final String databaseUUID = currentHistoryPath.get(1);
        final String tableID = currentHistoryPath.get(3) + "." + currentHistoryPath.get(4);
        final List<String> columnsAndValues = currentHistoryPath.subList(5, currentHistoryPath.size());
        String page = columnsAndValues.get(columnsAndValues.size() - 1);
        if (page.equals(HistoryManager.ROUTE_TABLE_OPTIONS)) {
          setContent(databaseUUID, currentHistoryPath.get(0), tableID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database, CollectionStatus status) {
              return ForeignKeyPanelOptions.getInstance(database, status,tableID,
                columnsAndValues.subList(0, columnsAndValues.size() - 1));
            }
          });
        } else if (columnsAndValues.size() % 2 == 0) {
          setContent(databaseUUID, currentHistoryPath.get(0), tableID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database, CollectionStatus status) {
              return ForeignKeyPanel.createInstance(database, tableID, columnsAndValues, status);
            }
          });
        } else {
          handleErrorPath(currentHistoryPath);
        }
      } else {
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_SEND_TO_LIVE_DBMS.equals(currentHistoryPath.get(0))) {
      DBMSWizardManager instance = DBMSWizardManager.getInstance(currentHistoryPath.get(1));
      if (currentHistoryPath.size() == 3) {
        final String databaseName = currentHistoryPath.get(2);
        instance.setBreadcrumbDatabaseName(databaseName);
      }
      if (currentHistoryPath.size() == 4) {
        final String wizardPage = currentHistoryPath.get(2);
        final String toSelect = currentHistoryPath.get(3);
        instance.change(wizardPage, toSelect);
      }
      contentPanel.setWidget(instance);
    } else if (HistoryManager.ROUTE_MIGRATE_TO_SIARD.equals(currentHistoryPath.get(0))) {
      SIARDWizardManager instance = SIARDWizardManager.getInstance(currentHistoryPath.get(1),
        currentHistoryPath.get(2));
      if (currentHistoryPath.size() == 4) {
        final String wizardPage = currentHistoryPath.get(2);
        final String toSelect = currentHistoryPath.get(3);
        instance.change(wizardPage, toSelect);
      } else if (currentHistoryPath.size() == 5) {
        final String wizardPage = currentHistoryPath.get(2);
        final String toSelect = currentHistoryPath.get(3);
        final String schemaUUID = currentHistoryPath.get(4);
        instance.change(wizardPage, toSelect, schemaUUID);
      } else if (currentHistoryPath.size() == 6) {
        final String wizardPage = currentHistoryPath.get(2);
        final String toSelect = currentHistoryPath.get(3);
        final String schemaUUID = currentHistoryPath.get(4);
        final String tableUUID = currentHistoryPath.get(5);
        instance.change(wizardPage, toSelect, schemaUUID, tableUUID);
      }
      contentPanel.setWidget(instance);
    } else if (HistoryManager.ROUTE_CREATE_SIARD.equals(currentHistoryPath.get(0))) {
      CreateWizardManager instance = CreateWizardManager.getInstance();
      if (currentHistoryPath.size() == 3) {
        instance.change(currentHistoryPath.get(1), currentHistoryPath.get(2));
      } else if (currentHistoryPath.size() == 4) {
        final String wizardPage = currentHistoryPath.get(1);
        final String toSelect = currentHistoryPath.get(2);
        final String schemaUUID = currentHistoryPath.get(3);
        instance.change(wizardPage, toSelect, schemaUUID);
      } else if (currentHistoryPath.size() == 5) {
        final String wizardPage = currentHistoryPath.get(1);
        final String toSelect = currentHistoryPath.get(2);
        final String schemaUUID = currentHistoryPath.get(3);
        final String tableUUID = currentHistoryPath.get(4);
        instance.change(wizardPage, toSelect, schemaUUID, tableUUID);
      }
      contentPanel.setWidget(instance);
    } else if (HistoryManager.ROUTE_SIARD_EDIT_METADATA.equals(currentHistoryPath.get(0))) {
      String databaseUUID = currentHistoryPath.get(1);
      if (currentHistoryPath.size() == 2) {
        setMetadataRightPanelContent(databaseUUID, databaseUUID, new MetadataPanelLoad() {
          @Override
          public MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
            return MetadataInformation.getInstance(database, SIARDbundle);
          }
        });
      } else if (currentHistoryPath.size() == 3) {
        final String user = currentHistoryPath.get(2);
        setMetadataRightPanelContent(databaseUUID, user, new MetadataPanelLoad() {
          @Override
          public MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
            return MetadataUsersPanel.getInstance(database, SIARDbundle);
          }
        });
      }
    } else if (HistoryManager.ROUTE_DESKTOP_METADATA_TABLE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);

        setMetadataRightPanelContent(databaseUUID, tableUUID, new MetadataPanelLoad() {
          @Override

          public MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
            return MetadataTablePanel.getInstance(database, SIARDbundle, tableUUID);
          }
        });
      }
    } else if (HistoryManager.ROUTE_DESKTOP_METADATA_VIEW.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 4) {
        String databaseUUID = currentHistoryPath.get(1);
        final String schemaUUID = currentHistoryPath.get(2);
        final String viewUUID = currentHistoryPath.get(3);

        setMetadataRightPanelContent(databaseUUID, viewUUID, new MetadataPanelLoad() {
          @Override
          public MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
            return MetadataViewPanel.getInstance(database, SIARDbundle, schemaUUID, viewUUID);
          }
        });
      }
    } else if (HistoryManager.ROUTE_DESKTOP_METADATA_ROUTINE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 4) {
        String databaseUUID = currentHistoryPath.get(1);
        final String schemaUUID = currentHistoryPath.get(2);
        final String routineUUID = currentHistoryPath.get(3);

        setMetadataRightPanelContent(databaseUUID, routineUUID, new MetadataPanelLoad() {
          @Override
          public MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
            return MetadataRoutinePanel.getInstance(database, SIARDbundle, schemaUUID, routineUUID);
          }
        });
      }
    } else if (HistoryManager.ROUTE_SIARD_VALIDATOR.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 4) {
        final String databaseUUID = currentHistoryPath.get(1);
        final String reporterPath = currentHistoryPath.get(2);
        final String skipAdditionalChecks = currentHistoryPath.get(3);

        setContent(databaseUUID, new ContentPanelLoader() {
          @Override
          public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
            return ValidatorPage.getInstance(database, reporterPath, skipAdditionalChecks);
          }
        });

      } else if (currentHistoryPath.size() == 5) {
        final String databaseUUID = currentHistoryPath.get(1);
        final String reporterPath = currentHistoryPath.get(2);
        final String udtPath = currentHistoryPath.get(3);
        final String skipAdditionalChecks = currentHistoryPath.get(4);

        setContent(databaseUUID, new ContentPanelLoader() {
          @Override
          public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
            return ValidatorPage.getInstance(database, reporterPath, udtPath, skipAdditionalChecks);
          }
        });
      }
    } else if (HistoryManager.ROUTE_JOBS.equals(currentHistoryPath.get(0))) {
      setContent(new ContentPanelLoader() {
        @Override
        public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
          return JobManager.getInstance();
        }
      });
    } else if (HistoryManager.ROUTE_ADVANCED_CONFIGURATION.equals(currentHistoryPath.get(0))) {
      final String databaseUUID = currentHistoryPath.get(1);
      setContent(databaseUUID, new ContentPanelLoader() {
        @Override
        public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
          return AdvancedConfiguration.getInstance(database);
        }
      });
    } else if (HistoryManager.ROUTE_TABLE_MANAGEMENT.equals(currentHistoryPath.get(0))) {
      final String databaseUUID = currentHistoryPath.get(1);
      setContent(databaseUUID, new ContentPanelLoader() {
        @Override
        public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
          return TableManagementPanel.getInstance(database, status);
        }
      });
    } else if (HistoryManager.ROUTE_COLUMNS_MANAGEMENT.equals(currentHistoryPath.get(0))) {
      final String databaseUUID = currentHistoryPath.get(1);
      Sidebar sidebar = ColumnsManagementSidebar.getInstance(databaseUUID);
      if (currentHistoryPath.size() == 2) {
        // columns-management/<databaseUUID>
        setContent(databaseUUID, HistoryManager.ROUTE_COLUMNS_MANAGEMENT, databaseUUID, sidebar,
          new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database, CollectionStatus status) {
              return ColumnsManagementPanel.getInstance(status, database, null, sidebar);
            }
          });
      } else if (currentHistoryPath.size() == 4) {
        // columns-management/<databaseUUID>/<schema>/<table>
        final String tableId = currentHistoryPath.get(2) + "." + currentHistoryPath.get(3);
        setContent(databaseUUID, HistoryManager.ROUTE_COLUMNS_MANAGEMENT, tableId, sidebar, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return ColumnsManagementPanel.getInstance(status, database, tableId, sidebar);
          }
        });
      }
    } else {
      handleErrorPath(currentHistoryPath);
    }
  }

  private void setMetadataRightPanelContent(String databaseUUID, String sidebarSelected,
    MetadataPanelLoad rightPanelLoader) {
    SIARDEditMetadataPage instance = SIARDEditMetadataPage.getInstance(databaseUUID);
    contentPanel.setWidget(instance);
    instance.load(rightPanelLoader, sidebarSelected);
  }

  private void handleErrorPath(List<String> currentHistoryPath) {
    HistoryManager.gotoHome();
  }

  /*
   * History change handling
   *
   * (switching to another page = using a different RightPanel)
   * ____________________________________________________________________________________________________________________
   */
  public void setContent(ContentPanelLoader panel) {
    setContent(null, panel);
  }

  private void setContent(String databaseUUID, String route, String toSelect, Sidebar sidebar,
    RightPanelLoader rightPanelLoader) {
    GWT.log("setContent, dbuid " + databaseUUID);
    DatabasePanel databasePanel = DatabasePanel.getInstance(databaseUUID, route, true, sidebar);
    databasePanel.setTopLevelPanelCSS("browseContent wrapper skip_padding");
    contentPanel.setWidget(databasePanel);
    databasePanel.load(rightPanelLoader, toSelect);
    JavascriptUtils.scrollToElement(contentPanel.getElement());
  }

  private void setContent(String databaseUUID, String route, String toSelect, RightPanelLoader rightPanelLoader) {
    GWT.log("setContent, dbuid " + databaseUUID);

    Sidebar sidebar = DatabaseSidebar.getInstance(databaseUUID);
    DatabasePanel containerPanel = DatabasePanel.getInstance(databaseUUID, route, true, sidebar);
    containerPanel.setTopLevelPanelCSS("browseContent wrapper skip_padding");
    contentPanel.setWidget(containerPanel);
    containerPanel.load(rightPanelLoader, toSelect);
    JavascriptUtils.scrollToElement(contentPanel.getElement());
  }

  private void setContent(String databaseUUID, ContentPanelLoader panelLoader) {
    DatabasePanel containerPanel;
    if (databaseUUID == null) {
      containerPanel = DatabasePanel.getInstance(true);
    } else {
      containerPanel = DatabasePanel.getInstance(databaseUUID, true);
    }
    contentPanel.setWidget(containerPanel);
    containerPanel.load(panelLoader);
    containerPanel.setTopLevelPanelCSS("browseContent wrapper skip_padding server");

  }

  private void setContent(String databaseUUID, String sidebarSelected, MetadataPanelLoad rightPanelLoader) {
    SIARDEditMetadataPage instance = SIARDEditMetadataPage.getInstance(databaseUUID);
    instance.setTopLevelPanelCSS("browseContent wrapper skip_padding server");
    contentPanel.setWidget(instance);
    instance.load(rightPanelLoader, sidebarSelected);
  }
}
