package com.databasepreservation.dbviewer.shared.client.Tools;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.dbviewer.client.main.BreadcrumbItem;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BreadcrumbManager {
  private static String LOADING_DATABASE = "Database (loading)";
  private static String LOADING_SCHEMA = "Schema (loading)";
  private static String LOADING_TABLE = "Table (loading)";

  public static void updateBreadcrumb(BreadcrumbPanel breadcrumb, List<BreadcrumbItem> items) {
    breadcrumb.updatePath(items);
    breadcrumb.setVisible(true);
  }

  public static List<BreadcrumbItem> forDatabases() {
    List<BreadcrumbItem> items = new ArrayList<>();
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return "<i class=\"fa fa-server\"></i> Databases";
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
        return "<i class=\"fa fa-database\"></i> " + databaseName;
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
        return "<i class=\"fa fa-cube\"></i> " + schemaName;
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
        return "<i class=\"fa fa-table\"></i> " + tableName;
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
        return "<i class=\"fa fa-file-o\"></i> Record";
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoRecord(databaseUUID, tableUUID, recordUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingDatabase(final String databaseUUID) {
    List<BreadcrumbItem> items = forDatabases();
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return "<i class=\"fa fa-database\"></i> " + LOADING_DATABASE;
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
        return "<i class=\"fa fa-cube\"></i> " + LOADING_SCHEMA;
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
        return "<i class=\"fa fa-cube\"></i> " + LOADING_SCHEMA;
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
        return "<i class=\"fa fa-table\"></i> " + LOADING_TABLE;
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoTable(databaseUUID, tableUUID);
      }
    }));
    return items;
  }

  public static List<BreadcrumbItem> loadingRecord(final String databaseUUID, final String tableUUID,
    final String recordUUID) {
    List<BreadcrumbItem> items = loadingTable(databaseUUID, tableUUID);
    items.add(new BreadcrumbItem(new SafeHtml() {
      @Override
      public String asString() {
        return "<i class=\"fa fa-file-o\"></i> Record";
      }
    }, new Command() {
      @Override
      public void execute() {
        HistoryManager.gotoRecord(databaseUUID, tableUUID, recordUUID);
      }
    }));
    return items;
  }
}
