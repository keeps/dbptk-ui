/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists;

import java.util.ArrayList;
import java.util.Collections;
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
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
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
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private static final String OPEN_VALIDATED_SPAN = "<span>&#10004;</span>";
  private static final String OPEN_NOT_VALIDATED_SPAN = "<span>&#10006;</span>";

  private Column<ViewerDatabase, SafeHtml> nameColumn;
  private Column<ViewerDatabase, SafeHtml> descriptionColumn;
  private Column<ViewerDatabase, SafeHtml> dbmsColumn;
  private Column<ViewerDatabase, SafeHtml> dataOwnerColumn;
  private Column<ViewerDatabase, SafeHtml> dataOriginTimespanColumn;
  private Column<ViewerDatabase, String> locationColumn;
  private Column<ViewerDatabase, SafeHtml> sizeColumn;
  private Column<ViewerDatabase, SafeHtml> versionColumn;
  private Column<ViewerDatabase, SafeHtml> validColumn;
  private Column<ViewerDatabase, SafeHtml> statusColumn;
  private Column<ViewerDatabase, String> openColumn;

  public DatabaseList() {
    this(new Filter(), null, null, false, false, false);
  }

  protected DatabaseList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable,
    boolean copiable) {
    super(filter, facets, summary, selectable, exportable, copiable, 15, 15);
    autoUpdate(10000);
  }

  @Override
  protected void configureDisplay(CellTable<ViewerDatabase> display) {
    display.setSelectionModel(display.getSelectionModel(), DefaultSelectionEventManager.createBlacklistManager(4, 10));

    nameColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null && database.getMetadata().getName() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getName())
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    nameColumn.setSortable(true);

    descriptionColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null && database.getMetadata().getDescription() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDescription())
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    descriptionColumn.setSortable(true);

    dbmsColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null && database.getMetadata().getDatabaseProduct() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDatabaseProduct())
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    dbmsColumn.setSortable(true);

    dataOwnerColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null && database.getMetadata().getDataOwner() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDataOwner())
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    dataOwnerColumn.setSortable(true);

    dataOriginTimespanColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
          && database.getMetadata().getDataOriginTimespan() != null
            ? SafeHtmlUtils.fromString(database.getMetadata().getDataOriginTimespan())
            : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    dataOriginTimespanColumn.setSortable(true);

    locationColumn = new ButtonColumn<ViewerDatabase>() {
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

    sizeColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(Humanize.readableFileSize(database.getSize()))
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    sizeColumn.setSortable(true);

    versionColumn = new TooltipColumn<ViewerDatabase>() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getVersion())
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    versionColumn.setSortable(true);

    validColumn = new Column<ViewerDatabase, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? LabelUtils.getSIARDValidationStatus(database.getValidationStatus())
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    validColumn.setSortable(true);

    statusColumn = new Column<ViewerDatabase, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? LabelUtils.getDatabaseStatus(database.getStatus())
          : SafeHtmlUtils.fromString(ViewerConstants.UNKNOWN);
      }
    };
    statusColumn.setSortable(true);

    openColumn = new ButtonColumn<ViewerDatabase>() {
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
          addColumn(descriptionColumn, messages.managePageTableHeaderTextForDescription(), true, TextAlign.NONE, 15);
          addColumn(dataOwnerColumn, messages.managePageTableHeaderTextForDataOwner(), true, TextAlign.NONE, 5);
          addColumn(dataOriginTimespanColumn, messages.managePageTableHeaderTextForDataOriginTimespan(), true,
            TextAlign.NONE, 7);
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
      addColumn(descriptionColumn, messages.managePageTableHeaderTextForDescription(), true, TextAlign.NONE, 15);
      addColumn(dataOwnerColumn, messages.managePageTableHeaderTextForDataOwner(), true, TextAlign.NONE, 5);
      addColumn(dataOriginTimespanColumn, messages.managePageTableHeaderTextForArchivalDate(), true, TextAlign.NONE, 5);
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

    if (nameColumn != null) {
      columnSortingKeyMap.put(nameColumn, Collections.singletonList(ViewerConstants.SOLR_DATABASES_METADATA_NAME));
    }
    if (descriptionColumn != null) {
      columnSortingKeyMap.put(descriptionColumn,
        Collections.singletonList(ViewerConstants.SOLR_DATABASES_METADATA_DESCRIPTION));
    }
    if (dataOwnerColumn != null) {
      columnSortingKeyMap.put(dataOwnerColumn,
        Collections.singletonList(ViewerConstants.SOLR_DATABASES_METADATA_DATA_OWNER));
    }
    if (dataOriginTimespanColumn != null) {
      columnSortingKeyMap.put(dataOriginTimespanColumn,
        Collections.singletonList(ViewerConstants.SOLR_DATABASES_METADATA_ORIGIN_TIMESPAN));
    }
    if (dbmsColumn != null) {
      columnSortingKeyMap.put(dbmsColumn,
        Collections.singletonList(ViewerConstants.SOLR_DATABASES_METADATA_DATABASE_PRODUCT));
    }
    if (sizeColumn != null) {
      columnSortingKeyMap.put(sizeColumn, Collections.singletonList(ViewerConstants.SOLR_DATABASES_SIARD_SIZE));
    }
    if (versionColumn != null) {
      columnSortingKeyMap.put(versionColumn, Collections.singletonList(ViewerConstants.SOLR_DATABASES_SIARD_VERSION));
    }
    if (validColumn != null) {
      columnSortingKeyMap.put(validColumn, Collections.singletonList(ViewerConstants.SOLR_DATABASES_VALIDATION_STATUS));
    }
    if (statusColumn != null) {
      columnSortingKeyMap.put(statusColumn, Collections.singletonList(ViewerConstants.SOLR_DATABASES_STATUS));
    }

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    List<String> queryFields = List.of(ViewerConstants.INDEX_SEARCH);

    FindRequest findRequest = new FindRequest(ViewerDatabase.class.getName(), filter, sorter, sublist, getFacets(),
      false, new ArrayList<>(), new HashMap<>(), ViewerConstants.SOLR_EDISMAX, new Filter(), queryFields, false,
      List.of());

    DatabaseService.Util.call(callback).find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName());
  }

  @Override
  public void exportClickHandler() {
    // do nothing
  }

  @Override
  public void selectedToCopyHtml() {

  }
}
