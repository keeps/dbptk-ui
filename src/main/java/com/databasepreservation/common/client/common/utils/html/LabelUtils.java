/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.utils.html;

import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.Humanize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class LabelUtils {
  private static final String CLOSE_SPAN = "</span>";
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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

    return SafeHtmlUtils.fromSafeConstant("<span class='" + style + "'>" + Humanize.logEntryState(state) + CLOSE_SPAN);
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

  public static SafeHtml getDatabaseStatus(ViewerDatabaseStatus status) {
    String style;
    switch (status) {
      case ERROR:
        style = "label-danger label-error";
        break;
      case AVAILABLE:
        style = "label-success";
        break;
      case INGESTING:
        style = "label-info";
        break;
      case REMOVING:
      case METADATA_ONLY:
      default:
        style = "label-default";
        break;
    }

    return SafeHtmlUtils
      .fromSafeConstant("<span class='" + style + "'>" + Humanize.databaseStatus(status) + CLOSE_SPAN);
  }

  public static SafeHtml getJobStatus(ViewerJobStatus status) {
    String style;
    switch (status) {
      case FAILED:
        style = "label-danger label-error";
        break;
      case COMPLETED:
        style = "label-success";
        break;
      case STARTING:
        style = "label-warning";
        break;
      case STARTED:
      case STOPPING:
      case STOPPED:
        style = "label-info";
        break;
      case ABANDONED:
      case UNKNOWN:
      default:
        style = "label-default";
        break;
    }

    return SafeHtmlUtils.fromSafeConstant("<span class='" + style + "'>" + Humanize.jobStatus(status) + CLOSE_SPAN);
  }

  public static SafeHtml getDatabasePermission(String permission, boolean isGroup) {
    if (isGroup) {
      return SafeHtmlUtils.fromSafeConstant("<span class='label-info btn-margin-right'>" + permission + CLOSE_SPAN);
    } else {
      return SafeHtmlUtils
        .fromSafeConstant("<span class='label-default btn-margin-right'>" + permission + " " + FontAwesomeIconManager
          .getTag("fas fa-exclamation-circle", messages.SIARDHomePageDialogDetailsForUnknownPermission()) + CLOSE_SPAN);
    }
  }
}
