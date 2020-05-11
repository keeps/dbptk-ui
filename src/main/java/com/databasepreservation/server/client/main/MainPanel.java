package com.databasepreservation.server.client.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.sidebar.ColumnsManagementSidebar;
import com.databasepreservation.common.client.common.sidebar.DataTransformationSidebar;
import com.databasepreservation.common.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.common.client.common.sidebar.Sidebar;
import com.databasepreservation.common.client.common.sponsors.SponsorsPanel;
import com.databasepreservation.common.client.common.utils.ContentPanelLoader;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.utils.RightPanelLoader;
import com.databasepreservation.common.client.common.visualization.activity.log.ActivityLogDetailedPanel;
import com.databasepreservation.common.client.common.visualization.activity.log.ActivityLogPanel;
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
import com.databasepreservation.common.client.common.visualization.manager.databasePanel.user.UserDatabaseListPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanelLoad;
import com.databasepreservation.common.client.common.visualization.metadata.SIARDEditMetadataPage;
import com.databasepreservation.common.client.common.visualization.metadata.information.MetadataInformation;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.routines.MetadataRoutinePanel;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.tables.MetadataTablePanel;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.views.MetadataViewPanel;
import com.databasepreservation.common.client.common.visualization.metadata.users.MetadataUsersPanel;
import com.databasepreservation.common.client.common.visualization.preferences.PreferencesPanel;
import com.databasepreservation.common.client.common.visualization.validation.ValidatorPage;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.databasepreservation.server.client.browse.HomePanel;
import com.databasepreservation.server.client.browse.UploadPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class MainPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // databaseUUID, databaseName
  private static Map<String, String> databaseNames = new HashMap<>();

  interface MainPanelUiBinder extends UiBinder<Widget, MainPanel> {
  }

  private static MainPanelUiBinder binder = GWT.create(MainPanelUiBinder.class);

  @UiField
  SimplePanel contentPanel;

  @UiField
  AccessibleFocusPanel homeLinkArea;

  @UiField
  SimplePanel bannerLogo;

  MainPanel() {
    initWidget(binder.createAndBindUi(this));
    reSetHeader();
  }

  public void onHistoryChanged(String token) {
    List<String> currentHistoryPath = HistoryManager.getCurrentHistoryPath();
    // reSetHeader(currentHistoryPath);

    if (currentHistoryPath.isEmpty()) {
      // #
      HistoryManager.gotoHome();

    } else if (HistoryManager.ROUTE_UPLOADS.equals(currentHistoryPath.get(0))) {
      // #uploads
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User result) {
          if (result.isAdmin()) {
            setContent(new ContentPanelLoader() {
              @Override
              public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                return UploadPanel.getInstance();
              }
            });
          } else {
            HistoryManager.gotoHome();
          }
        }
      });
    } else if (HistoryManager.ROUTE_HOME.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User result) {
          if (result.isGuest()) {
            setContent(new ContentPanelLoader() {
              @Override
              public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                return HomePanel.getInstance();
              }
            });
          } else {
            if (result.isAdmin()) {
              setContent(new ContentPanelLoader() {
                @Override
                public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                  return DatabaseManage.getInstance();
                }
              });
            } else {
              setContent(new ContentPanelLoader() {
                @Override
                public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                  return UserDatabaseListPanel.getInstance();
                }
              });
            }
          }
        }
      });
    } else if (HistoryManager.ROUTE_ACTIVITY_LOG.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User result) {
          if (!result.isAdmin()) {
            HistoryManager.gotoHome();
          } else {
            if (currentHistoryPath.size() == 1) {
              // #activityLog
              setContent(new ContentPanelLoader() {
                @Override
                public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                  return ActivityLogPanel.getInstance();
                }
              });
            } else if (currentHistoryPath.size() == 2) {
              // #activityLog/<logUUID>
              String logUUID = currentHistoryPath.get(1);
              setContent(new ContentPanelLoader() {
                @Override
                public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                  return ActivityLogDetailedPanel.getInstance(logUUID);
                }
              });
            }
          }
        }
      });
    } else if (HistoryManager.ROUTE_SIARD_INFO.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (user.isAdmin()) {
            String databaseUUID = currentHistoryPath.get(1);
            setContent(databaseUUID, new ContentPanelLoader() {
              @Override
              public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                return SIARDManagerPage.getInstance(database);
              }
            });
          } else {
            HistoryManager.gotoHome();
          }
        }
      }, true);
    } else if (HistoryManager.ROUTE_UPLOAD_SIARD_DATA.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (!user.isGuest()) {
            String databaseUUID = currentHistoryPath.get(1);
            setContent(databaseUUID, new ContentPanelLoader() {
              @Override
              public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                return IngestPage.getInstance(database);
              }
            });
          } else {
            HistoryManager.gotoHome();
          }
        }
      }, true);
    } else if (HistoryManager.ROUTE_SIARD_VALIDATOR.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (user.isAdmin()) {
            final String databaseUUID = currentHistoryPath.get(1);
            setContent(databaseUUID, new ContentPanelLoader() {
              @Override
              public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                return ValidatorPage.getInstance(database);
              }
            });
          } else {
            HistoryManager.gotoHome();
          }
        }
      }, true);
    } else if (HistoryManager.ROUTE_ADVANCED_CONFIGURATION.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (user.isAdmin()) {
            final String databaseUUID = currentHistoryPath.get(1);
            setContent(databaseUUID, new ContentPanelLoader() {
              @Override
              public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                return AdvancedConfiguration.getInstance(database);
              }
            });
          } else {
            HistoryManager.gotoHome();
          }
        }
      }, true);
    } else if (HistoryManager.ROUTE_DATA_TRANSFORMATION.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (user.isAdmin()) {
            final String databaseUUID = currentHistoryPath.get(1);
            DataTransformationSidebar sidebar = DataTransformationSidebar.getInstance(databaseUUID);
            if (currentHistoryPath.size() == 2) {
              setContent(databaseUUID, HistoryManager.ROUTE_DATA_TRANSFORMATION, databaseUUID, sidebar,
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return DataTransformation.getInstance(status, database, sidebar);
                  }
                });
            } else if (currentHistoryPath.size() == 4) {
              final String tableId = currentHistoryPath.get(2) + "." + currentHistoryPath.get(3);
              setContent(databaseUUID, HistoryManager.ROUTE_DATA_TRANSFORMATION, tableId, sidebar,
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return DataTransformation.getInstance(status, database, tableId, sidebar);
                  }
                });
            }
          } else {
            HistoryManager.gotoHome();
          }
        }
      }, true);
    } else if (HistoryManager.ROUTE_TABLE_MANAGEMENT.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (user.isAdmin()) {
            final String databaseUUID = currentHistoryPath.get(1);
            setContent(databaseUUID, new ContentPanelLoader() {
              @Override
              public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
                return TableManagementPanel.getInstance(database, status);
              }
            });
          } else {
            HistoryManager.gotoHome();
          }
        }
      }, true);
    } else if (HistoryManager.ROUTE_COLUMNS_MANAGEMENT.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (user.isAdmin()) {
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
              setContent(databaseUUID, HistoryManager.ROUTE_COLUMNS_MANAGEMENT, tableId, sidebar,
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return ColumnsManagementPanel.getInstance(status, database, tableId, sidebar);
                  }
                });
            }
          } else {
            HistoryManager.gotoHome();
          }
        }
      }, true);
    } else if (HistoryManager.ROUTE_JOBS.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          setContent(new ContentPanelLoader() {
            @Override
            public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
              return JobManager.getInstance();
            }
          });
        }
      }, true);
    } else if (HistoryManager.ROUTE_PREFERENCES.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          setContent(new ContentPanelLoader() {
            @Override
            public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
              return PreferencesPanel.createInstance();
            }
          });
        }
      }, true);
    } else if (HistoryManager.ROUTE_DATABASE.equals(currentHistoryPath.get(0))) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          if (!user.isGuest()) {

            if (currentHistoryPath.size() == 2) {
              // #database/<database_uuid>
              String databaseUUID = currentHistoryPath.get(1);
              setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, currentHistoryPath.get(0),
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return DatabaseInformationPanel.getInstance(database, status);
                  }
                });

            } else if (currentHistoryPath.size() == 3
              && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_REPORT)) {
              // #database/<id>/report
              String databaseUUID = currentHistoryPath.get(1);
              setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, currentHistoryPath.get(2),
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return ReportPanel.getInstance(database);
                  }
                });

            } else if (currentHistoryPath.size() == 3
              && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_USERS)) {
              // #database/<id>/users
              String databaseUUID = currentHistoryPath.get(1);
              setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, currentHistoryPath.get(2),
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return UsersPanel.getInstance(database);
                  }
                });

            } else if (currentHistoryPath.size() == 3
              && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_SEARCH)) {
              // #database/<id>/search
              String databaseUUID = currentHistoryPath.get(1);
              setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, currentHistoryPath.get(2),
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return DatabaseSearchPanel.getInstance(database, status);
                  }
                });

            } else if (currentHistoryPath.size() == 3
              && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_SCHEMA_ROUTINES)) {
              // #database/<database_uuid>/routines
              String databaseUUID = currentHistoryPath.get(1);
              setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, currentHistoryPath.get(2),
                new RightPanelLoader() {
                  @Override
                  public RightPanel load(ViewerDatabase database, CollectionStatus status) {
                    return RoutinesPanel.getInstance(database);
                  }
                });
            } else {
              // #database/...
              handleErrorPath(currentHistoryPath);
            }
          } else {
            HistoryManager.gotoHome();
          }
        }
      });
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
        setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, tableId, new RightPanelLoader() {
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
          setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, tableId, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database, CollectionStatus status) {
              return TablePanelOptions.getInstance(status, database, tableId);
            }
          });

        } else {
          /// #table/<databaseUUID>/data/<schema>/<table>/<searchInfoJSON>
          setContent(databaseUUID, HistoryManager.ROUTE_DATABASE, tableId, new RightPanelLoader() {
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
        final String rowIndex = currentHistoryPath.get(5);
        setContent(databaseUUID, currentHistoryPath.get(0), tableId, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database, CollectionStatus status) {
            return RowPanel.createInstance(database, tableId, rowIndex, status);
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
              return ForeignKeyPanelOptions.getInstance(database, status, tableID,
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
        // #searches/<databaseUUID>/<searchUUID>
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
    } else if (HistoryManager.ROUTE_SIARD_EDIT_METADATA.equals(currentHistoryPath.get(0))) {
      String databaseUUID = currentHistoryPath.get(1);
      if (currentHistoryPath.size() == 2) {
        setContent(databaseUUID, databaseUUID, new MetadataPanelLoad() {
          @Override
          public MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
            return MetadataInformation.getInstance(database, SIARDbundle);
          }
        });
      } else if (currentHistoryPath.size() == 3) {
        final String user = currentHistoryPath.get(2);
        setContent(databaseUUID, user, new MetadataPanelLoad() {
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

        setContent(databaseUUID, tableUUID, new MetadataPanelLoad() {
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

        setContent(databaseUUID, viewUUID, new MetadataPanelLoad() {
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

        setContent(databaseUUID, routineUUID, new MetadataPanelLoad() {
          @Override
          public MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
            return MetadataRoutinePanel.getInstance(database, SIARDbundle, schemaUUID, routineUUID);
          }
        });
      }
    } else if (HistoryManager.ROUTE_SPONSORS.equals(currentHistoryPath.get(0))) {
      setContent(new ContentPanelLoader() {
        @Override
        public ContentPanel load(ViewerDatabase database, CollectionStatus status) {
          return new SponsorsPanel();
        }
      });
    } else {
      handleErrorPath(currentHistoryPath);
    }
  }

  private void handleErrorPath(List<String> currentHistoryPath) {
    if (currentHistoryPath.size() >= 2) {
      String databaseUUID = currentHistoryPath.get(1);
      reSetHeader(databaseUUID);
      HistoryManager.gotoDatabase(databaseUUID);
    } else {
      HistoryManager.gotoHome();
    }
  }

  /*
   * Header related methods
   * ____________________________________________________________________________________________________________________
   */
  private void reSetHeader() {
    HTMLPanel headerText = new HTMLPanel(
      SafeHtmlUtils.fromTrustedString(ClientConfigurationManager.getStringWithDefault(
        ViewerConstants.DEFAULT_PROPERTY_UI_HEADER_TITLE, ViewerConstants.PROPERTY_UI_HEADER_TITLE)));
    headerText.addStyleName("homeText");

    bannerLogo.setWidget(headerText);

    homeLinkArea.addClickHandler(event -> HistoryManager.gotoHome());

    homeLinkArea.setTitle(messages.goHome());
  }

  private void reSetHeader(final String databaseUUID, String databaseName) {
    HTMLPanel headerText = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(
      FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE) + " " + SafeHtmlUtils.htmlEscape(databaseName)));
    headerText.addStyleName("homeText");
    bannerLogo.setWidget(headerText);

    homeLinkArea.addClickHandler(event -> HistoryManager.gotoDatabase(databaseUUID));

    homeLinkArea.setTitle(messages.goToDatabaseInformation());
  }

  private void reSetHeader(final String databaseUUID) {
    if (databaseUUID == null) {
      reSetHeader();
    } else if (databaseNames.containsKey(databaseUUID)) {
      reSetHeader(databaseUUID, databaseNames.get(databaseUUID));
    } else {
      reSetHeader();

      DatabaseService.Util.call((ViewerDatabase result) -> {
        String databaseName = result.getMetadata().getName();

        databaseNames.put(databaseUUID, databaseName);
        reSetHeader(databaseUUID, databaseName);
      }).retrieve(databaseUUID);
    }
  }

  private void reSetHeader(List<String> currentHistoryPath) {
    if (currentHistoryPath.size() >= 2) {
      reSetHeader(currentHistoryPath.get(1));
    } else {
      reSetHeader((String) null);
    }
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

  private void checkAccessPermission() {
    UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
      @Override
      public void onSuccess(User result) {
        if (result.isGuest()) {
          HistoryManager.gotoHome();
        }
      }
    }, true);
  }
}
