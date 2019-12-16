package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.tools.Humanize;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class LabelUtils {
  private static final String CLOSE_SPAN = "</span>";

  public static SafeHtml getLogEntryState(LogEntryState state) {
    String style;
    switch (state) {
      case UNAUTHORIZED:
        style = "label-warning";
        break;
      case FAILURE:
        style = "label-danger";
        break;
      case SUCCESS:
        style = "label-success";
        break;
      case UNKNOWN:
      default:
        style = "label-default";
        break;
    }

    return SafeHtmlUtils
        .fromSafeConstant("<span class='" + style + "'>" + Humanize.logEntryState(state) + CLOSE_SPAN);
  }

  public static SafeHtml getSIARDValidationStatus(ViewerDatabaseValidationStatus status) {
    String style;
    switch (status) {
      case ERROR:
        style = "label-danger label-error";
        break;
      case VALIDATION_FAILED:
        style = "label-danger";
        break;
      case VALIDATION_SUCCESS:
        style = "label-success";
        break;
      case NOT_VALIDATED:
      case VALIDATION_RUNNING:
      default:
        style = "label-default";
        break;
    }

    return SafeHtmlUtils
        .fromSafeConstant("<span class='" + style + "'>" + Humanize.validationStatus(status) + CLOSE_SPAN);
  }
}
