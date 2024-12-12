/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.tools.HistoryManager;
import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.lists.columns.ButtonColumn;
import com.databasepreservation.common.client.common.lists.columns.TooltipColumn;
import com.databasepreservation.common.client.common.lists.utils.BasicAsyncTableCell;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.PathUtils;
import com.databasepreservation.common.client.tools.RestUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.DefaultSelectionEventManager;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class CrossDatabaseList extends BasicAsyncTableCell<ViewerDatabase> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private String searchValue;

  public CrossDatabaseList() {
    this(new Filter(), null, null, false, false);
    this.searchValue = "";
  }

  public CrossDatabaseList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, facets, summary, selectable, exportable, 15, 15);
  }

  @Override
  protected void configureDisplay(CellTable<ViewerDatabase> display) {
    display.setSelectionModel(display.getSelectionModel(), DefaultSelectionEventManager.createBlacklistManager(4, 10));

    display.addCellPreviewHandler(event -> {
      if (event.getNativeEvent().getType().equals("click")) {
        int columnIndex = event.getColumn();
        ViewerDatabase database = event.getValue();

        UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
          @Override
          public void onSuccess(User user) {
            if (database != null) {
              if (user.isAdmin()) {
                if (columnIndex == 7) {
                  HistoryManager.gotoDatabaseSearchWithValue(database.getUuid(), searchValue);
                  getSelectionModel().clear();
                } else {
                  HistoryManager.gotoSIARDInfo(database.getUuid());
                  getSelectionModel().clear();
                }
              } else {
                if (columnIndex == 4) {
                  if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
                    HistoryManager.gotoDatabaseSearchWithValue(database.getUuid(), searchValue);
                  }
                  getSelectionModel().clear();
                } else {
                  if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
                    HistoryManager.gotoDatabase(database.getUuid());
                  }
                  getSelectionModel().clear();
                }
              }
            }
          }
        });
      }
    });

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
        return database != null && database.getMetadata() != null && database.getMetadata().getDescription() != null
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
        return database != null && database.getMetadata() != null && database.getMetadata().getDataOwner() != null
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

    Column<ViewerDatabase, String> locationColumn = new ButtonColumn<ViewerDatabase>() {
      @Override
      public String getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null ? PathUtils.getFileName(database.getPath()) : null;
      }
    };
    locationColumn.setFieldUpdater((index, object, value) -> {
      if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
        JavascriptUtils.showItemInFolder(object.getPath());
      } else {
        SafeUri downloadUri = RestUtils.createFileResourceDownloadSIARDUri(object.getPath());
        Window.Location.assign(downloadUri.asString());
      }
    });

    Column<ViewerDatabase, SafeHtml> validColumn = new Column<ViewerDatabase, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? LabelUtils.getSIARDValidationStatus(database.getValidationStatus()) : null;
      }
    };

    Column<ViewerDatabase, SafeHtml> statusColumn = new Column<ViewerDatabase, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? LabelUtils.getDatabaseStatus(database.getStatus()) : null;
      }
    };

    Column<ViewerDatabase, String> searchHitsColumn = new ButtonColumn<ViewerDatabase>() {
      @Override
      public String getValue(ViewerDatabase database) {
        if (searchValue != null && !searchValue.isEmpty()) {
          return database != null
            ? String.valueOf(database.getSearchHits()) + " " + messages.manageDatabaseSearchAllResults()
            : "unknown";
        } else {
          return database != null ? "" : "unknown";
        }
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
            addColumn(validColumn, messages.managePageTableHeaderTextForSIARDValidationStatus(), true, TextAlign.NONE,
              5);
            addColumn(statusColumn, messages.managePageTableHeaderTextForDatabaseStatus(), true, TextAlign.NONE, 5);
          }
          addColumn(searchHitsColumn, messages.managePageTableHeaderTextForSearchHits(), true, TextAlign.NONE, 5);
        }
      });
    } else {
      addColumn(nameColumn, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.NONE, 8);
      addColumn(description, messages.managePageTableHeaderTextForDescription(), true, TextAlign.NONE, 15);
      addColumn(dataOwnerColumn, messages.managePageTableHeaderTextForDataOwner(), true, TextAlign.NONE, 5);
      addColumn(archivalDateColumn, messages.managePageTableHeaderTextForArchivalDate(), true, TextAlign.NONE, 5);
      addColumn(locationColumn, messages.managePageTableHeaderTextForSIARDLocation(), true, TextAlign.NONE, 8);
      addColumn(dbmsColumn, messages.managePageTableHeaderTextForProductName(), true, TextAlign.NONE, 10);
      addColumn(validColumn, messages.managePageTableHeaderTextForSIARDValidationStatus(), true, TextAlign.NONE, 5);
      addColumn(statusColumn, messages.managePageTableHeaderTextForDatabaseStatus(), true, TextAlign.NONE, 5);
      addColumn(searchHitsColumn, messages.managePageTableHeaderTextForSearchHits(), true, TextAlign.NONE, 5);
    }

    Alert alert = new Alert(Alert.MessageAlertType.LIGHT, messages.noItemsToDisplay());
    display.setEmptyTableWidget(alert);
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    MethodCallback<IndexResult<ViewerDatabase>> callback) {
    Filter filter = getFilter();

    Map<Column<ViewerDatabase, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    FindRequest findRequest = new FindRequest(ViewerDatabase.class.getName(), filter, sorter, sublist, getFacets());

    DatabaseService.Util.call(callback).findAll(findRequest, LocaleInfo.getCurrentLocale().getLocaleName());
  }

  public String getSearchValue() {
    return searchValue;
  }

  public void setSearchValue(String searchValue) {
    this.searchValue = searchValue;
  }

  @Override
  public void exportClickHandler() {
    // do nothing
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    // refresh();
  }
}
