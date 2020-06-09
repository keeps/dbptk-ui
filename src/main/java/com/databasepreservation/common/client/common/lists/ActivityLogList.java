package com.databasepreservation.common.client.common.lists;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.lists.columns.TooltipColumn;
import com.databasepreservation.common.client.common.lists.utils.BasicAsyncTableCell;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.services.ActivityLogService;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogList extends BasicAsyncTableCell<ActivityLogEntry> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final int INITIAL_PAGE_SIZE = 20;
  private static final int INITIAL_PAGE_SIZE_INCREMENT = 20;

  private Column<ActivityLogEntry, SafeHtml> dateColumn;
  private Column<ActivityLogEntry, SafeHtml> componentColumn;
  private Column<ActivityLogEntry, SafeHtml> methodColumn;
  private Column<ActivityLogEntry, SafeHtml> userColumn;
  private Column<ActivityLogEntry, SafeHtml> durationColumn;
  private Column<ActivityLogEntry, SafeHtml> addressColumn;
  private Column<ActivityLogEntry, SafeHtml> outcomeColumn;
  private final boolean showInUTC;

  public ActivityLogList() {
    this(null, null, null, false, false);
  }

  public ActivityLogList(Filter filter, Facets facets, boolean selectable, boolean exportable) {
    this(filter, facets, null, selectable, exportable);
  }

  private ActivityLogList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, facets, summary, selectable, exportable, INITIAL_PAGE_SIZE, INITIAL_PAGE_SIZE_INCREMENT);
    this.showInUTC = ClientConfigurationManager.getBoolean(false, "ui.interface.show.datetime.utc");
  }

  @Override
  protected void configureDisplay(CellTable<ActivityLogEntry> display) {
    display.setSelectionModel(display.getSelectionModel());
    setSelectedClass(ActivityLogEntry.class);

    dateColumn = new TooltipColumn<ActivityLogEntry>() {
      @Override
      public SafeHtml getValue(ActivityLogEntry log) {
        return log != null
          ? SafeHtmlUtils
            .fromString(Humanize.formatDateTimeFromSolr(log.getDatetime(), "yyyy-MM-dd HH:mm:ss", showInUTC))
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    componentColumn = new TooltipColumn<ActivityLogEntry>() {
      @Override
      public SafeHtml getValue(ActivityLogEntry log) {
        return log != null
          ? SafeHtmlUtils
            .fromString(translate(ViewerConstants.SOLR_ACTIVITY_LOG_ACTION_COMPONENT, log.getActionComponent()))
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    methodColumn = new TooltipColumn<ActivityLogEntry>() {
      @Override
      public SafeHtml getValue(ActivityLogEntry log) {
        return log != null
          ? SafeHtmlUtils.fromString(ViewerStringUtils.getPrettifiedActionMethod(log.getActionMethod()))
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    userColumn = new TooltipColumn<ActivityLogEntry>() {
      @Override
      public SafeHtml getValue(ActivityLogEntry log) {
        return log != null ? SafeHtmlUtils.fromString(log.getUsername()) : SafeHtmlUtils.fromString("unknown");
      }
    };

    durationColumn = new TooltipColumn<ActivityLogEntry>() {
      @Override
      public SafeHtml getValue(ActivityLogEntry log) {
        return log != null ? SafeHtmlUtils.fromString(Humanize.durationMillisToShortDHMS(log.getDuration()))
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    addressColumn = new TooltipColumn<ActivityLogEntry>() {
      @Override
      public SafeHtml getValue(ActivityLogEntry log) {
        return log != null ? SafeHtmlUtils.fromString(log.getAddress()) : SafeHtmlUtils.fromString("unknown");
      }
    };

    outcomeColumn = new Column<ActivityLogEntry, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ActivityLogEntry log) {
        return log != null ? LabelUtils.getLogEntryState(log.getState()) : SafeHtmlUtils.fromString("unknown");
      }
    };

    dateColumn.setSortable(true);
    componentColumn.setSortable(true);
    methodColumn.setSortable(true);
    userColumn.setSortable(true);
    durationColumn.setSortable(true);
    addressColumn.setSortable(true);
    outcomeColumn.setSortable(true);

    addColumn(dateColumn, messages.activityLogTextForDate(), true, TextAlign.NONE, 14);
    addColumn(componentColumn, messages.activityLogTextForComponent(), true, TextAlign.NONE);
    addColumn(methodColumn, messages.activityLogTextForMethod(), true, TextAlign.NONE);
    addColumn(userColumn, messages.activityLogTextForUser(), true, TextAlign.NONE);
    addColumn(durationColumn, messages.activityLogTextForDuration(), true, TextAlign.NONE, 7);
    addColumn(addressColumn, messages.activityLogTextForAddress(), true, TextAlign.NONE);
    addColumn(outcomeColumn, messages.activityLogTextForOutcome(), true, TextAlign.NONE);

    Alert alert = new Alert(Alert.MessageAlertType.LIGHT, messages.noItemsToDisplay());
    display.setEmptyTableWidget(alert);

    // default sorting
    display.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(dateColumn, false));
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    MethodCallback<IndexResult<ActivityLogEntry>> callback) {
    Filter filter = getFilter();

    Map<Column<ActivityLogEntry, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(dateColumn, Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_DATETIME));
    columnSortingKeyMap.put(componentColumn,
      Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_ACTION_COMPONENT));
    columnSortingKeyMap.put(methodColumn, Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_ACTION_METHOD));
    columnSortingKeyMap.put(userColumn, Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_USERNAME));
    columnSortingKeyMap.put(durationColumn, Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_DURATION));
    columnSortingKeyMap.put(addressColumn, Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_IP_ADDRESS));
    columnSortingKeyMap.put(outcomeColumn, Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_STATE));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);
    FindRequest findRequest = new FindRequest(ActivityLogEntry.class.getName(), filter, sorter, sublist, getFacets());

    ActivityLogService.Util.call(callback).find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName());
  }

  @Override
  public void exportClickHandler() {
    // do nothing, for now
  }
}
