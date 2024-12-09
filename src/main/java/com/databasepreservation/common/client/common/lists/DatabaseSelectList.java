/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists;

import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.lists.columns.TooltipColumn;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.DefaultSelectionEventManager;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class DatabaseSelectList extends DatabaseList {
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  public DatabaseSelectList() {
    this(new Filter(), null, null, false, false);
  }

  public DatabaseSelectList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, facets, summary, selectable, exportable);
    setPersistSelections(true);
  }

  @Override
  protected void configureDisplay(CellTable<ViewerDatabase> display) {
    display.setSelectionModel(display.getSelectionModel(), DefaultSelectionEventManager.createBlacklistManager(4, 10));
    setSelectedClass(ViewerDatabase.class);

    Column<ViewerDatabase, SafeHtml> nameColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null && database.getMetadata().getName() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getName())
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    Column<ViewerDatabase, SafeHtml> description = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDescription())
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    Column<ViewerDatabase, SafeHtml> dbmsColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null && database.getMetadata().getDatabaseProduct() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDatabaseProduct())
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    Column<ViewerDatabase, SafeHtml> dataOwnerColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDataOwner())
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    Column<ViewerDatabase, SafeHtml> archivalDateColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null && database.getMetadata().getArchivalDate() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getArchivalDate().substring(0, 10))
          : null;
      }
    };

    Column<ViewerDatabase, SafeHtml> sizeColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(Humanize.readableFileSize(database.getSize()))
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    Column<ViewerDatabase, SafeHtml> versionColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getVersion()) : SafeHtmlUtils.fromString("unknown");
      }
    };

    if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          addColumn(nameColumn, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.NONE, 8);
          addColumn(description, messages.managePageTableHeaderTextForDescription(), true, TextAlign.NONE, 15);
          addColumn(dataOwnerColumn, messages.managePageTableHeaderTextForDataOwner(), true, TextAlign.NONE, 5);
          addColumn(archivalDateColumn, messages.managePageTableHeaderTextForArchivalDate(), true, TextAlign.NONE, 5);
          if (user.isAdmin()) {
            addColumn(dbmsColumn, messages.managePageTableHeaderTextForProductName(), true, TextAlign.NONE, 10);
            addColumn(sizeColumn, messages.managePageTableHeaderTextForSIARDSize(), true, TextAlign.NONE, 4);
            addColumn(versionColumn, messages.managePageTableHeaderTextForSIARDVersion(), true, TextAlign.NONE, 4);
          }
        }
      });
    } else {
      addColumn(nameColumn, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.NONE, 8);
      addColumn(description, messages.managePageTableHeaderTextForDescription(), true, TextAlign.NONE, 15);
      addColumn(dataOwnerColumn, messages.managePageTableHeaderTextForDataOwner(), true, TextAlign.NONE, 5);
      addColumn(archivalDateColumn, messages.managePageTableHeaderTextForArchivalDate(), true, TextAlign.NONE, 5);
      addColumn(dbmsColumn, messages.managePageTableHeaderTextForProductName(), true, TextAlign.NONE, 10);
      addColumn(sizeColumn, messages.managePageTableHeaderTextForSIARDSize(), true, TextAlign.NONE, 4);
      addColumn(versionColumn, messages.managePageTableHeaderTextForSIARDVersion(), true, TextAlign.NONE, 4);
    }

    Alert alert = new Alert(Alert.MessageAlertType.LIGHT, messages.noItemsToDisplay());
    display.setEmptyTableWidget(alert);
  }
}
