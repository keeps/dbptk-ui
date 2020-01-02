package com.databasepreservation.common.client.common.visualization.activity.log;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.RowField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.FilterHtmlUtils;
import com.databasepreservation.common.client.common.utils.LabelUtils;
import com.databasepreservation.common.client.common.utils.SublistHtmlUtils;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.ActivityLogService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.ViewerJsonUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogDetailedPanel extends ContentPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ActivityLogDetailedPanelUiBinder extends UiBinder<Widget, ActivityLogDetailedPanel> {
  }

  private static ActivityLogDetailedPanelUiBinder uiBinder = GWT.create(ActivityLogDetailedPanelUiBinder.class);

  @UiField
  SimplePanel logHeader;

  @UiField
  FlowPanel content;

  private static Map<String, ActivityLogDetailedPanel> instances = new HashMap<>();
  private ActivityLogWrapper activityLogWrapper;

  public static ActivityLogDetailedPanel getInstance(String logUUID) {
    return instances.computeIfAbsent(logUUID, k -> new ActivityLogDetailedPanel(logUUID));
  }

  private ActivityLogDetailedPanel(String logUUID) {
    initWidget(uiBinder.createAndBindUi(this));

    ActivityLogService.Util.call((ActivityLogWrapper wrapper) -> {
      activityLogWrapper = wrapper;
      init();
    }).retrieve(logUUID);
  }

  private void init() {
    logHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.ACTIVITY_LOG),
      messages.activityLogDetailedHeaderText(), "h1"));

    ActivityLogEntry activityLogEntry = activityLogWrapper.getActivityLogEntry();

    RowField dataField = RowField.createInstance(messages.activityLogTextForDate(),
      new HTML(Humanize.formatDateTime(activityLogEntry.getDatetime(), true)));
    RowField componentField = RowField.createInstance(messages.activityLogTextForComponent(),
      new HTML(messages.activityLogComponent(activityLogEntry.getActionComponent())));
    RowField methodField = RowField.createInstance(messages.activityLogTextForMethod(),
      new HTML(activityLogEntry.getActionMethod()));
    RowField addressField = RowField.createInstance(messages.activityLogTextForAddress(),
      new HTML(activityLogEntry.getAddress()));
    RowField userField = RowField.createInstance(messages.activityLogTextForUser(),
      new HTML(activityLogEntry.getUsername()));
    RowField durationField = RowField.createInstance(messages.activityLogTextForDuration(),
      new HTML(Humanize.durationMillisToShortDHMS(activityLogEntry.getDuration())));

    final RowField parametersField = RowField.createInstance(messages.activityLogTextForParameters(),
      new HTML(activityLogEntry.getParameters().toString()));

    RowField outcomeField = RowField.createInstance(messages.activityLogTextForOutcome(),
      new HTML(LabelUtils.getLogEntryState(activityLogEntry.getState())));

    content.add(dataField);
    content.add(componentField);
    content.add(methodField);
    content.add(addressField);
    content.add(userField);
    content.add(durationField);
    content.add(parametersField);
    content.add(outcomeField);

//    final Map<String, String> parameters = activityLogEntry.getParameters();
//    final String sublistJson = parameters.get(ViewerConstants.CONTROLLER_SUBLIST_PARAM);

//    final Sublist sublist = ViewerJsonUtils.getSubListMapper().read(sublistJson);
//    final SafeHtml sublistHTML = SublistHtmlUtils.getSublistHTML(sublist,
//      Long.parseLong(parameters.get(ViewerConstants.CONTROLLER_RETRIEVE_COUNT)));
//    content.add(new HTML(sublistHTML));
//
//    final String filterJson = parameters.get(ViewerConstants.CONTROLLER_FILTER_PARAM);
//    final Filter filter = ViewerJsonUtils.getFilterMapper().read(filterJson);
//    final SafeHtml filterHTML = FilterHtmlUtils.getFilterHTML(filter, ViewerTable.class.getSimpleName());
//    content.add(new HTML(filterHTML));

    // for (LogEntryParameter entryParameter : activityLogEntry.getParameters()) {
    // if (entryParameter.getName().equals(ViewerConstants.CONTROLLER_FILTER_PARAM))
    // {
    // final Filter read =
    // ViewerJsonUtils.getFilterMapper().read(entryParameter.getValue());
    // final SafeHtml filterHTML = FilterHtmlUtils.getFilterHTML(read,
    // ActivityLogEntry.class.getSimpleName());
    //
    // content.add(new HTML(filterHTML));
    // }
    //
    // if
    // (entryParameter.getName().equals(ViewerConstants.CONTROLLER_SUBLIST_PARAM)) {
    // final Sublist sublist =
    // ViewerJsonUtils.getSubListMapper().read(entryParameter.getValue());
    // // final SafeHtml sublistHTML = SublistHtmlUtils.getSublistHTML(sublist);
    //
    // // content.add(new HTML(sublistHTML));
    // }
    // }
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forActivityLogDetailed());
  }
}
