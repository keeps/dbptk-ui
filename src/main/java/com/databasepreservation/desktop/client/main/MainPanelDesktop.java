package com.databasepreservation.desktop.client.main;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.utils.ContentPanelLoader;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.utils.RightPanelLoader;
import com.databasepreservation.common.client.common.visualization.browse.ContainerPanel;
import com.databasepreservation.common.client.common.visualization.browse.DatabasePanel;
import com.databasepreservation.common.client.common.visualization.browse.DatabaseSearchPanel;
import com.databasepreservation.common.client.common.visualization.browse.DatabaseSearchesPanel;
import com.databasepreservation.common.client.common.visualization.browse.ReferencesPanel;
import com.databasepreservation.common.client.common.visualization.browse.RowPanel;
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
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.common.visualization.manager.databasePanel.DatabaseManage;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanelLoad;
import com.databasepreservation.common.client.common.visualization.metadata.SIARDEditMetadataPage;
import com.databasepreservation.common.client.common.visualization.metadata.information.MetadataInformation;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.routines.MetadataRoutinePanel;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.tables.MetadataTablePanel;
import com.databasepreservation.common.client.common.visualization.metadata.schemas.views.MetadataViewPanel;
import com.databasepreservation.common.client.common.visualization.metadata.users.MetadataUsersPanel;
import com.databasepreservation.common.client.common.visualization.validation.ValidatorPage;
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

import java.util.ArrayList;
import java.util.List;


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
        public ContentPanel load(ViewerDatabase database) {
          return IngestPage.getInstance(database);
        }
      });

    } else if (HistoryManager.ROUTE_SIARD_INFO.equals(currentHistoryPath.get(0))) {
      String databaseUUID = currentHistoryPath.get(1);
      setContent(databaseUUID, new ContentPanelLoader() {
        @Override
        public ContentPanel load(ViewerDatabase database) {
          return SIARDManagerPage.getInstance(database);
        }
      });

    } else if (HistoryManager.ROUTE_DATABASE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 1) {
        // #database
        setContent(new ContentPanelLoader() {
          @Override
          public ContentPanel load(ViewerDatabase database) {
            return DatabaseManage.getInstance();
          }
        });
      } else if (currentHistoryPath.size() == 2) {
        // #database/<database_uuid>
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseInformationPanel.getInstance(database);
          }
        });
      } else if (currentHistoryPath.size() == 3
          && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_REPORT)) {
        // #database/<database_uuid>/report
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return ReportPanel.getInstance(database);
          }
        });
      } else if (currentHistoryPath.size() == 3
          && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_USERS)) {
        // #database/<database_uuid>/users
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return UsersPanel.getInstance(database);
          }
        });
      } else if (currentHistoryPath.size() == 3
          && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_SEARCH)) {
        // #database/<database_uuid>/search
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseSearchPanel.getInstance(database);
          }
        });
      } else if (currentHistoryPath.size() == 3
          && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_SCHEMA_ROUTINES)) {
        // #database/<database_uuid>/routines
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(2), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return RoutinesPanel.getInstance(database);
          }
        });
      }
    } else if (HistoryManager.ROUTE_SAVED_SEARCHES.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 2) {
        // #searches/<databaseUUID>
        final String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, currentHistoryPath.get(0), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseSearchesPanel.createInstance(database);
          }
        });

      } else if (currentHistoryPath.size() == 3) {
        // #searches / < databaseUUID >/<searchUUID >
        final String databaseUUID = currentHistoryPath.get(1);
        final String searchUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, currentHistoryPath.get(0), new RightPanelLoader() {
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
        setContent(databaseUUID, currentHistoryPath.get(0), new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return TableSavedSearchEditPanel.createInstance(database, searchUUID);
          }
        });
      } else {
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_VIEW.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        //#view/<databaseUUID>/<viewUUID>
        String databaseUUID = currentHistoryPath.get(1);
        String viewUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, viewUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return ViewPanel.getInstance(database, viewUUID);
          }
        });
      } else if (currentHistoryPath.size() == 4) {
        //#view/<databaseUUID>/<tableUUID>/options
        String databaseUUID = currentHistoryPath.get(1);
        String viewUUID = currentHistoryPath.get(2);
        final String page = currentHistoryPath.get(3);
        if (page.equals(HistoryManager.ROUTE_TABLE_OPTIONS)) {
          setContent(databaseUUID, viewUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database) {
              return ViewPanelStructure.getInstance(database, viewUUID);
            }
          });
        } else {
          // #table/...
          handleErrorPath(currentHistoryPath);
        }
      }
      else {
        // #table/...
        handleErrorPath(currentHistoryPath);
      }
    } else if (HistoryManager.ROUTE_TABLE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        // #table/<databaseUUID>/<tableUUID>
        String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, tableUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return TablePanel.getInstance(database, tableUUID, currentHistoryPath.get(0));
          }
        });

      } else if (currentHistoryPath.size() == 4) {

        String databaseUUID = currentHistoryPath.get(1);
        final String tableUUID = currentHistoryPath.get(2);
        final String page = currentHistoryPath.get(3);
        if (page.equals(HistoryManager.ROUTE_TABLE_OPTIONS)) {
          // #table/<databaseUUID>/<tableUUID>/options
          setContent(databaseUUID, tableUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database) {
              return TablePanelOptions.getInstance(database, tableUUID);
            }
          });

        } else if (page.equals(HistoryManager.ROUTE_TABLE_UPDATE)) {
          // #table/<databaseUUID>/<tableUUID>/update
          setContent(databaseUUID, tableUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database) {
              final TablePanel instance = TablePanel.getInstance(database, tableUUID, currentHistoryPath.get(0));
              instance.update();
              return instance;
            }
          });
        } else {
          // #table/<databaseUUID>/<tableUUID>/<searchInfoJSON>
          setContent(databaseUUID, tableUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database) {
              return TablePanel.getInstance(database, tableUUID, page);
            }
          });
        }
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
        setContent(databaseUUID, tableUUID, new RightPanelLoader() {
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
        setContent(databaseUUID, tableUUID, new RightPanelLoader() {
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
        String page = columnsAndValues.get(columnsAndValues.size() - 1);
        if (page.equals(HistoryManager.ROUTE_TABLE_OPTIONS)) {
          setContent(databaseUUID, tableUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database) {
              return ForeignKeyPanelOptions.getInstance(database, tableUUID, columnsAndValues.subList(0, columnsAndValues.size() - 1));
            }
          });
        } else if (page.equals(HistoryManager.ROUTE_TABLE_UPDATE)) {
          setContent(databaseUUID, tableUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database) {
              return ForeignKeyPanel.createInstance(database, tableUUID, columnsAndValues.subList(0, columnsAndValues.size() - 1), true);
            }
          });
        } else if (columnsAndValues.size() % 2 == 0) {
          setContent(databaseUUID, tableUUID, new RightPanelLoader() {
            @Override
            public RightPanel load(ViewerDatabase database) {
              return ForeignKeyPanel.createInstance(database, tableUUID, columnsAndValues);
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
      SIARDWizardManager instance = SIARDWizardManager.getInstance(currentHistoryPath.get(1), currentHistoryPath.get(2));
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
    }  else if (HistoryManager.ROUTE_SIARD_EDIT_METADATA.equals(currentHistoryPath.get(0))) {
      String databaseUUID =  currentHistoryPath.get(1);
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
    } else if(HistoryManager.ROUTE_SIARD_VALIDATOR.equals(currentHistoryPath.get(0))){
      if (currentHistoryPath.size() == 4) {
        final String databaseUUID = currentHistoryPath.get(1);
        final String reporterPath = currentHistoryPath.get(2);
        final String skipAdditionalChecks = currentHistoryPath.get(3);

        setContent(databaseUUID, new ContentPanelLoader() {
          @Override
          public ContentPanel load(ViewerDatabase database) {
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
          public ContentPanel load(ViewerDatabase database) {
            return ValidatorPage.getInstance(database, reporterPath, udtPath, skipAdditionalChecks);
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
  public void setContent(RightPanelLoader rightPanelLoader) {
    setContent(null, null, rightPanelLoader);
  }

  public void setContent(ContentPanelLoader panel) {
    setContent(null, panel);
  }

  private void setContent(String databaseUUID, String toSelect, RightPanelLoader rightPanelLoader) {
    GWT.log("setContent, dbuid " + databaseUUID);
    DatabasePanel databasePanel = DatabasePanel.getInstance(databaseUUID, false);
    databasePanel.setTopLevelPanelCSS("browseContent wrapper skip_padding");
    contentPanel.setWidget(databasePanel);
    databasePanel.load(rightPanelLoader, toSelect);
    JavascriptUtils.scrollToElement(contentPanel.getElement());
  }

  private void setContent(String databaseUUID, ContentPanelLoader panel) {
    GWT.log("LOADER ::: setContent, dbuid " + databaseUUID);
    ContainerPanel containerPanel = ContainerPanel.getInstance(databaseUUID, false);
    containerPanel.setTopLevelPanelCSS("browseContent wrapper skip_padding");
    contentPanel.setWidget(containerPanel);
    containerPanel.load(panel);
    JavascriptUtils.scrollToElement(contentPanel.getElement());
  }
}