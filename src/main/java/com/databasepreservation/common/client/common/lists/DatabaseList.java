package com.databasepreservation.common.client.common.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ClientLogger;
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
import com.databasepreservation.common.client.tools.HistoryManager;
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
public class DatabaseList extends BasicAsyncTableCell<ViewerDatabase> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private static final String OPEN_VALIDATED_SPAN = "<span>&#10004;</span>";
  private static final String OPEN_NOT_VALIDATED_SPAN = "<span>&#10006;</span>";

  public DatabaseList() {
    this(new Filter(), null, null, false, false);
  }

  private DatabaseList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, facets, summary, selectable, exportable, 15, 15);
    autoUpdate(10000);
  }

  @Override
  protected void configureDisplay(CellTable<ViewerDatabase> display) {
    display.setSelectionModel(display.getSelectionModel(), DefaultSelectionEventManager.createBlacklistManager(4, 10));

    Column<ViewerDatabase, SafeHtml> nameColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
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
        return database != null && database.getMetadata() != null
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
        return database != null && database.getMetadata() != null
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

    Column<ViewerDatabase, String> openColumn = new ButtonColumn<ViewerDatabase>() {
      @Override
      public String getValue(ViewerDatabase object) {
        return messages.basicActionOpen();
      }
    };
    openColumn.setFieldUpdater((index, object, value) -> HistoryManager.gotoSIARDInfo(object.getUuid()));

    if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
        @Override
        public void onSuccess(User user) {
          addColumn(nameColumn, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.NONE, 8);
          addColumn(description, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.NONE, 15);
          addColumn(dataOwnerColumn, messages.managePageTableHeaderTextForDataOwner(), true, TextAlign.NONE, 5);
          addColumn(archivalDateColumn, messages.managePageTableHeaderTextForArchivalDate(), true, TextAlign.NONE, 5);
          if (user.isAdmin()) {
            addColumn(dbmsColumn, messages.managePageTableHeaderTextForProductName(), true, TextAlign.NONE, 10);
            addColumn(sizeColumn, messages.managePageTableHeaderTextForSIARDSize(), true, TextAlign.NONE, 4);
            addColumn(versionColumn, messages.managePageTableHeaderTextForSIARDVersion(), true, TextAlign.NONE, 4);
            addColumn(validColumn, messages.managePageTableHeaderTextForSIARDValidationStatus(), true, TextAlign.NONE,
              5);
            addColumn(statusColumn, messages.managePageTableHeaderTextForDatabaseStatus(), true, TextAlign.NONE, 5);
            addColumn(openColumn, messages.managePageTableHeaderTextForActions(), true, TextAlign.NONE, 5);
          }
        }
      });
    } else {
      addColumn(nameColumn, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.NONE, 8);
      addColumn(description, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.NONE, 15);
      addColumn(dataOwnerColumn, messages.managePageTableHeaderTextForDataOwner(), true, TextAlign.NONE, 5);
      addColumn(archivalDateColumn, messages.managePageTableHeaderTextForArchivalDate(), true, TextAlign.NONE, 5);
      addColumn(locationColumn, messages.managePageTableHeaderTextForSIARDLocation(), true, TextAlign.NONE, 8);
      addColumn(dbmsColumn, messages.managePageTableHeaderTextForProductName(), true, TextAlign.NONE, 10);
      addColumn(sizeColumn, messages.managePageTableHeaderTextForSIARDSize(), true, TextAlign.NONE, 4);
      addColumn(versionColumn, messages.managePageTableHeaderTextForSIARDVersion(), true, TextAlign.NONE, 4);
      addColumn(validColumn, messages.managePageTableHeaderTextForSIARDValidationStatus(), true, TextAlign.NONE, 5);
      addColumn(statusColumn, messages.managePageTableHeaderTextForDatabaseStatus(), true, TextAlign.NONE, 5);
      addColumn(openColumn, messages.managePageTableHeaderTextForActions(), true, TextAlign.NONE, 5);
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

    DatabaseService.Util.call(callback).find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName());
  }

  @Override
  public void exportClickHandler() {
    // do nothing
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    refresh();
  }
}
