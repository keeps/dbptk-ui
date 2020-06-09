/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.tools;

import java.util.Date;

import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

import com.google.gwt.i18n.client.TimeZone;
import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Humanize {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final long ONE_SECOND = 1000;
  public static final long SECONDS = 60;
  public static final long ONE_MINUTE = ONE_SECOND * 60;
  public static final long MINUTES = 60;
  public static final long ONE_HOUR = ONE_MINUTE * 60;
  public static final long HOURS = 24;
  public static final long ONE_DAY = ONE_HOUR * 24;

  public static final String BYTES = "B";
  public static final String KILOBYTES = "KB";
  public static final String MEGABYTES = "MB";
  public static final String GIGABYTES = "GB";
  public static final String TERABYTES = "TB";
  public static final String PETABYTES = "PB";

  public static final String[] UNITS = new String[] {BYTES, KILOBYTES, MEGABYTES, GIGABYTES, TERABYTES, PETABYTES};

  public static final double BYTES_IN_KILOBYTES = 1024L;
  public static final double BYTES_IN_MEGABYTES = 1048576L;
  public static final double BYTES_IN_GIGABYTES = 1073741824L;
  public static final double BYTES_IN_TERABYTES = 1099511627776L;
  public static final double BYTES_IN_PETABYTES = 1125899906842624L;

  public static final double[] BYTES_IN_UNITS = {1, BYTES_IN_KILOBYTES, BYTES_IN_MEGABYTES, BYTES_IN_GIGABYTES,
    BYTES_IN_TERABYTES, BYTES_IN_PETABYTES};

  protected static final NumberFormat SMALL_NUMBER_FORMAT = NumberFormat.getFormat("0.#");
  protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getFormat("#");

  public static Long parseFileSize(String size, String unit) {
    Long ret = null;
    if (size != null && !size.isEmpty()) {
      size = size.trim();
      if (unit.equals(PETABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_PETABYTES);
      } else if (unit.equals(TERABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_TERABYTES);
      } else if (unit.equals(GIGABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_GIGABYTES);
      } else if (unit.equals(MEGABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_MEGABYTES);
      } else if (unit.equals(KILOBYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_KILOBYTES);
      } else if (unit.equals(BYTES)) {
        ret = Long.parseLong(size);
      } else {
        throw new IllegalArgumentException(size);
      }
    }
    return ret;
  }

  public static String readableFileSize(long size) {
    if (size <= 0) {
      return "0 B";
    }
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return NumberFormat.getFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
  }

  public static String formatDateTime(String dateTimeString) {
    if (ViewerStringUtils.isBlank(dateTimeString)) {
      return dateTimeString;
    }

    DateTimeFormat archivalDateFormat = DateTimeFormat.getFormat("yyyy-MM-ddTHH:mm:ss.SSSZZZZ");
    DateTimeFormat normalizedDateFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm (zzzz)");

    try {
      final Date parsed = archivalDateFormat.parse(dateTimeString);
      return normalizedDateFormat.format(parsed);
    } catch (IllegalArgumentException e) {
      return dateTimeString;
    }
  }

  public static String formatDateTimeFromSolr(String dateTimeString, String outputDateTimeFormat) {
    return Humanize.formatDateTimeFromSolr(dateTimeString, outputDateTimeFormat, false);
  }

  public static String formatDateTimeFromSolr(Date datetime, String outputDateTimeFormat, boolean viewInUTC) {
    DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZZZ");
    GWT.log("UTC: " + viewInUTC);
    return Humanize.formatDateTimeFromSolr(fmt.format(datetime), outputDateTimeFormat, viewInUTC);
  }

  public static String formatDateTimeFromSolr(String dateTimeString, String outputDateTimeFormat, boolean viewInUTC) {
    if (ViewerStringUtils.isBlank(dateTimeString)) {
      return dateTimeString;
    }

    DateTimeFormat input = DateTimeFormat.getFormat("yyyy-MM-ddTHH:mm:ss.SSSZZZ");
    DateTimeFormat output = DateTimeFormat.getFormat(outputDateTimeFormat);

    try {
      if (!viewInUTC) {
        return output.format(input.parse(dateTimeString));
      } else {
        return output.format(input.parse(dateTimeString), TimeZone.createTimeZone(0));
      }
    } catch (IllegalArgumentException e) {
      input = DateTimeFormat.getFormat("yyyy-MM-ddTHH:mm:ssZZZ");
      try {
        if (!viewInUTC) {
          return output.format(input.parse(dateTimeString));
        } else {
          return output.format(input.parse(dateTimeString), TimeZone.createTimeZone(0));
        }
      } catch (IllegalArgumentException ea) {
        return dateTimeString;
      }
    }
  }

  public static String formatDateTime(Date date, boolean showTimeZone) {
    DateTimeFormat format;
    if (!showTimeZone) {
      format = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
    } else {
      format = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss zzz");
    }
    return format.format(date);
  }

  public static String formatDateTime(Date date) {
    return formatDateTime(date, false);
  }

  public static String logEntryState(LogEntryState state) {
    switch (state) {
      case SUCCESS:
        return messages.activityLogHumanizedTextForSuccess();
      case FAILURE:
        return messages.activityLogHumanizedTextForFailure();
      case UNAUTHORIZED:
        return messages.activityLogHumanizedTextForUnauthorized();
      case UNKNOWN:
      default:
        return messages.activityLogHumanizedTextForUnknown();
    }
  }

  public static String validationStatus(ViewerDatabaseValidationStatus status) {
    switch (status) {
      case NOT_VALIDATED:
        return messages.humanizedTextForSIARDNotValidated();
      case VALIDATION_SUCCESS:
        return messages.humanizedTextForSIARDValidationSuccess();
      case VALIDATION_FAILED:
        return messages.humanizedTextForSIARDValidationFailed();
      case VALIDATION_RUNNING:
        return messages.humanizedTextForSIARDValidationRunning();
      case ERROR:
        return messages.alertErrorTitle();
      default:
        return "";
    }
  }

  public static String databaseStatus(ViewerDatabaseStatus status) {
    switch (status) {
      case INGESTING:
        return messages.humanizedTextForSolrIngesting();
      case AVAILABLE:
        return messages.humanizedTextForSolrAvailable();
      case METADATA_ONLY:
        return messages.humanizedTextForSolrMetadataOnly();
      case REMOVING:
        return messages.humanizedTextForSolrRemoving();
      case ERROR:
        return messages.humanizedTextForSolrError();
      default:
        return "";
    }
  }

  public static String jobStatus(ViewerJobStatus status) {
    return messages.humanizedTextForViewerJobStatus(status.name());
  }

  /**
   * converts time (in milliseconds) to human-readable format "<dd:>hh:mm:ss"
   */
  public static String durationMillisToShortDHMS(long duration) {
    long d = duration;
    int millis = (int) (d % ONE_SECOND);
    d /= ONE_SECOND;
    int seconds = (int) (d % SECONDS);
    d /= SECONDS;
    int minutes = (int) (d % MINUTES);
    d /= MINUTES;
    int hours = (int) (d % HOURS);
    int days = (int) (d / HOURS);

    if (days > 0) {
      return messages.durationDHMSShortDays(days, hours, minutes, seconds);
    } else if (hours > 0) {
      return messages.durationDHMSShortHours(hours, minutes, seconds);
    } else if (minutes > 0) {
      return messages.durationDHMSShortMinutes(minutes, seconds);
    } else if (seconds > 0) {
      return messages.durationDHMSShortSeconds(seconds);
    } else {
      return messages.durationDHMSShortMillis(millis);
    }
  }
}
