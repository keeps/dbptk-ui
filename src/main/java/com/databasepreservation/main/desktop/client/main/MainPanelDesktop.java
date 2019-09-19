package com.databasepreservation.main.desktop.client.main;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.common.utils.RightPanelLoader;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.DatabaseInformationPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.DatabasePanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.DatabaseReportPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.DatabaseSearchPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.DatabaseSearchesPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.DatabaseUsersPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.SchemaCheckConstraintsPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.SchemaDataPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.SchemaRoutinesPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.SchemaStructurePanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.SchemaTriggersPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.SchemaViewsPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.TablePanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.TableSavedSearchEditPanel;
import com.databasepreservation.main.common.shared.client.common.visualization.browse.TableSavedSearchPanel;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.dbptk.HomePage;
import com.databasepreservation.main.desktop.client.dbptk.Manage;
import com.databasepreservation.main.desktop.client.dbptk.SIARDMainPage;
import com.databasepreservation.main.desktop.client.dbptk.ingest.IngestPage;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanelLoad;
import com.databasepreservation.main.desktop.client.dbptk.metadata.SIARDEditMetadataPage;
import com.databasepreservation.main.desktop.client.dbptk.metadata.information.MetadataInformation;
import com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.routines.MetadataRoutinePanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables.MetadataTablePanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.views.MetadataViewPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.users.MetadataUsersPanel;
import com.databasepreservation.main.desktop.client.dbptk.validator.ValidatorPage;
import com.databasepreservation.main.desktop.client.dbptk.wizard.download.DBMSWizardManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.download.SIARDWizardManager;
import com.databasepreservation.main.desktop.client.dbptk.wizard.upload.CreateWizardManager;
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
      contentPanel.clear();
      contentPanel.add(HomePage.getInstance());

    } else if (HistoryManager.ROUTE_DESKTOP_UPLOAD_SIARD_DATA.equals(currentHistoryPath.get(0))) {
      IngestPage ingestPage = IngestPage.getInstance(currentHistoryPath.get(1));

      contentPanel.clear();
      contentPanel.add(ingestPage);
    } else if (HistoryManager.ROUTE_SIARD_INFO.equals(currentHistoryPath.get(0))) {
      SIARDMainPage instance = SIARDMainPage.getInstance(currentHistoryPath.get(1));

      contentPanel.clear();
      contentPanel.add(instance);

    } else if (HistoryManager.ROUTE_DESKTOP_DATABASE.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 1) {
        // #database
        Manage manage = Manage.getInstance();
        contentPanel.setWidget(manage);

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
        && currentHistoryPath.get(2).equals(HistoryManager.ROUTE_DATABASE_REPORT)) {
        // #database/<id>/report
        String databaseUUID = currentHistoryPath.get(1);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return DatabaseReportPanel.getInstance(database);
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

      }
    } else if (HistoryManager.ROUTE_DESKTOP_SAVED_SEARCHES.equals(currentHistoryPath.get(0))) {
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
        // #searches / < databaseUUID >/<searchUUID >
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
    } else if (HistoryManager.ROUTE_DESKTOP_SCHEMA.equals(currentHistoryPath.get(0))) {
      if (currentHistoryPath.size() == 3) {
        // #schema/<databaseUUID>/<schemaUUID>
        String databaseUUID = currentHistoryPath.get(1);
        final String schemaUUID = currentHistoryPath.get(2);
        setContent(databaseUUID, new RightPanelLoader() {
          @Override
          public RightPanel load(ViewerDatabase database) {
            return SchemaStructurePanel.getInstance(database, schemaUUID);
          }
        });

      } else if (currentHistoryPath.size() == 4) {
        // #schema/<databaseUUID>/<schemaUUID>/structure
        // #schema/<databaseUUID>/<schemaUUID>/routines
        // #schema/<databaseUUID>/<schemaUUID>/triggers
        // #schema/<databaseUUID>/<schemaUUID>/views
        String databaseUUID = currentHistoryPath.get(1);
        final String schemaUUID = currentHistoryPath.get(2);
        String pageSpec = currentHistoryPath.get(3);

        switch (pageSpec) {
          case HistoryManager.ROUTE_SCHEMA_STRUCTURE:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaStructurePanel.getInstance(database, schemaUUID);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_ROUTINES:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaRoutinesPanel.getInstance(database, schemaUUID);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_TRIGGERS:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaTriggersPanel.getInstance(database, schemaUUID);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_VIEWS:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaViewsPanel.getInstance(database, schemaUUID);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_CHECK_CONSTRAINTS:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaCheckConstraintsPanel.getInstance(database, schemaUUID);
              }
            });
            break;
          case HistoryManager.ROUTE_SCHEMA_DATA:
            setContent(databaseUUID, new RightPanelLoader() {
              @Override
              public RightPanel load(ViewerDatabase database) {
                return SchemaDataPanel.getInstance(database, schemaUUID);
              }
            });
            break;
          default:
            // #schema/<databaseUUID>/<schemaUUID>/*invalid-page*
            handleErrorPath(currentHistoryPath);
        }

      } else {
        // #schema/...
        handleErrorPath(currentHistoryPath);

      }
    } else if (HistoryManager.ROUTE_DESKTOP_TABLE.equals(currentHistoryPath.get(0))) {
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
    } else if (HistoryManager.ROUTE_SEND_TO_LIVE_DBMS.equals(currentHistoryPath.get(0))) {
      DBMSWizardManager instance = DBMSWizardManager.getInstance(currentHistoryPath.get(1));
      contentPanel.clear();
      if (currentHistoryPath.size() == 3) {
        final String databaseName = currentHistoryPath.get(2);
        instance.setBreadcrumbDatabaseName(databaseName);
      }
      if (currentHistoryPath.size() == 4) {
        final String wizardPage = currentHistoryPath.get(2);
        final String toSelect = currentHistoryPath.get(3);
        instance.change(wizardPage, toSelect);
      }
      contentPanel.add(instance);
    } else if (HistoryManager.ROUTE_MIGRATE_TO_SIARD.equals(currentHistoryPath.get(0))) {
      SIARDWizardManager instance = SIARDWizardManager.getInstance(currentHistoryPath.get(1), currentHistoryPath.get(2));
      contentPanel.clear();
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
      contentPanel.add(instance);
    } else if (HistoryManager.ROUTE_CREATE_SIARD.equals(currentHistoryPath.get(0))) {
      CreateWizardManager instance = CreateWizardManager.getInstance();
      contentPanel.clear();
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
      contentPanel.add(instance);
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
      if (currentHistoryPath.size() == 3) {
        final String databaseUUID = currentHistoryPath.get(1);
        final String reporterPath = currentHistoryPath.get(2);
        ValidatorPage instance = ValidatorPage.getInstance(databaseUUID, reporterPath);
        contentPanel.clear();
        contentPanel.add(instance);

      } else if (currentHistoryPath.size() == 4) {
        final String databaseUUID = currentHistoryPath.get(1);
        final String reporterPath = currentHistoryPath.get(2);
        final String udtPath = currentHistoryPath.get(3);

        ValidatorPage instance = ValidatorPage.getInstance(databaseUUID, reporterPath, udtPath);
        contentPanel.clear();
        contentPanel.add(instance);
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
    setContent(null, rightPanelLoader);
  }

  private void setContent(String databaseUUID, RightPanelLoader rightPanelLoader) {
    GWT.log("setContent, dbuid " + databaseUUID);
    DatabasePanel databasePanel = DatabasePanel.getInstance(databaseUUID);
    contentPanel.setWidget(databasePanel);
    databasePanel.load(rightPanelLoader);
    JavascriptUtils.scrollToElement(contentPanel.getElement());
  }
}