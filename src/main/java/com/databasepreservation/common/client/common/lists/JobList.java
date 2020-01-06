package com.databasepreservation.common.client.common.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.view.client.DefaultSelectionEventManager;
import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.common.lists.columns.TooltipColumn;
import com.databasepreservation.common.client.common.lists.utils.BasicAsyncTableCell;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.services.JobService;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobList extends BasicAsyncTableCell<ViewerJob> {
  @Override
  protected void configureDisplay(CellTable<ViewerJob> display) {

    display.setSelectionModel(display.getSelectionModel(), DefaultSelectionEventManager.createBlacklistManager(4,9));

    Column<ViewerJob, SafeHtml> idColumn = new TooltipColumn<ViewerJob>() {
      @Override
      public SafeHtml getValue(ViewerJob viewerJob) {
        return SafeHtmlUtils.fromString(viewerJob.getUuid());
      }
    };

    Column<ViewerJob, SafeHtml> nameColumn = new TooltipColumn<ViewerJob>() {
      @Override
      public SafeHtml getValue(ViewerJob viewerJob) {
        return SafeHtmlUtils.fromString(viewerJob.getName());
      }
    };

    Column<ViewerJob, SafeHtml> startTimeColumn = new TooltipColumn<ViewerJob>() {
      @Override
      public SafeHtml getValue(ViewerJob viewerJob) {
        return SafeHtmlUtils.fromString(viewerJob.getStartTime());
      }
    };

    Column<ViewerJob, SafeHtml> endTimeColumn = new TooltipColumn<ViewerJob>() {
      @Override
      public SafeHtml getValue(ViewerJob viewerJob) {
        return SafeHtmlUtils.fromString(viewerJob.getEndTime());
      }
    };

    Column<ViewerJob, SafeHtml> statusColumn = new TooltipColumn<ViewerJob>() {
      @Override
      public SafeHtml getValue(ViewerJob viewerJob) {
        return SafeHtmlUtils.fromString(viewerJob.getStatus());
      }
    };

    addColumn(idColumn, "jobId", true, TextAlign.NONE, 15);
    addColumn(nameColumn, "jobName", true, TextAlign.NONE, 15);
    addColumn(startTimeColumn, "startTime", true, TextAlign.NONE, 15);
    addColumn(endTimeColumn, "endTime", true, TextAlign.NONE, 15);
    addColumn(statusColumn, "status", true, TextAlign.NONE, 15);
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    MethodCallback<IndexResult<ViewerJob>> callback) {
    Filter filter = getFilter();

    Map<Column<ViewerJob, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    FindRequest findRequest = new FindRequest(ViewerJob.class.getName(), filter, sorter, sublist, getFacets());

    JobService.Util.call(callback).findJobs(findRequest);
  }

  @Override
  public void exportClickHandler() {
    // do nothing
  }
}
