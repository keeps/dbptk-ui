package com.databasepreservation.common.client.common.visualization.activity.log;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.RowField;
import com.databasepreservation.common.client.common.utils.ActivityLogUtils;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.services.ActivityLogService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
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

  private ActivityLogWrapper activityLogWrapper;

  public static ActivityLogDetailedPanel getInstance(String logUUID) {
    return new ActivityLogDetailedPanel(logUUID);
  }

  private ActivityLogDetailedPanel(String logUUID) {
    initWidget(uiBinder.createAndBindUi(this));

    ActivityLogService.Util.call((ActivityLogWrapper wrapper) -> {
      activityLogWrapper = wrapper;
      init();
    }).retrieve(logUUID);
  }

  private void init() {
    logHeader.setWidget(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.ACTIVITY_LOG),
      messages.activityLogDetailedHeaderText(), "h1"));

    ActivityLogEntry activityLogEntry = activityLogWrapper.getActivityLogEntry();

    RowField dataField = RowField.createInstance(messages.activityLogTextForDate(),
      new HTML(Humanize.formatDateTime(activityLogEntry.getDatetime(), true)));
    RowField componentField = RowField.createInstance(messages.activityLogTextForComponent(),
      new HTML(messages.activityLogComponent(activityLogEntry.getActionComponent())));
    RowField methodField = RowField.createInstance(messages.activityLogTextForMethod(),
      new HTML(ViewerStringUtils.getPrettifiedActionMethod(activityLogEntry.getActionMethod())));
    RowField addressField = RowField.createInstance(messages.activityLogTextForAddress(),
      new Anchor(activityLogEntry.getAddress(), "https://ipinfo.io/" + activityLogEntry.getAddress(), "_blank"));
    RowField userField = RowField.createInstance(messages.activityLogTextForUser(),
      new HTML(activityLogEntry.getUsername()));
    RowField durationField = RowField.createInstance(messages.activityLogTextForDuration(),
      new HTML(Humanize.durationMillisToShortDHMS(activityLogEntry.getDuration())));

    // final RowField parametersFieldTreated =
    // RowField.createInstance(messages.activityLogTextForParameters(), );
    FlowPanel panel = new FlowPanel();
    panel.add(ActivityLogUtils.getParametersHTML(activityLogWrapper));

    RowField outcomeField = RowField.createInstance(messages.activityLogTextForOutcome(),
      new HTML(LabelUtils.getLogEntryState(activityLogEntry.getState())));

    content.add(dataField);
    content.add(durationField);
    content.add(userField);
    content.add(addressField);
    content.add(componentField);
    content.add(methodField);
    content.add(panel);
    content.add(outcomeField);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forActivityLogDetailed());
  }
}
