package com.databasepreservation.visualization.shared.client.Tools;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.visualization.client.main.BreadcrumbItem;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.HOME) + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_home())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoHome();
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forLogin() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.LOGIN) + SafeHtmlUtils.htmlEscape(" " + messages.loginLogin())), new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoLogin();
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forUploads() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.UPLOADS) + SafeHtmlUtils.htmlEscape(" " + messages.uploads())), new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoDatabaseList();
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forNewUpload() {
    List<BreadcrumbItem> items = forUploads();
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.NEW_UPLOAD) + SafeHtmlUtils.htmlEscape(" " + messages.newUpload())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoNewUpload();
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forUpload(final String databaseUUID) {
    List<BreadcrumbItem> items = forUploads();
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.NEW_UPLOAD) + SafeHtmlUtils.htmlEscape(" " + messages.uploadedSIARD())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoUpload(databaseUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabases() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.DATABASES) + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_databases())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoDatabaseList();
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabase(final String databaseName, final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabases();
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.DATABASE) + SafeHtmlUtils.htmlEscape(" " + databaseName)), new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoDatabase(databaseUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseUsers(final String databaseName, final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabase(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.DATABASE_USERS) + SafeHtmlUtils.htmlEscape(" " + messages.titleUsers())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoDatabaseUsers(databaseUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabaseSavedSearches(final String databaseName, final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabase(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SAVED_SEARCH)
      + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_savedSearches())), new Command() {
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
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_savedSearch())), new Command() {
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SAVED_SEARCH) + SafeHtmlUtils.htmlEscape(" " + messages.editingSavedSearch())),
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA) + SafeHtmlUtils.htmlEscape(" " + schemaName)), new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoSchema(databaseUUID, schemaUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaStructure(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA_STRUCTURE)
      + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_structure())), new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoSchemaStructure(databaseUUID, schemaUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaRoutines(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_ROUTINES)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_routines())), new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchemaRoutines(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forSchemaTriggers(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_TRIGGERS)
        + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_triggers())), new Command() {
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA_CHECK_CONSTRAINTS)
      + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_checkConstraints())), new Command() {
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA_VIEWS) + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_views())),
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA_DATA) + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_data())),
      new Command() {
        @Override
        public void execute() {
          HistoryManager.gotoSchemaData(databaseUUID, schemaUUID);
        }
      }));
    return items;
  }

  public static List<BreadcrumbItem> forTable(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID, final String tableName, final String tableUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.TABLE) + SafeHtmlUtils.htmlEscape(" " + tableName)), new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoTable(databaseUUID, tableUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forRecord(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID, final String tableName, final String tableUUID,
    final String recordUUID) {
    List<BreadcrumbItem> items = forTable(databaseName, databaseUUID, schemaName, schemaUUID, tableName, tableUUID);
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.RECORD) + SafeHtmlUtils.htmlEscape(" " + messages.menusidebar_record())),
      new Command() {
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
    List<BreadcrumbItem> items = forRecord(databaseName, databaseUUID, schemaName, schemaUUID, tableName, tableUUID,
      recordUUID);
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.REFERENCE)
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.DATABASE) + SafeHtmlUtils.htmlEscape(" " + LOADING_DATABASE)), new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoDatabase(databaseUUID);
      }
    }));
    return items;
  }
}
