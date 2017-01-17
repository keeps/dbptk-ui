package com.databasepreservation.visualization.shared.client.Tools;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.visualization.client.main.BreadcrumbItem;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BreadcrumbManager {
  private static String LOADING_DATABASE = "Database (loading)";

  public static void updateBreadcrumb(BreadcrumbPanel breadcrumb, List<BreadcrumbItem> items) {
    breadcrumb.updatePath(items);
    breadcrumb.setVisible(true);
  }

  public static List<BreadcrumbItem> empty() {
    return new ArrayList<>();
  }

  public static List<BreadcrumbItem> forDatabases() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.DATABASES) + SafeHtmlUtils.htmlEscape(" Databases")), new Command() {
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
      .getTag(FontAwesomeIconManager.DATABASE_USERS) + SafeHtmlUtils.htmlEscape(" Users")), new Command() {
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
      .getTag(FontAwesomeIconManager.SAVED_SEARCH) + SafeHtmlUtils.htmlEscape(" Saved searches")), new Command() {
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SAVED_SEARCH) + SafeHtmlUtils.htmlEscape(" Saved search")), new Command() {
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
      .getTag(FontAwesomeIconManager.SAVED_SEARCH) + SafeHtmlUtils.htmlEscape(" Editing saved search")), new Command() {
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
      .getTag(FontAwesomeIconManager.SCHEMA_STRUCTURE) + SafeHtmlUtils.htmlEscape(" Structure")), new Command() {
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA_ROUTINES) + SafeHtmlUtils.htmlEscape(" Routines")), new Command() {
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA_TRIGGERS) + SafeHtmlUtils.htmlEscape(" Triggers")), new Command() {
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
      .getTag(FontAwesomeIconManager.SCHEMA_CHECK_CONSTRAINTS) + SafeHtmlUtils.htmlEscape(" Check constraints")),
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
    items.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.SCHEMA_VIEWS) + SafeHtmlUtils.htmlEscape(" Views")), new Command() {
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
      .getTag(FontAwesomeIconManager.SCHEMA_DATA) + SafeHtmlUtils.htmlEscape(" Data")), new Command() {
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
      .getTag(FontAwesomeIconManager.RECORD) + SafeHtmlUtils.htmlEscape(" Record")), new Command() {
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
      + SafeHtmlUtils.htmlEscape(" References for column " + columnNameInTable)), new Command() {
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
