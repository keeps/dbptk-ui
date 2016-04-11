package com.databasepreservation.dbviewer.client.main;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BreadcrumbManager {
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

  public static List<BreadcrumbItem> forTable(final String databaseName, final String databaseUUID,
    final String tableName, final String tableUUID) {
    List<BreadcrumbItem> items = forDatabase(databaseName, databaseUUID);
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
}
