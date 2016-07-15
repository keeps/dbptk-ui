package com.databasepreservation.visualization.shared.client.Tools;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.visualization.client.main.BreadcrumbItem;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BreadcrumbManager {
  private static String LOADING_DATABASE = "Database (loading)";
  private static String LOADING_SCHEMA = "Schema (loading)";
  private static String LOADING_TABLE = "Table (loading)";
  private static String LOADING_RECORDS = "Records (loading)";

  public static void updateBreadcrumb(BreadcrumbPanel breadcrumb, List<BreadcrumbItem> items) {
    breadcrumb.updatePath(items);
    breadcrumb.setVisible(true);
  }

  public static List<BreadcrumbItem> forDatabases() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASES) + " Databases";
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoDatabaseList();
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forDatabase(final String databaseName, final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabases();
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE) + " " + databaseName;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoDatabase(databaseUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forSchema(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID) {
    List<BreadcrumbItem> items = forDatabase(databaseName, databaseUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA) + " " + schemaName;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoSchema(databaseUUID, schemaUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> forTable(final String databaseName, final String databaseUUID,
    final String schemaName, final String schemaUUID, final String tableName, final String tableUUID) {
    List<BreadcrumbItem> items = forSchema(databaseName, databaseUUID, schemaName, schemaUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE) + " " + tableName;
      }
    }, new Command() {
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
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD) + " Record";
      }
    }, new Command() {
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
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE) + " References for column "
          + columnNameInTable;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoReferences(databaseUUID, tableUUID, recordUUID, columnIndexInTable);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingDatabase(final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabases();
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE) + " " + LOADING_DATABASE;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoDatabase(databaseUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingSchema(final String databaseUUID) {
    List<BreadcrumbItem> items = loadingDatabase(databaseUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA) + " " + LOADING_SCHEMA;
      }
    }, new Command() {
      @Override
      public void execute() {
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingSchema(final String databaseUUID, final String schemaUUID) {
    List<BreadcrumbItem> items = loadingDatabase(databaseUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA) + " " + LOADING_SCHEMA;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoSchema(databaseUUID, schemaUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingTable(final String databaseUUID, final String tableUUID) {
    List<BreadcrumbItem> items = loadingSchema(databaseUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE) + " " + LOADING_TABLE;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoTable(databaseUUID, tableUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingRecord(final String databaseUUID, final String tableUUID, final String recordUUID) {
    List<BreadcrumbItem> items = loadingTable(databaseUUID, tableUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.RECORD) + " Record";
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoRecord(databaseUUID, tableUUID, recordUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingReferences(final String databaseUUID, final String tableUUID,
    final String recordUUID, final String columnIndexInTable) {
    List<BreadcrumbItem> items = loadingRecord(databaseUUID, tableUUID, recordUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE) + " " + LOADING_RECORDS;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoReferences(databaseUUID, tableUUID, recordUUID, columnIndexInTable);
      }
    }));
    return items;
  }
}
