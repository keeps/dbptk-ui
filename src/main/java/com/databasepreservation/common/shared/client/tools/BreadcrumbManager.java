package com.databasepreservation.common.shared.client.tools;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BreadcrumbManager {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static String LOADING_DATABASE = "Database (loading)";

  public static void updateBreadcrumb(BreadcrumbPanel breadcrumb, List<BreadcrumbItem> items) {
    breadcrumb.updatePath(items);
    breadcrumb.setVisible(true);
  }

  public static List<BreadcrumbItem> empty() {
    return new ArrayList<>();
  }

  public static List<BreadcrumbItem> forHome() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.HOME)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_home())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoHome();
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forLogin() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.LOGIN)
        + SafeHtmlUtils.htmlEscape(" " + messages.loginLogin())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoLogin();
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forUploads() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.UPLOADS)
        + SafeHtmlUtils.htmlEscape(" " + messages.uploads())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoDatabaseList();
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forNewUpload() {
    List<BreadcrumbItem> items = forUploads();
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.NEW_UPLOAD)
        + SafeHtmlUtils.htmlEscape(" " + messages.newUpload())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoNewUpload();
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forUpload(final String databaseUUID) {
    List<BreadcrumbItem> items = forUploads();
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.NEW_UPLOAD)
        + SafeHtmlUtils.htmlEscape(" " + messages.uploadedSIARD())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoUpload(databaseUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabases() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASES)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_databases())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoDatabaseList();
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabase(final String databaseName, final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabases();
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(
        FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE) + SafeHtmlUtils.htmlEscape(" " + databaseName)),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoDatabase(databaseUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseInformation(final String databaseUUID, final String databaseName) {
      List<BreadcrumbItem> items;
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
          items = forSIARDMainPage(databaseUUID, databaseName);
      } else {
          items = forDatabase(databaseName, databaseUUID);
      }
      items.add(new BreadcrumbItem(
              SafeHtmlUtils.fromSafeConstant(
                      FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_INFORMATION) + SafeHtmlUtils.htmlEscape(" " + messages.databaseInformationTextForTitle())),
              new Command() {
                  @Override
                  public void execute() {
                      HistoryManager.gotoDatabase(databaseUUID);
                  }
              }));

      return items;
  }

  public static List<BreadcrumbItem> forDesktopDatabaseSavedSearches(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_SEARCH)
            + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForDesktopDatabaseSavedSearches())), new Command(){
          @Override
          public void execute() {
            HistoryManager.gotoSavedSearches(databaseUUID);
          }
        })
    );
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseSearchPanel(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items;
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        items = forSIARDMainPage(databaseUUID, databaseName);
    } else {
        items = forDatabase(databaseName, databaseUUID);
    }

    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_SEARCH) + SafeHtmlUtils.htmlEscape(" " + messages.search())),
              new Command() {
                  @Override
                  public void execute() {
                      HistoryManager.gotoDatabaseSearch(databaseUUID);
                  }
              }));

    return items;
  }

  public static List<BreadcrumbItem> forDatabaseReport(final String databaseUUID, final String databaseName) {
     List<BreadcrumbItem> items;
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
          items = forSIARDMainPage(databaseUUID, databaseName);
     } else {
        items = forDatabase(databaseName, databaseUUID);
     }

    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_REPORT)
        + SafeHtmlUtils.htmlEscape(" " + messages.titleReport())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoDatabaseReport(databaseUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopDatabaseUsers(final String databaseName, final String databaseUUID) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_USERS)
            + SafeHtmlUtils.htmlEscape(" " + messages.titleUsers())),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoDesktopDatabaseUsers(databaseUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseUsers(final String databaseName, final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabase(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_USERS)
        + SafeHtmlUtils.htmlEscape(" " + messages.titleUsers())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoDatabaseUsers(databaseUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseSavedSearches(final String databaseName, final String databaseUUID) {
      List<BreadcrumbItem> items;
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        items = forSIARDMainPage(databaseUUID, databaseName);
    } else {
          items = forDatabase(databaseName, databaseUUID);
      }
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_savedSearches())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSavedSearches(databaseUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseSavedSearch(final String databaseName, final String databaseUUID,
    final String savedSearchUUID) {
    List<BreadcrumbItem> items = forDatabaseSavedSearches(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_savedSearch())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSavedSearch(databaseUUID, savedSearchUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopDatabaseSavedSearch(final String databaseName, final String databaseUUID,
                                                            final String savedSearchUUID) {
    List<BreadcrumbItem> items = forDesktopDatabaseSavedSearches(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH)
            + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_savedSearch())),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoSavedSearch(databaseUUID, savedSearchUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseSavedSearchEdit(final String databaseName, final String databaseUUID,
    final String savedSearchUUID) {
    List<BreadcrumbItem> items = forDatabaseSavedSearches(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH)
        + SafeHtmlUtils.htmlEscape(" " + messages.editingSavedSearch())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoEditSavedSearch(databaseUUID, savedSearchUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopDatabaseSavedSearchEdit(final String databaseName, final String databaseUUID,
                                                                final String savedSearchUUID) {
    List<BreadcrumbItem> items = forDesktopDatabaseSavedSearches(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH)
            + SafeHtmlUtils.htmlEscape(" " + messages.editingSavedSearch())),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoEditSavedSearch(databaseUUID, savedSearchUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forSchema(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forDatabase(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(
        FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA) + SafeHtmlUtils.htmlEscape(" " + schemaName)),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchema(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopSchema(final String databaseName, final String databaseUUID,
                                               final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(
            FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA) + SafeHtmlUtils.htmlEscape(" " + schemaName)),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoDesktopSchema(databaseUUID, schemaUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopSchemaStructure(final String databaseName, final String databaseUUID,
                                                        final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_STRUCTURE)
            + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_structure())),
        new Command() {
          @Override
          public void execute() {
            //HistoryManager.gotoSchemaStructure(databaseUUID, schemaUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaStructure(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_STRUCTURE)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_structure())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchemaStructure(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaRoutines(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items;
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        items = forDatabase(databaseName, databaseUUID);
    } else {
        items = forSIARDMainPage(databaseUUID, databaseName);
    }
    items.add(new BreadcrumbItem(
              SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_ROUTINES)
                      + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_routines())),
              new Command() {
                  @Override
                  public void execute() {
                      HistoryManager.gotoSchemaRoutines(databaseUUID);
                  }
              }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaTriggers(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_TRIGGERS)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_triggers())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchemaTriggers(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopSchemaTriggers(final String databaseName, final String databaseUUID,
                                                       final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_TRIGGERS)
            + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_triggers())),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoSchemaTriggers(databaseUUID, schemaUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaCheckConstraints(final String databaseName, final String databaseUUID,
                                                               final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_CHECK_CONSTRAINTS)
            + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_checkConstraints())),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoSchemaCheckConstraints(databaseUUID, schemaUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopSchemaCheckConstraints(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_CHECK_CONSTRAINTS)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_checkConstraints())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchemaCheckConstraints(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaViews(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_VIEWS)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_views())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchemaViews(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopSchemaViews(final String databaseName, final String databaseUUID,
                                                    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_VIEWS)
            + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_views())),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoSchemaViews(databaseUUID, schemaUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaData(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forDatabase(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_DATA)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_data())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchemaData(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopTable(final String databaseName, final String databaseUUID,
                                              final String schemaName, final String schemaUUID, final String tableName, final String tableUUID) {
    List<BreadcrumbItem> items = forDesktopSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(
            FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE) + SafeHtmlUtils.htmlEscape(" " + tableName)),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoDesktopTable(databaseUUID, tableUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forTable(final String databaseName, final String databaseUUID,
     final String tableName, final String tableUUID) {
      List<BreadcrumbItem> items;
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
          items = forSIARDMainPage(databaseUUID, databaseName);
      } else {
          items = forDatabase(databaseName, databaseUUID);
      }
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(
        FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE) + SafeHtmlUtils.htmlEscape(" " + tableName)),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoTable(databaseUUID, tableUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forRecord(final String databaseName, final String databaseUUID, final String tableName, final String tableUUID,
    final String recordUUID) {
    List<BreadcrumbItem> items = forTable(databaseName, databaseUUID, tableName, tableUUID);
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_record())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoRecord(databaseUUID, tableUUID, recordUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopRecord(final String databaseName, final String databaseUUID,
                                               final String schemaName, final String schemaUUID, final String tableName, final String tableUUID,
                                               final String recordUUID) {
    List<BreadcrumbItem> items = forDesktopTable(databaseName, databaseUUID, schemaName, schemaUUID, tableName, tableUUID);
    items.add(
        new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD)
            + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_record())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoRecord(databaseUUID, tableUUID, recordUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forReferences(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID, final String tableName, final String tableUUID,
    final String recordUUID, final String columnNameInTable, final String columnIndexInTable) {
    List<BreadcrumbItem> items = forRecord(databaseName, databaseUUID, tableName, tableUUID,
      recordUUID);
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE)
        + SafeHtmlUtils.htmlEscape(messages.menusidebar_referencesForColumn(columnNameInTable))), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoReferences(databaseUUID, tableUUID, recordUUID, columnIndexInTable);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forDesktopReferences(final String databaseName, final String databaseUUID,
                                                   final String schemaName, final String schemaUUID, final String tableName, final String tableUUID,
                                                   final String recordUUID, final String columnNameInTable, final String columnIndexInTable) {
    List<BreadcrumbItem> items = forDesktopRecord(databaseName, databaseUUID, schemaName, schemaUUID, tableName, tableUUID,
        recordUUID);
    items.add(
        new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE)
            + SafeHtmlUtils.htmlEscape(messages.menusidebar_referencesForColumn(columnNameInTable))), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoReferences(databaseUUID, tableUUID, recordUUID, columnIndexInTable);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> loadingDatabase(final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabases();
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE)
        + SafeHtmlUtils.htmlEscape(" " + LOADING_DATABASE)), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoDatabase(databaseUUID);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forSIARDMainPage(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forManageSIARD();
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(
        FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE) + SafeHtmlUtils.htmlEscape(" " + databaseName)),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSIARDInfo(databaseUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forSIARDIngesting(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(new BreadcrumbItem(
        SafeHtmlUtils.fromSafeConstant(
            FontAwesomeIconManager.getTag(FontAwesomeIconManager.WRITE) + SafeHtmlUtils.htmlEscape(messages.breadcrumbTextForSIARDIngesting())),
        new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoIngestSIARDData(databaseUUID, databaseName);
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forSIARDValidatorPage(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SIARD_VALIDATIONS)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForSIARDValidator()))));
    return items;
  }

  public static List<BreadcrumbItem> forManageSIARD() {
    List<BreadcrumbItem> items = forHome();
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SERVER)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForManageSIARD())), new Command() {
          @Override
          public void execute() {
            HistoryManager.gotoDatabase();
          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forCreateConnection() {
    List<BreadcrumbItem> items = forHome();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardCreateSIARDConnection()))));
    return items;
  }

  public static List<BreadcrumbItem> forTableAndColumns() {
    List<BreadcrumbItem> items = forHome();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardCreateSIARDTableAndColumns()))));
    return items;
  }

  public static List<BreadcrumbItem> forSIARDExportOptions() {
    List<BreadcrumbItem> items = forHome();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardCreateSIARDExportOptions()))));
    return items;
  }

  public static List<BreadcrumbItem> forMetadataExportOptions() {
    List<BreadcrumbItem> items = forHome();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardCreateSIARDMetadataOptions()))));
    return items;
  }

  public static List<BreadcrumbItem> forCustomViews() {
    List<BreadcrumbItem> items = forHome();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardCreateSIARDCustomViews()))));
    return items;
  }

  public static List<BreadcrumbItem> forSIARDEditMetadataPage(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForSIARDEditMetadata())), new Command() {
          @Override
          public void execute() {

          }
        }));
    return items;
  }

  public static List<BreadcrumbItem> forCreateSIARD() {
    List<BreadcrumbItem> items = forHome();
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForCreateSIARD()))));
    return items;
  }

  public static List<BreadcrumbItem> forTableAndColumnsSendToWM(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardSendToTableAndColumns()))));
    return items;
  }

  public static List<BreadcrumbItem> forDBMSConnectionSendToWM(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardSendToDBMSConnection()))));
    return items;
  }

  public static List<BreadcrumbItem> forSIARDExportOptionsSenToWM(final String databaseUUID,
    final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardSendToSIARDExportOptions()))));
    return items;
  }

  public static List<BreadcrumbItem> forMetadataExportOptionsSendToWM(final String databaseUUID,
    final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardSendToMetadataExportOptions()))));
    return items;
  }

  public static List<BreadcrumbItem> forProgressBarPanelSendToWM(final String databaseUUID, final String databaseName) {
    List<BreadcrumbItem> items = forSIARDMainPage(databaseUUID, databaseName);
    items
      .add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.WIZARD)
        + SafeHtmlUtils.htmlEscape(" " + messages.breadcrumbTextForWizardSendToProgressPanel()))));
    return items;
  }
}
