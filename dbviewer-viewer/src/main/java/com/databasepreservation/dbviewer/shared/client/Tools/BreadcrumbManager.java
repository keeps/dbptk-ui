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
  public static void updateBreadcrumb(BreadcrumbPanel breadcrumb, List<BreadcrumbItem> items){
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

  public static List<BreadcrumbItem> forSchema(final String databaseName, final String databaseUUID, final String schemaName, final String schemaUUID) {
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

  public static List<BreadcrumbItem> forTable(final String databaseName, final String databaseUUID, final String schemaName, final String schemaUUID,
    final String tableName, final String tableUUID) {
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
}
