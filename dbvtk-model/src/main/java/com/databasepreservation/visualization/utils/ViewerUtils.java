package com.databasepreservation.visualization.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.databasepreservation.visualization.shared.ViewerSafeConstants;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerUtils {
  public static Date parseDate(String date) throws ParseException {
    Date ret;
    if (date != null) {
      SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(ViewerSafeConstants.ISO8601);
      ret = iso8601DateFormat.parse(date);
    } else {
      ret = null;
    }
    return ret;
  }

  public static String dateToString(Date date) {
    String ret;
    if (date != null) {
      SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(ViewerSafeConstants.ISO8601);
      ret = iso8601DateFormat.format(date);
    } else {
      ret = null;
    }

    return ret;
  }

  public static String jodaDateTimeToString(DateTime dateTime) {
    DateTimeFormatter formatterWithOriginalTimezone = ISODateTimeFormat.dateTime().withZone(dateTime.getZone());
    return dateTime.toString(formatterWithOriginalTimezone);
  }

  public static DateTime parseJodaDateTime(String dateTime) {
    return DateTime.parse(dateTime);
  }
}
